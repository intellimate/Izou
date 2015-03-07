package intellimate.izou.output;

import intellimate.izou.AddonThreadPoolUser;
import intellimate.izou.IdentifiableCollection;
import intellimate.izou.IzouModule;
import intellimate.izou.events.Event;
import intellimate.izou.main.Main;
import intellimate.izou.resource.Resource;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IdentificationManager;
import intellimate.izouSDK.resource.ResourceImpl;

import java.util.*;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * OutputManager manages all output plugins and is the main class anyone outside the output package should talk to.
 * It can register/remove new output-plugins and add/delete output-extensions
 */
public class OutputManager extends IzouModule implements AddonThreadPoolUser {

    /**
     * a list that contains all the registered output-plugins of Jarvis
     */
    IdentifiableCollection<OutputPlugin> outputPlugins;

    /**
     * HashMap that stores the future objects of the output-plugins
     */
    private HashMap<String, Future> futureHashMap;

    /**
     * a HashMap that stores all outputExtensions which were to be added to a still non-existent output-plugins,
     * this HashMap gets checked every time a new output-plugin is added for relevant output-extensions
     */
    private HashMap<String, List<OutputExtension>> tempExtensionStorage;


    /**
     * Creates a new output-manager with a list of output-plugins
     * @param main the main instance started from
     */
    public OutputManager(Main main) {
        super(main);
        outputPlugins = new IdentifiableCollection<>();
        futureHashMap = new HashMap<>();
        tempExtensionStorage = new HashMap<>();
        if (!IdentificationManager.getInstance().registerIdentification(this)) {
            log.fatal("Unable to obtain ID for" + getID());
        }
    }

    /**
     * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
     * @param outputPlugin OutputPlugin to add
     */
    public void addOutputPlugin(OutputPlugin outputPlugin) {
        if (!futureHashMap.containsKey(outputPlugin.getID())) {
            outputPlugins.add(outputPlugin);
            futureHashMap.put(outputPlugin.getID(), submit(outputPlugin));
        } else {
            if (futureHashMap.get(outputPlugin.getID()).isDone()) {
                futureHashMap.remove(outputPlugin.getID());
                futureHashMap.put(outputPlugin.getID(), submit(outputPlugin));
            }
        }

        if (tempExtensionStorage.containsKey(outputPlugin.getID())) {
            for(OutputExtension oE: tempExtensionStorage.get(outputPlugin.getID())) {
                try {
                    //noinspection unchecked
                    outputPlugin.addOutputExtension(oE);
                }
                catch (ClassCastException e) {
                    log.warn(e);
                }
            }
            tempExtensionStorage.remove(outputPlugin.getID());
        }
    }

    /**
     * adds output extension to desired outputPlugin
     *
     * adds output extension to desired outputPlugin, so that the output-plugin can start and stop the outputExtension
     * task as needed. The outputExtension is specific to the output-plugin
     *
     * @param outputExtension the outputExtension to be added
     */
    public void addOutputExtension(OutputExtension outputExtension) {
        for (OutputPlugin oPlug: outputPlugins) {
            if (oPlug.getID().equals(outputExtension.getPluginId())) {
                try {
                    //noinspection unchecked
                    oPlug.addOutputExtension(outputExtension);
                } catch (ClassCastException e) {
                    log.error("Error while trying to add the OutputExtension: " + outputExtension.getID()
                                    + " to the OutputPlugin: " + oPlug.getID(), e);
                }
                break;
            }
        }
        if(tempExtensionStorage.containsKey(outputExtension.getPluginId())) {
            tempExtensionStorage.get(outputExtension.getPluginId()).add(outputExtension);
        }
        else {
            List<OutputExtension> outputExtensionList = new ArrayList<>();
            outputExtensionList.add(outputExtension);
            tempExtensionStorage.put(outputExtension.getPluginId(), outputExtensionList);
        }
    }

    /**
     * removes the output-extension of id: extensionId from outputPluginList
     *
     * @param pluginId the id of the output-plugin in which the output-extension should be removed
     * @param extensionId the id of output-extension to be removed
     */
    public void removeOutputExtension(String pluginId, String extensionId) {
        for(OutputPlugin oPlug: outputPlugins) {
            if(oPlug.getID().equals(pluginId)) {
                oPlug.removeOutputExtension(extensionId);
                break;
            }
        }
    }

    /**
     * gets the Event and sends it to the right outputPlugin for further processing
     *
     * passDataToOutputPlugins is the main method of OutputManger. It is called whenever the output process has to be started
     *
     * @param event an Instance of Event
     */
    public void passDataToOutputPlugins(Event event) {
        IdentificationManager identificationManager = IdentificationManager.getInstance();
        List<Identification> allIds = outputPlugins.stream()
                .map(identificationManager::getIdentification)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        HashMap<Integer, List<Identification>> outputPluginBehaviour = event.getEventBehaviourController()
                .getOutputPluginBehaviour(allIds);

        Set<OutputPlugin> outputPluginsCopy = this.outputPlugins.getCopy();
        
        Function<List<Identification>, List<OutputPlugin>> getOutputPlugin = ids -> ids.stream()
                .map(id -> outputPluginsCopy.stream()
                        .filter(outputPlugin -> outputPlugin.isOwner(id))
                        .findFirst()
                        .orElseGet(null))
                .filter(Objects::nonNull)
                .peek(outputPluginsCopy::remove)
                .collect(Collectors.toList());
        
        outputPluginBehaviour.entrySet().stream()
                .sorted(Comparator.comparingInt((Map.Entry<Integer, List<Identification>> x) -> x.getKey()).reversed())
                .flatMap(entry -> getOutputPlugin.apply(entry.getValue()).stream())
                .distinct()
                .forEach(op -> processOutputPlugins(event, op));
        
        outputPluginsCopy.forEach(op -> processOutputPlugins(event, op));
    }

    private void processOutputPlugins(Event event, OutputPlugin outputPlugin) {
       final Lock lock = new ReentrantLock();
       final Condition processing = lock.newCondition();

       Consumer<Boolean> consumer = noParam -> {
           lock.lock();
           processing.signal();
           lock.unlock();
       };
        
        Resource<Consumer<Boolean>> resource = IdentificationManager.getInstance().getIdentification(this)
               .map(id -> (Resource<Consumer<Boolean>>) new ResourceImpl<>(outputPlugin.getID(), id, consumer))
               .orElse(new ResourceImpl<Consumer<Boolean>>(outputPlugin.getID()).setResource(consumer));
       event.getListResourceContainer().addResource(resource);

       outputPlugin.addToEventList(event);

       boolean finished = false;
       try {
           lock.lock();
           finished = processing.await(100, TimeUnit.SECONDS);
       } catch (InterruptedException e) {
           log.error("Waiting for OutputPlugins interrupted", e);
       } finally {
           lock.unlock();
       }
       if(finished) {
           log.debug("OutputPlugin: " + outputPlugin.getID() + " finished");
       } else {
           log.error("OutputPlugin: " + outputPlugin.getID() + " timed out");
       }
   }
}
