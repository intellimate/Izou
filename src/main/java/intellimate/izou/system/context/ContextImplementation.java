package intellimate.izou.system.context;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.Event;
import intellimate.izou.events.EventCallable;
import intellimate.izou.events.EventListener;
import intellimate.izou.events.EventsController;
import intellimate.izou.identification.Identification;
import intellimate.izou.main.Main;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceBuilder;
import intellimate.izou.system.Context;
import intellimate.izou.system.file.FileSubscriber;
import intellimate.izou.system.file.ReloadableFile;
import intellimate.izou.system.logger.IzouLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * This class provides much of the general Communication with Izou.
 */
public class ContextImplementation implements Context {
    private final AddOn addOn;
    private final Main main;
    private final Events events = new EventsImpl();
    private final Resources resources = new ResourcesImpl();
    private final Files files;
    private final ExtendedLogger logger;
    private final ThreadPool threadPool;
    private final Activators activators = new ActivatorsImpl();
    private final Output output = new OutputImpl();

    /**
     * creates a new context for the addOn
     *
     * A context contains all the "global" or generally necessary information an addOn might need that it otherwise does
     * not have access too
     *
     * @param addOn the addOn for which to create a new context
     * @param main instance of main
     * @param logLevel the logLevel to initialize the IzouLogger with
     */
    public ContextImplementation(AddOn addOn, Main main, String logLevel) {
        this.addOn = addOn;
        this.main = main;
        this.files = new FilesImpl();
        this.threadPool = new ThreadPoolImpl();

        IzouLogger izouLogger = main.getIzouLogger();
        ExtendedLogger logger = null;
        if (izouLogger != null)
            this.logger = izouLogger.createFileLogger(addOn.getID(), logLevel);
        else {
            this.logger = null;
            org.apache.logging.log4j.Logger fileLogger = LogManager.getLogger(this.getClass());
            fileLogger.error("IzouLogger has not been initialized");
            throw new NullPointerException("IzouLogger has not been initialized");
        }
    }

    /**
     * returns the API used for interaction with Events
     * @return Events
     */
    @Override
    public Events getEvents() {
        return events;
    }

    /**
     * returns the API used for interaction with Resource
     * @return Resource
     */
    @Override
    public Resources getResources() {
        return resources;
    }

    /**
     * returns the API used for interaction with Files
     * @return Files
     */
    @Override
    public Files getFiles() {
        return files;
    }

    /**
     * gets logger for addOn
     *
     * @return the logger
     */
    @Override
    public ExtendedLogger getLogger() {
        return logger;
    }

    /**
     * returns the API used to manage the ThreadPool
     * @return ThreadPool
     */
    @Override
    public ThreadPool getThreadPool() {
        return threadPool;
    }

    /**
     * returns the API to manage the Activators
     * @return Activator
     */
    @Override
    public Activators getActivators() {
        return activators;
    }

    /**
     * returns the API used to manage the OutputPlugins and OutputExtensions
     * @return Output
     */
    @Override
    public Output getOutput() {
        return output;
    }

    /**
     * gets addOn
     *
     * @return the addOn
     */
    @Override
    public AddOn getAddOn() {
        return addOn;
    }

    private class FilesImpl implements Files {
        /**
         * Use this method to register a file with the watcherService
         *
         * @param dir directory of file
         * @param fileType the name/extension of the file
         *                 IMPORTANT: Please try to always enter the full name with extension of the file (Ex: "test.txt"),
         *                 it would be best if the fileType is the full file name, and that the file name is clearly
         *                 distinguishable from other files.
         *                 For example, the property files are stored with the ID of the addon they belong too. That way
         *                 every property file is easily distinguishable.
         * @param reloadableFile object of interface that file belongs to
         * @throws IOException exception thrown by watcher service
         */
        @Override
        public void registerFileDir(Path dir, String fileType, ReloadableFile reloadableFile) throws IOException {
            main.getFileManager().registerFileDir(dir, fileType, reloadableFile);
        }

        /**
         * Writes default file to real file
         * The default file would be a file that can be packaged along with the code, from which a real file (say a
         * properties file for example) can be loaded. This is useful because there are files (like property files0 that
         * cannot be shipped with the package and have to be created at runtime. To still be able to fill these files, you
         * can create a default file (usually txt) from which the content, as mentioned above, can then be loaded into the
         * real file.
         *
         * @param defaultFilePath path to default file (or where it should be created)
         * @param realFilePath path to real file (that should be filled with content of default file)
         * @return true if operation has succeeded, else false
         */
        @Override
        public boolean writeToFile(String defaultFilePath, String realFilePath) {
            return main.getFileManager().writeToFile(defaultFilePath, realFilePath);
        }

        /**
         * Creates a default File in case it does not exist yet. Default files can be used to load other files that are
         * created at runtime (like properties file)
         *
         * @param defaultFilePath path to default file.txt (or where it should be created)
         * @param initMessage the string to write in default file
         * @throws java.io.IOException is thrown by bufferedWriter
         */
        @Override
        public void createDefaultFile(String defaultFilePath, String initMessage) throws IOException {
            main.getFileManager().createDefaultFile(defaultFilePath, initMessage);
        }

        /**
         * Registers a {@link FileSubscriber} with a {@link ReloadableFile}. So when the {@code reloadableFile} is
         * reloaded, the fileSubscriber will be notified. Multiple file subscribers can be registered with the same
         * reloadable file.
         *
         * @param reloadableFile the reloadable file that should be observed
         * @param fileSubscriber the fileSubscriber that should be notified when the reloadable file is reloaded
         * @param identification the Identification of the requesting instance
         */
        @Override
        public void register(ReloadableFile reloadableFile, FileSubscriber fileSubscriber, Identification identification) {
            main.getFilePublisher().register(reloadableFile, fileSubscriber, identification);
        }

        /**
         * Registers a {@link intellimate.izou.system.file.FileSubscriber} so that whenever any file is reloaded, the fileSubscriber is notified.
         *
         * @param fileSubscriber the fileSubscriber that should be notified when the reloadable file is reloaded
         * @param identification the Identification of the requesting instance
         */
        @Override
        public void register(FileSubscriber fileSubscriber, Identification identification) {
           main.getFilePublisher().register(fileSubscriber, identification);
        }

        /**
         * Unregisters all instances of fileSubscriber found.
         *
         * @param fileSubscriber the fileSubscriber to unregister
         */
        @Override
        public void unregister(FileSubscriber fileSubscriber) {
           main.getFilePublisher().unregister(fileSubscriber);
        }
    }

    private class EventsImpl implements Events {
        public EventsDistributor eventsDistributor = new DistributorImpl();
        /**
         * Adds an listener for events.
         * <p>
         * Be careful with this method, it will register the listener for ALL the informations found in the Event. If your
         * event-type is a common event type, it will fire EACH time!.
         * It will also register for all Descriptors individually!
         * It will also ignore if this listener is already listening to an Event.
         * Method is thread-safe.
         * </p>
         * @param event the Event to listen to (it will listen to all descriptors individually!)
         * @param eventListener the ActivatorEventListener-interface for receiving activator events
         */
        @Override
        public void registerEventListener(Event event, EventListener eventListener) {
            main.getEventDistributor().registerEventListener(event, eventListener);
        }

        /**
         * Adds an listener for events.
         * <p>
         * It will register for all ids individually!
         * This method will ignore if this listener is already listening to an Event.
         * Method is thread-safe.
         * </p>
         * @param ids this can be type, or descriptors etc.
         * @param eventListener the ActivatorEventListener-interface for receiving activator events
         */
        @Override
        public void registerEventListener(List<String> ids, EventListener eventListener) {
            main.getEventDistributor().registerEventListener(ids, eventListener);
        }
        /**
         * unregister an EventListener
         *<p>
         * It will unregister for all Descriptors individually!
         * It will also ignore if this listener is not listening to an Event.
         * Method is thread-safe.
         *
         * @param event the Event to stop listen to
         * @param eventListener the ActivatorEventListener used to listen for events
         * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
         */
        @Override
        public void unregisterEventListener(Event event, EventListener eventListener) {
            main.getEventDistributor().unregisterEventListener(event, eventListener);
        }

        /**
         * Registers with the LocalEventManager to fire an event.
         * <p>
         * Note: the same Event can be fired from multiple sources.
         * Method is thread-safe.
         * @param identification the Identification of the the instance
         * @return an Optional, empty if already registered
         */
        @Override
        public Optional<EventCallable> registerEventCaller(Identification identification) {
            return main.getLocalEventManager().registerCaller(identification);
        }

        /**
         * Unregister with the LocalEventManager.
         * <p>
         * Method is thread-safe.
         * @param identification the Identification of the the instance
         */
        @Override
        public void unregisterEventCaller(Identification identification) {
            main.getLocalEventManager().unregisterCaller(identification);
        }

        /**
         * Adds the event ID of {@code value} to the PopularEvents.properties file with a key of {@code key}
         *
         * @param description a short description of what the event ID is for, should not be null
         * @param key the key with which to store the event ID, should not be null
         * @param value the complete event ID, should not be null
         */
        @Override
        public void addEventIDToPropertiesFile(String description, String key, String value) {
            main.getEventPropertiesManager().registerEventID(description, key, value);
        }

        /**
         * Gets the full event ID associated with the key {@code key}
         *
         * @param key the key of the full event ID
         * @return the complete the event ID, or null if none is found
         */
        @Override
        public String getEventsID(String key) {
            return main.getEventPropertiesManager().getEventID(key);
        }

        /**
         * returns the API for the EventsDistributor
         * @return Distributor
         */
        @Override
        public EventsDistributor distributor() {
            return eventsDistributor;
        }

        private class DistributorImpl implements EventsDistributor {
            /**
             * with this method you can register EventPublisher add a Source of Events to the System.
             * <p>
             * This method represents a higher level of abstraction! Use the EventManager to fire Events!
             * This method is intended for use cases where you have an entire new source of events (e.g. network)
             * @param identification the Identification of the Source
             * @return An Optional Object which may or may not contains an EventPublisher
             */
            @Override
            public Optional<EventCallable> registerEventPublisher(Identification identification) {
                return main.getEventDistributor().registerEventPublisher(identification);
            }

            /**
             * with this method you can unregister EventPublisher add a Source of Events to the System.
             * <p>
             * This method represents a higher level of abstraction! Use the EventManager to fire Events!
             * This method is intended for use cases where you have an entire new source of events (e.g. network)
             * @param identification the Identification of the Source
             */
            @Override
            public void unregisterEventPublisher(Identification identification) {
                main.getEventDistributor().unregisterEventPublisher(identification);
            }

            /**
             * Registers an EventController to control EventDispatching-Behaviour
             * <p>
             * Method is thread-safe.
             * It is expected that this method executes quickly.
             *
             * @param eventsController the EventController Interface to control event-dispatching
             */
            @Override
            public void registerEventsController(EventsController eventsController) {
                main.getEventDistributor().registerEventsController(eventsController);
            }

            /**
             * Unregisters an EventController
             * <p>
             * Method is thread-safe.
             *
             * @param eventsController the EventController Interface to remove
             */
            @Override
            public void unregisterEventsController(EventsController eventsController) {
                main.getEventDistributor().unregisterEventsController(eventsController);
            }
        }
    }

    private class ResourcesImpl implements Resources {
        /**
         * registers a ResourceBuilder.
         * <p>
         *  this method registers all the events, resourcesID etc.
         * </p>
         * @param resourceBuilder an instance of the ResourceBuilder
         */
        @Override
        public void registerResourceBuilder(ResourceBuilder resourceBuilder) {
            main.getResourceManager().registerResourceBuilder(resourceBuilder);
        }

        /**
         * unregister a ResourceBuilder.
         * <p>
         * this method unregisters all the events, resourcesID etc.
         * @param resourceBuilder an instance of the ResourceBuilder
         */
        @Override
        public void unregisterResourceBuilder(ResourceBuilder resourceBuilder) {
            main.getResourceManager().unregisterResourceBuilder(resourceBuilder);
        }

        /**
         * generates a resources
         * <p>
         * @param resource the resource to request
         * @param consumer the callback when the ResourceBuilder finishes
         */
        @Override
        @Deprecated
        public void generateResource(Resource resource, Consumer<List<Resource>> consumer) {
            main.getResourceManager().generatedResource(resource, consumer);
        }

        /**
         * generates a resources
         * <p>
         * It will use the first matching resource! So if you really want to be sure, set the provider
         * Identification
         * </p>
         * @param resource the resource to request
         * @return an optional of an CompletableFuture
         */
        @Override
        public Optional<CompletableFuture<List<Resource>>> generateResource(Resource resource) {
            return main.getResourceManager().generateResource(resource);
        }
    }

    private class ThreadPoolImpl implements ThreadPool {
        /**
         * Submits a new Callable to the ThreadPool
         * @param callable the callable to submit
         * @param <V> the type of the callable
         * @return a Future representing pending completion of the task
         */
        @Override
        public <V> Future<V> submitToIzouThreadPool(Callable<V> callable) {
            return main.getThreadPoolManager().getAddOnsThreadPool().submit(callable);
        }

        /**
         * returns an ThreadPool where all the IzouPlugins are running
         * @return an instance of ExecutorService
         */
        @Override
        public ExecutorService getThreadPool() {
            return main.getThreadPoolManager().getAddOnsThreadPool();
        }
    }

    private class ActivatorsImpl implements Activators {
        /**
         * adds an activator and automatically submits it to the Thread-Pool
         * @param activator the activator to add
         */
        @Override
        public void addActivator(intellimate.izou.activator.Activator activator) {
            main.getActivatorManager().addActivator(activator);
        }

        /**
         * removes the activator and stops the Thread
         * @param activator the activator to remove
         */
        @Override
        public void removeActivator(intellimate.izou.activator.Activator activator) {
            main.getActivatorManager().removeActivator(activator);
        }
    }

    public class OutputImpl implements Output {
        /**
         * adds output extension to desired outputPlugin
         *
         * adds output extension to desired outputPlugin, so that the output-plugin can start and stop the outputExtension
         * task as needed. The outputExtension is specific to the output-plugin
         *
         * @param outputExtension the outputExtension to be added
         */
        @Override
        public void addOutputExtension(OutputExtension outputExtension) {
            main.getOutputManager().addOutputExtension(outputExtension);
        }

        /**
         * removes the output-extension of id: extensionId from outputPluginList
         *
         * @param outputExtension the OutputExtension to remove
         */
        @Override
        public void removeOutputExtension(OutputExtension outputExtension) {
            main.getOutputManager().removeOutputExtension(outputExtension);
        }

        /**
         * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
         * @param outputPlugin OutputPlugin to add
         */
        @Override
        public void addOutputPlugin(OutputPlugin outputPlugin) {
            main.getOutputManager().addOutputPlugin(outputPlugin);
        }

        /**
         * removes the OutputPlugin and stops the thread
         * @param outputPlugin the outputPlugin to remove
         */
        @Override
        public void removeOutputPlugin(OutputPlugin outputPlugin) {
            main.getOutputManager().removeOutputPlugin(outputPlugin);
        }
    }
}
