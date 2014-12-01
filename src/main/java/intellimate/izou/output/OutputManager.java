package intellimate.izou.output;

import intellimate.izou.events.Event;
import intellimate.izou.resource.Resource;
import intellimate.izou.system.Identifiable;
import intellimate.izou.system.Identification;
import intellimate.izou.system.IdentificationManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * OutputManager manages all output plugins and is the main class anyone outside the output package should talk to.
 * It can register/remove new output-plugins and add/delete output-extensions
 */
public class OutputManager implements Identifiable{
    public static final String ID = OutputManager.class.getCanonicalName();

    /**
     * a list that contains all the registered output-plugins of Jarvis
     */
    private List<OutputPlugin> outputPluginsList;

    /**
     * responsible for running output-plugins in different threads
     */
    private final ExecutorService executor;

    /**
     * HashMap that stores the future objects of the output-plugins
     */
    private HashMap<String, Future> futureHashMap;

    /**
     * a HashMap that stores all outputExtensions which were to be added to a still non-existent output-plugins,
     * this HashMap gets checked every time a new output-plugin is added for relevant output-extensions
     */
    private HashMap<String, List<OutputExtension>> tempExtensionStorage;

    private final Logger fileLogger = LogManager.getLogger(this.getClass());


    /**
     * Creates a new output-manager with a list of output-plugins
     */
    public OutputManager() {
        outputPluginsList = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
        futureHashMap = new HashMap<>();
        tempExtensionStorage = new HashMap<>();
        if (!IdentificationManager.getInstance().registerIdentification(this)) {
            //TODO log fatal
        }
    }

    /**
     * returns the list containing all the registered outputPlugins of the output-manager
     *
     * @return list containing all the registered outputPlugins
     */
    public List<OutputPlugin> getOutputPluginsList() {
        return outputPluginsList;
    }

    /**
     * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
     * @param outputPlugin OutputPlugin to add
     */
    public void addOutputPlugin(OutputPlugin outputPlugin) {
        if (!futureHashMap.containsKey(outputPlugin.getID())) {
            outputPlugin.setExecutor(executor);
            outputPluginsList.add(outputPlugin);
            futureHashMap.put(outputPlugin.getID(), executor.submit(outputPlugin));
        } else {
            if (futureHashMap.get(outputPlugin.getID()).isDone()) {
                outputPlugin.setExecutor(executor);
                futureHashMap.remove(outputPlugin.getID());
                futureHashMap.put(outputPlugin.getID(), executor.submit(outputPlugin));
            }
        }

        if (tempExtensionStorage.containsKey(outputPlugin.getID())) {
            for(OutputExtension oE: tempExtensionStorage.get(outputPlugin.getID())) {
                try {
                    //noinspection unchecked
                    outputPlugin.addOutputExtension(oE);
                }
                catch (ClassCastException e) {
                    fileLogger.warn(e.getMessage());
                }
            }
            tempExtensionStorage.remove(outputPlugin.getID());
        }
    }

    public OutputPlugin getOutputPlugin(String id) {
        for (OutputPlugin oP: outputPluginsList) {
            if (oP.getID().equals(id)) {
                return oP;
            }
        }
        return null;
    }

    /**
     * removes the output-plugin of id: pluginId from outputPluginList and ends the thread
     *
     * @param pluginId the id of the output-plugin to remove
     */
    public void removeOutputPlugin(String pluginId) {
        for(OutputPlugin oPlug: outputPluginsList) {
            if(oPlug.getID().equals(pluginId)) {
                outputPluginsList.remove(oPlug);
                oPlug.setExecutor(null);
                futureHashMap.get(oPlug.getID()).cancel(true);
                futureHashMap.remove(oPlug.getID());
                break;
            }
        }
    }

    /**
     * adds output extension to desired outputPlugin
     *
     * adds output extension to desired outputPlugin, so that the output-plugin can start and stop the outputExtension
     * task as needed. The outputExtension is specific to the output-plugin
     *
     * @param outputExtension the outputExtension to be added
     * @param outputPluginId the output-plugin the outputExtension is to be added to
     */
    public void addOutputExtension(OutputExtension outputExtension, String outputPluginId) {
        boolean found = false;
        for (OutputPlugin oPlug: outputPluginsList) {
            if (oPlug.getID().equals(outputPluginId)) {
                try {
                    //noinspection unchecked
                    oPlug.addOutputExtension(outputExtension);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                outputExtension.setPluginId(oPlug.getID());
                found = true;
                break;
            }
        }
        if (!found) {
            if(tempExtensionStorage.containsKey(outputPluginId)) {
                tempExtensionStorage.get(outputPluginId).add(outputExtension);
            }
            else {
                List<OutputExtension> outputExtensionList = new ArrayList<>();
                outputExtensionList.add(outputExtension);
                tempExtensionStorage.put(outputPluginId, outputExtensionList);
            }
        }
    }

    /**
     * removes the output-extension of id: extensionId from outputPluginList
     *
     * @param pluginId the id of the output-plugin in which the output-extension should be removed
     * @param extensionId the id of output-extension to be removed
     */
    public void removeOutputExtension(String pluginId, String extensionId) {
        for(OutputPlugin oPlug: outputPluginsList) {
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
        List<Identification> ids = outputPluginsList.stream()
                .map(identificationManager::getIdentification)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        HashMap<Integer, List<Identification>> outputPluginBehaviour = event.getEventBehaviourController()
                .getOutputPluginBehaviour(ids);

        Set<Integer> keySet = outputPluginBehaviour.keySet();

        List<OutputPlugin> orderedPositive = keySet.stream()
                .sorted()
                .filter(integer -> integer >= 0)
                .map(outputPluginBehaviour::get)
                .flatMap(Collection::stream)
                .distinct()
                .map(id -> outputPluginsList.stream()
                        .filter(outputPlugin -> outputPlugin.isOwner(id))
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        List<OutputPlugin> orderedNegative = keySet.stream()
                .sorted()
                .filter(integer -> integer < 0)
                .map(outputPluginBehaviour::get)
                .flatMap(Collection::stream)
                .distinct()
                .map(id -> outputPluginsList.stream()
                        .filter(outputPlugin -> outputPlugin.isOwner(id))
                        .findFirst())
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        List<Identification> all = outputPluginBehaviour.values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        @SuppressWarnings("SuspiciousMethodCalls")
        List<OutputPlugin> orderedNeutral = outputPluginsList.stream()
                .filter(outputPlugin -> !all.contains(outputPlugin))
                .collect(Collectors.toList());

        processOutputPlugins(event, orderedPositive);
        processOutputPlugins(event, orderedNeutral);
        processOutputPlugins(event, orderedNegative);
    }

    private void processOutputPlugins(Event event, List<OutputPlugin> outputPlugins) {
       for(OutputPlugin outputPlugin : outputPlugins) {
           Optional<Identification> managerID = IdentificationManager.getInstance().getIdentification(this);

           if(!managerID.isPresent()) continue;
           Resource<Consumer> resource = new Resource<>(outputPlugin.getID());
           resource.setProvider(managerID.get());

           final boolean[] stop = {false};
           Consumer<Boolean> consumer = noParam -> stop[0] = true;
           resource.setResource(consumer);

           outputPlugin.distributeEvent(event);
           outputPlugin.addToEventList(event);

           int counter = 0;
           while(!stop[0] && (counter < 10)) {
               try {
                   Thread.sleep(100);
                   counter++;
               } catch (InterruptedException e) {
                   //TODO: logging
               }
           }
       }
    }

    @Override
    public String getID() {
        return ID;
    }
}
