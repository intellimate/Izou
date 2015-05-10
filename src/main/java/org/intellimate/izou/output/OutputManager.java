package org.intellimate.izou.output;

import com.google.common.reflect.TypeToken;
import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.identification.IdentificationManagerM;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.resource.ResourceMinimalImpl;

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
    private IdentifiableSet<OutputPluginModel<?, ?>> outputPlugins;

    /**
     * HashMap that stores OutputPlugins and the Future objects representing the Task
     */
    private HashMap<String, Future> futureHashMap;

    /**
     * this HashMap stores all added OutputExtensions
     */
    private HashMap<String, IdentifiableSet<OutputExtensionModel<?, ?>>> outputExtensions;


    /**
     * Creates a new output-manager with a list of output-plugins
     * @param main the main instance started from
     */
    public OutputManager(Main main) {
        super(main);
        outputPlugins = new IdentifiableSet<>();
        futureHashMap = new HashMap<>();
        outputExtensions = new HashMap<>();
    }

    /**
     * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
     * @param outputPlugin OutputPlugin to add
     * @throws IllegalIDException not yet implemented
     */
    public void addOutputPlugin(OutputPluginModel<?, ?> outputPlugin) throws IllegalIDException {
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
    public void removeOutputPlugin(OutputPluginModel outputPlugin) {
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
    public void addOutputExtension(OutputExtensionModel<?, ?> outputExtension) throws IllegalIDException {
        if(outputExtensions.containsKey(outputExtension.getPluginId())) {
            outputExtensions.get(outputExtension.getPluginId()).add(outputExtension);
        }
        else {
            IdentifiableSet<OutputExtensionModel<?, ?>> outputExtensionList = new IdentifiableSet<>();
            outputExtensionList.add(outputExtension);
            outputExtensions.put(outputExtension.getPluginId(), outputExtensionList);
        }
        IdentificationManager.getInstance().getIdentification(outputExtension)
                .ifPresent(id -> outputPlugins.stream()
                        .filter(outputPlugin -> outputPlugin.getID().equals(outputExtension.getPluginId()))
                        .forEach(outputPlugin -> outputPlugin.outputExtensionAdded(id)));
    }

    /**
     * removes the output-extension of id: extensionId from outputPluginList
     *
     * @param outputExtension the OutputExtension to remove
     */
    public void removeOutputExtension(OutputExtensionModel<?, ?> outputExtension) {
        IdentifiableSet<OutputExtensionModel<?, ?>> outputExtensions =
                this.outputExtensions.get(outputExtension.getPluginId());
        if (outputExtensions != null)
            outputExtensions.remove(outputExtension);
        IdentificationManager.getInstance().getIdentification(outputExtension)
                .ifPresent(id -> outputPlugins.stream()
                        .filter(outputPlugin -> outputPlugin.getID().equals(outputExtension.getPluginId()))
                        .forEach(outputPlugin -> outputPlugin.outputExtensionRemoved(id)));
    }

    /**
     * gets the Event and sends it to the right outputPlugin for further processing
     *
     * passDataToOutputPlugins is the main method of OutputManger. It is called whenever the output process has to be started
     *
     * @param event an Instance of Event
     */
    public void passDataToOutputPlugins(EventModel event) {
        IdentificationManagerM identificationManager = IdentificationManager.getInstance();
        List<Identification> allIds = outputPlugins.stream()
                .map(identificationManager::getIdentification)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        
        HashMap<Integer, List<Identification>> outputPluginBehaviour = event.getEventBehaviourController()
                .getOutputPluginBehaviour(allIds);

        @SuppressWarnings("unchecked")
        Set<OutputPluginModel> outputPluginsCopy = (Set<OutputPluginModel>) this.outputPlugins.clone();
        
        Function<List<Identification>, List<OutputPluginModel>> getOutputPlugin = ids -> ids.stream()
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

    private void processOutputPlugin(EventModel event, OutputPluginModel outputPlugin) {
        debug("processing outputPlugin: " + outputPlugin.getID() + " for event: " + event.getDescriptors().toString());
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
           error("Waiting for OutputPlugins interrupted", e);
       } finally {
           lock.unlock();
       }
       if(finished) {
           debug("OutputPlugin: " + outputPlugin.getID() + " finished");
       } else {
           error("OutputPlugin: " + outputPlugin.getID() + " timed out");
       }
   }

    /**
     * returns all the associated OutputExtensions
     * @param outputPlugin the OutputPlugin to search for
     * @return a List of Identifications
     */
    //TODO: TEST!!!!
    public List<Identification> getAssociatedOutputExtension(OutputPluginModel<?, ?> outputPlugin) {
        IdentifiableSet<OutputExtensionModel<?, ?>> outputExtensions = this.outputExtensions.get(outputPlugin.getID());
        IdentificationManagerM identificationManager = IdentificationManager.getInstance();
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
    private List<OutputExtensionModel<?, ?>> filterType(Collection<OutputExtensionModel<?, ?>> outputExtensions,
                                                   OutputPluginModel<?, ?> outputPlugin) {
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
                        isAssignable.test(outputExtension.getReturnType(), outputPlugin.getReceivingType()))
                .collect(Collectors.toList());
    }

    /**
     * starts every associated OutputExtension
     * @param outputPlugin the OutputPlugin to generate the Data for
     * @param t the argument or null
     * @param event the Event to generate for
     * @param <T> the type of the argument
     * @param <X> the return type
     * @return a List of Future-Objects
     */
    //TODO: Test! is it working?
    public <T, X> List<CompletableFuture<X>> generateAllOutputExtensions(OutputPluginModel<T, X> outputPlugin,
                                                                                T t, EventModel event) {
        IdentifiableSet<OutputExtensionModel<?, ?>> extensions = outputExtensions.get(outputPlugin.getID());
        if (extensions == null)
            return new ArrayList<>();
        return filterType(extensions, outputPlugin).stream()
                .map(extension -> {
                    try {
                        //noinspection unchecked
                        return (OutputExtensionModel<X, T>) extension;
                    } catch (ClassCastException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .filter(outputExtension -> outputExtension.canRun(event))
                .map(extension -> submit(() -> extension.generate(event, t)))
                .collect(Collectors.toList());
    }
}
