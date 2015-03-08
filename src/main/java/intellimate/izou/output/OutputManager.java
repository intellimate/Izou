package intellimate.izou.output;

import com.google.common.reflect.TypeToken;
import intellimate.izou.AddonThreadPoolUser;
import intellimate.izou.IdentifiableSet;
import intellimate.izou.IzouModule;
import intellimate.izou.events.Event;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IdentificationManager;
import intellimate.izou.identification.IllegalIDException;
import intellimate.izou.main.Main;
import intellimate.izou.resource.ResourceMinimalImpl;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiPredicate;
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
    private IdentifiableSet<OutputPlugin<?, ?>> outputPlugins;

    /**
     * HashMap that stores OutputPlugins and the Future objects representing the Task
     */
    private HashMap<String, Future> futureHashMap;

    /**
     * this HashMap stores all added OutputExtensions
     */
    private HashMap<String, IdentifiableSet<OutputExtension<?, ?>>> outputExtensions;


    /**
     * Creates a new output-manager with a list of output-plugins
     * @param main the main instance started from
     */
    public OutputManager(Main main) {
        super(main);
        outputPlugins = new IdentifiableSet<>();
        futureHashMap = new HashMap<>();
        outputExtensions = new HashMap<>();
        if (!IdentificationManager.getInstance().registerIdentification(this)) {
            log.fatal("Unable to obtain ID for" + getID());
        }
    }

    /**
     * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
     * @param outputPlugin OutputPlugin to add
     * @throws IllegalIDException not yet implemented
     */
    public void addOutputPlugin(OutputPlugin<?, ?> outputPlugin) throws IllegalIDException {
        if (!futureHashMap.containsKey(outputPlugin.getID())) {
            outputPlugins.add(outputPlugin);
            futureHashMap.put(outputPlugin.getID(), submit(outputPlugin));
        } else {
            if (futureHashMap.get(outputPlugin.getID()).isDone()) {
                futureHashMap.remove(outputPlugin.getID());
                futureHashMap.put(outputPlugin.getID(), submit(outputPlugin));
            }
        }
    }

    /**
     * removes the OutputPlugin and stops the thread
     * @param outputPlugin the outputPlugin to remove
     */
    public void removeOutputPlugin(OutputPlugin outputPlugin) {
        Future future = futureHashMap.remove(outputPlugin.getID());
        if (future != null) {
            future.cancel(true);
        }
        outputPlugins.remove(outputPlugin);
    }

    /**
     * adds output extension to desired outputPlugin
     *
     * adds output extension to desired outputPlugin, so that the output-plugin can start and stop the outputExtension
     * task as needed. The outputExtension is specific to the output-plugin
     *
     * @param outputExtension the outputExtension to be added
     * @throws IllegalIDException not yet implemented
     */
    public void addOutputExtension(OutputExtension<?, ?> outputExtension) throws IllegalIDException {
        if(outputExtensions.containsKey(outputExtension.getPluginId())) {
            outputExtensions.get(outputExtension.getPluginId()).add(outputExtension);
        }
        else {
            IdentifiableSet<OutputExtension<?, ?>> outputExtensionList = new IdentifiableSet<>();
            outputExtensionList.add(outputExtension);
            outputExtensions.put(outputExtension.getPluginId(), outputExtensionList);
        }
    }

    /**
     * removes the output-extension of id: extensionId from outputPluginList
     *
     * @param outputExtension the OutputExtension to remove
     */
    public void removeOutputExtension(OutputExtension<?, ?> outputExtension) {
        IdentifiableSet<OutputExtension<?, ?>> outputExtensions =
                this.outputExtensions.get(outputExtension.getPluginId());
        if (outputExtensions != null)
            outputExtensions.remove(outputExtension);
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

        @SuppressWarnings("unchecked")
        Set<OutputPlugin> outputPluginsCopy = (Set<OutputPlugin>) this.outputPlugins.clone();
        
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
                .forEach(op -> processOutputPlugin(event, op));
        
        outputPluginsCopy.forEach(op -> processOutputPlugin(event, op));
    }

    private void processOutputPlugin(Event event, OutputPlugin outputPlugin) {
       final Lock lock = new ReentrantLock();
       final Condition processing = lock.newCondition();

       Consumer<Boolean> consumer = noParam -> {
           lock.lock();
           processing.signal();
           lock.unlock();
       };
        
        ResourceMinimalImpl<Consumer<Boolean>> resource = IdentificationManager.getInstance().getIdentification(this)
               .map(id -> new ResourceMinimalImpl<>(outputPlugin.getID(), id, consumer, null))
               .orElse(new ResourceMinimalImpl<>(outputPlugin.getID(), null, consumer, null));
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

    /**
     * returns all the associated OutputExtensions
     * @param outputPlugin the OutputPlugin to search for
     * @return a List of Identifications
     */
    //TODO: TEST!!!!
    public List<Identification> getAssociatedOutputExtension(OutputPlugin<?, ?> outputPlugin) {
        IdentifiableSet<OutputExtension<?, ?>> outputExtensions = this.outputExtensions.get(outputPlugin.getID());
        IdentificationManager identificationManager = IdentificationManager.getInstance();
        return filterType(outputExtensions, outputPlugin).stream()
                .map(identificationManager::getIdentification)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * checks for the right type
     * @param outputExtensions the OutputExtensions to check
     * @param outputPlugin the OutputPlugin to check against
     * @return a List of filtered OutputExtensions
     */
    //TODO: TEST!!!!
    @SuppressWarnings("SimplifiableIfStatement")
    private List<OutputExtension<?, ?>> filterType(Collection<OutputExtension<?, ?>> outputExtensions,
                                                   OutputPlugin<?, ?> outputPlugin) {
        BiPredicate<TypeToken<?>, TypeToken<?>> isAssignable = (first, second) -> {
            if (first == null) {
                return second == null;
            } else if (second != null) {
                return first.isAssignableFrom(second);
            } else {
                return false;
            }
        };
        return outputExtensions.stream()
                .filter(outputExtension ->
                        isAssignable.test(outputExtension.getArgumentType(), outputPlugin.getArgumentType()))
                .filter(outputExtension ->
                        isAssignable.test(outputExtension.getReturnType(), outputPlugin.getRecievingType()))
                .collect(Collectors.toList());
    }

    /**
     * starts every associated OutputExtension
     * @param outputPlugin the OutputPlugin to generate the Data for
     * @param x the argument or null
     * @param event the Event to generate for
     * @return a List of Future-Objects
     */
    //TODO: Test! is it working?
    public <T, X> List<CompletableFuture<T>> generateAllOutputExtensions(OutputPlugin<T, X> outputPlugin,
                                                                                X x, Event event) {
        IdentifiableSet<OutputExtension<?, ?>> extensions = outputExtensions.get(outputPlugin.getID());
        return filterType(extensions, outputPlugin).stream()
                .map(extension -> {
                    try {
                        //noinspection unchecked
                        return (OutputExtension<X, T>) extension;
                    } catch (ClassCastException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(outputExtension -> outputExtension.canRun(event))
                .map(extension -> submit(() -> extension.generate(event, x)))
                .collect(Collectors.toList());
    }
}
