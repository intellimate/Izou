package org.intellimate.izou.internal.system.context;

import org.intellimate.izou.activator.ActivatorModel;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.events.*;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.internal.identification.IdentificationManagerImpl;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.internal.main.Main;
import org.intellimate.izou.output.OutputExtensionModel;
import org.intellimate.izou.output.OutputPluginModel;
import org.intellimate.izou.resource.ResourceModel;
import org.intellimate.izou.resource.ResourceBuilderModel;
import org.intellimate.izou.security.storage.SecureStorage;
import org.intellimate.izou.system.Context;
import org.intellimate.izou.system.context.*;
import org.intellimate.izou.system.context.System;
import org.intellimate.izou.system.file.FileSubscriber;
import org.intellimate.izou.system.file.ReloadableFile;
import org.intellimate.izou.internal.system.logger.IzouLogger;
import org.intellimate.izou.internal.threadpool.TrackingExecutorService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;

/**
 * This class provides much of the general Communication with Izou.
 */
public class ContextImplementation implements Context {
    private final AddOnModel addOn;
    private final Main main;
    private final Events events = new EventsImpl();
    private final Resources resources = new ResourcesImpl();
    private final Files files;
    private final ExtendedLogger logger;
    private final ThreadPool threadPool;
    private final Activators activators = new ActivatorsImpl();
    private final Output output = new OutputImpl();
    private final System system = new SystemImpl();

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
    public ContextImplementation(AddOnModel addOn, Main main, String logLevel) {
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
     * retruns the API used to interact with Izou.
     *
     * @return System.
     */
    @Override
    public System getSystem() {
        return system;
    }

    /**
     * Gets the Secure Storage object of Izou. {@link SecureStorage} allows addOns to safely store data so that other
     * addOns cannot access it. However while the data is encrypted, there is no guarantee that the end user cannot
     * decrypt the data and access it. So be careful to not store any sensitive information here
     *
     * @return the Secure Storage object of Izou
     */
    @Override
    public SecureStorage getSecureStorage() {
        return main.getSecureStorageManager();
    }

    /**
     * gets addOn
     *
     * @return the addOn
     */
    @Override
    public AddOnModel getAddOn() {
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
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void register(ReloadableFile reloadableFile, FileSubscriber fileSubscriber,
                             Identification identification) throws IllegalIDException {
            main.getFilePublisher().register(reloadableFile, fileSubscriber, identification);
        }

        /**
         * Registers a {@link FileSubscriber} so that whenever any file is reloaded, the fileSubscriber is notified.
         *
         * @param fileSubscriber the fileSubscriber that should be notified when the reloadable file is reloaded
         * @param identification the Identification of the requesting instance
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void register(FileSubscriber fileSubscriber, Identification identification) throws IllegalIDException {
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

        /**
         * gets the File pointing towards the location of the lib-folder
         *
         * @return the File
         */
        @Override
        public File getLibLocation() {
            return main.getFileSystemManager().getLibLocation();
        }

        /**
         * gets the File pointing towards the location of the resource-folder
         *
         * @return the File
         */
        @Override
        public File getResourceLocation() {
            return main.getFileSystemManager().getResourceLocation();
        }

        /**
         * gets the File pointing towards the location of the properties-folder
         *
         * @return the File
         */
        @Override
        public File getPropertiesLocation() {
            return main.getFileSystemManager().getPropertiesLocation();
        }

        /**
         * gets the File pointing towards the location of the logs-folder
         *
         * @return the File
         */
        @Override
        public File getLogsLocation() {
            return main.getFileSystemManager().getLogsLocation();
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
         * @throws IllegalIDException not yet implemented
         */
        @SuppressWarnings("JavaDoc")
        @Override
        public void registerEventListener(EventModel event, EventListenerModel eventListener) throws IllegalIDException {
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
        public void registerEventListener(List<String> ids, EventListenerModel eventListener) {
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
        public void unregisterEventListener(EventModel event, EventListenerModel eventListener) {
            main.getEventDistributor().unregisterEventListener(event, eventListener);
        }

        /**
         * unregister an EventListener that gets called before the generation of the resources and the outputPlugins.
         * <p>
         * It will unregister for all Descriptors individually!
         * It will also ignore if this listener is not listening to an Event.
         * Method is thread-safe.
         *
         * @param eventListener the ActivatorEventListener used to listen for events
         * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
         */
        @Override
        public void unregisterEventListener(EventListenerModel eventListener) {
            main.getEventDistributor().unregisterEventListener(eventListener);
        }

        /**
         * Adds an listener for events that gets called when the event finished processing.
         * <p>
         * Be careful with this method, it will register the listener for ALL the informations found in the Event. If your
         * event-type is a common event type, it will fire EACH time!.
         * It will also register for all Descriptors individually!
         * It will also ignore if this listener is already listening to an Event.
         * Method is thread-safe.
         * </p>
         *
         * @param event         the Event to listen to (it will listen to all descriptors individually!)
         * @param eventListener the ActivatorEventListener-interface for receiving activator events
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void registerEventFinishedListener(EventModel event, EventListenerModel eventListener) throws IllegalIDException {
            main.getEventDistributor().registerEventFinishedListener(event, eventListener);
        }

        /**
         * Adds an listener for events that gets called when the event finished processing.
         * <p>
         * It will register for all ids individually!
         * This method will ignore if this listener is already listening to an Event.
         * Method is thread-safe.
         * </p>
         *
         * @param ids           this can be type, or descriptors etc.
         * @param eventListener the ActivatorEventListener-interface for receiving activator events
         */
        @Override
        public void registerEventFinishedListener(List<String> ids, EventListenerModel eventListener) {
            main.getEventDistributor().registerEventFinishedListener(ids, eventListener);
        }

        /**
         * unregister an EventListener that got called when the event finished processing.
         * <p>
         * It will unregister for all Descriptors individually!
         * It will also ignore if this listener is not listening to an Event.
         * Method is thread-safe.
         *
         * @param event         the Event to stop listen to
         * @param eventListener the ActivatorEventListener used to listen for events
         * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
         */
        @Override
        public void unregisterEventFinishedListener(EventModel event, EventListenerModel eventListener) {
            main.getEventDistributor().unregisterEventFinishedListener(event, eventListener);
        }

        /**
         * unregister an EventListener that got called when the event finished processing.
         * <p>
         * It will unregister for all Descriptors individually!
         * It will also ignore if this listener is not listening to an Event.
         * Method is thread-safe.
         *
         * @param eventListener the ActivatorEventListener used to listen for events
         * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
         */
        @Override
        public void unregisterEventFinishedListener(EventListenerModel eventListener) {
            main.getEventDistributor().unregisterEventFinishedListener(eventListener);
        }

        /**
         * Registers with the LocalEventManager to fire an event.
         * <p>
         * Note: the same Event can be fired from multiple sources.
         * Method is thread-safe.
         * @param identification the Identification of the the instance
         * @return an Optional, empty if already registered
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public Optional<EventCallable> registerEventCaller(Identification identification) throws IllegalIDException {
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
         * This method fires an Event
         *
         * @param event the fired Event
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void fireEvent(EventModel event) throws IllegalIDException, MultipleEventsException {
            main.getLocalEventManager().fireEvent(event);
        }

        /**
         * returns the API for the EventsDistributor
         * @return Distributor
         */
        @Override
        public EventsDistributor distributor() {
            return eventsDistributor;
        }

        /**
         * returns the ID of the Manager (LocalEventManager)
         */
        @Override
        public Identification getManagerIdentification() {
            Optional<Identification> identification = IdentificationManagerImpl.getInstance()
                    .getIdentification(main.getLocalEventManager());
            if (!identification.isPresent()) {
                //should not happen
                throw new RuntimeException("unable to obtain ID for LocalEventManager");
            }
            return identification.get();
        }

        private class DistributorImpl implements EventsDistributor {
            /**
             * with this method you can register EventPublisher add a Source of Events to the System.
             * <p>
             * This method represents a higher level of abstraction! Use the EventManager to fire Events!
             * This method is intended for use cases where you have an entire new source of events (e.g. network)
             * @param identification the Identification of the Source
             * @return An Optional Object which may or may not contains an EventPublisher
             * @throws IllegalIDException not yet implemented
             */
            @Override
            public Optional<EventCallable> registerEventPublisher(Identification identification) throws IllegalIDException {
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
             * @throws IllegalIDException not yet implemented
             */
            @Override
            public void registerEventsController(EventsControllerModel eventsController) throws IllegalIDException {
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
            public void unregisterEventsController(EventsControllerModel eventsController) {
                main.getEventDistributor().unregisterEventsController(eventsController);
            }

            /**
             * fires the event concurrently, this is generally discouraged.
             * <p>
             * This method should not be used for normal Events, for for events which obey the following laws:<br>
             * 1. they are time critical.<br>
             * 2. addons are not expected to react in any way beside a small update<br>
             * 3. they are few.<br>
             * if your event matches the above laws, you may consider firing it concurrently.
             * </p>
             *
             * @param eventModel the EventModel
             */
            @Override
            public void fireEventConcurrently(EventModel<?> eventModel) {
                main.getEventDistributor().fireEventConcurrently(eventModel);
            }

            /**
             * returns the ID of the Manager (EventsDistributor)
             */
            @Override
            public Identification getManagerIdentification() {
                Optional<Identification> identification = IdentificationManagerImpl.getInstance()
                        .getIdentification(main.getEventDistributor());
                if (!identification.isPresent()) {
                    //should not happen
                    throw new RuntimeException("unable to obtain ID for EventsDistributor");
                }
                return identification.get();
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
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void registerResourceBuilder(ResourceBuilderModel resourceBuilder) throws IllegalIDException {
            main.getResourceManager().registerResourceBuilder(resourceBuilder);
        }

        /**
         * unregister a ResourceBuilder.
         * <p>
         * this method unregisters all the events, resourcesID etc.
         * @param resourceBuilder an instance of the ResourceBuilder
         */
        @Override
        public void unregisterResourceBuilder(ResourceBuilderModel resourceBuilder) {
            main.getResourceManager().unregisterResourceBuilder(resourceBuilder);
        }

        /**
         * generates a resources
         * <p>
         * @param resource the resource to request
         * @param consumer the callback when the ResourceBuilder finishes
         * @throws IllegalIDException not yet implemented
         */
        @Override
        @Deprecated
        public void generateResource(ResourceModel resource, Consumer<List<ResourceModel>> consumer) throws IllegalIDException {
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
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public Optional<CompletableFuture<List<ResourceModel>>> generateResource(ResourceModel resource) throws IllegalIDException {
            return main.getResourceManager().generateResource(resource);
        }

        /**
         * returns the ID of the Manager
         */
        @Override
        public Identification getManagerIdentification() {
            Optional<Identification> identification = IdentificationManagerImpl.getInstance()
                    .getIdentification(main.getResourceManager());
            if (!identification.isPresent()) {
                //should not happen
                throw new RuntimeException("unable to obtain ID for ResourceManager");
            }
            return identification.get();
        }
    }

    private class ThreadPoolImpl implements ThreadPool {
        /**
         * returns an ThreadPool where all the IzouPlugins are running
         * @param identifiable the Identifiable to set each created Task as the Source
         * @return an instance of ExecutorService
         * @throws IllegalIDException not implemented yet
         */
        @Override
        public ExecutorService getThreadPool(Identifiable identifiable) throws IllegalIDException {
            return TrackingExecutorService.createTrackingExecutorService(
                    main.getThreadPoolManager().getAddOnsThreadPool(), identifiable);
        }

        /**
         * tries everything to log the exception
         *
         * @param throwable the Throwable
         * @param target    an instance of the thing which has thrown the Exception
         */
        @Override
        public void handleThrowable(Throwable throwable, Object target) {
            main.getThreadPoolManager().handleThrowable(throwable, target);
        }

        /**
         * returns the ID of the Manager
         */
        @Override
        public Identification getManagerIdentification() {
            Optional<Identification> identification = IdentificationManagerImpl.getInstance()
                    .getIdentification(main.getEventDistributor());
            if (!identification.isPresent()) {
                //should not happen
                throw new RuntimeException("unable to obtain ID for ThreadPoolManager");
            }
            return identification.get();
        }
    }

    private class ActivatorsImpl implements Activators {
        /**
         * adds an activator and automatically submits it to the Thread-Pool
         * @param activatorModel the activator to add
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void addActivator(ActivatorModel activatorModel) throws IllegalIDException {
            main.getActivatorManager().addActivator(activatorModel);
        }

        /**
         * removes the activator and stops the Thread
         * @param activatorModel the activator to remove
         */
        @Override
        public void removeActivator(ActivatorModel activatorModel) {
            main.getActivatorManager().removeActivator(activatorModel);
        }

        /**
         * returns the ID of the Manager
         */
        @Override
        public Identification getManagerIdentification() {
            Optional<Identification> identification = IdentificationManagerImpl.getInstance()
                    .getIdentification(main.getActivatorManager());
            if (!identification.isPresent()) {
                //should not happen
                throw new RuntimeException("unable to obtain ID for ActivatorManager");
            }
            return identification.get();
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
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void addOutputExtension(OutputExtensionModel outputExtension) throws IllegalIDException {
            main.getOutputManager().addOutputExtension(outputExtension);
        }

        /**
         * removes the output-extension of id: extensionId from outputPluginList
         *
         * @param outputExtension the OutputExtension to remove
         */
        @Override
        public void removeOutputExtension(OutputExtensionModel outputExtension) {
            main.getOutputManager().removeOutputExtension(outputExtension);
        }

        /**
         * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
         * @param outputPlugin OutputPlugin to add
         * @throws IllegalIDException not yet implemented
         */
        @Override
        public void addOutputPlugin(OutputPluginModel outputPlugin) throws IllegalIDException {
            main.getOutputManager().addOutputPlugin(outputPlugin);
        }

        /**
         * removes the OutputPlugin and stops the thread
         * @param outputPlugin the outputPlugin to remove
         */
        @Override
        public void removeOutputPlugin(OutputPluginModel outputPlugin) {
            main.getOutputManager().removeOutputPlugin(outputPlugin);
        }

        /**
         * returns all the associated OutputExtensions
         *
         * @param outputPlugin the OutputPlugin to search for
         * @return a List of Identifications
         */
        @Override
        public List<Identification> getAssociatedOutputExtension(OutputPluginModel<?, ?> outputPlugin) {
            return main.getOutputManager().getAssociatedOutputExtension(outputPlugin);
        }

        /**
         * starts every associated OutputExtension
         *
         * @param outputPlugin the OutputPlugin to generate the Data for
         * @param t            the argument or null
         * @param event        the Event to generate for  @return a List of Future-Objects
         * @param <T>          the type of the argument
         * @param <X>          the return type
         * @return             a List of Future-Objects
         */
        @Override
        public <T, X> List<CompletableFuture<X>> generateAllOutputExtensions(OutputPluginModel<T, X> outputPlugin,
                                                                                       T t, EventModel event) {
            return main.getOutputManager().generateAllOutputExtensions(outputPlugin, t, event);
        }

        /**
         * returns the ID of the Manager
         */
        @Override
        public Identification getManagerIdentification() {
            Optional<Identification> identification = IdentificationManagerImpl.getInstance()
                    .getIdentification(main.getOutputManager());
            if (!identification.isPresent()) {
                //should not happen
                throw new RuntimeException("unable to obtain ID for OutputManager");
            }
            return identification.get();
        }
    }

    private class SystemImpl implements System {
        /**
         * this method registers an listener which will be fired when all the addons finished registering.
         *
         * @param runnable the runnable to register.
         */
        @Override
        public void registerInitializedListener(Runnable runnable) {
            main.getAddOnManager().addInitializedListener(runnable);
        }
    }
}
