package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.*;
import intellimate.izou.main.Main;
import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.ExtendedLogger;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class provides much of the general Communication with Izou.
 */

@SuppressWarnings("UnusedDeclaration")
public class Context {
    @SuppressWarnings("FieldCanBeLocal")
    private AddOn addOn;
    private Main main;
    public Events events = new Events();
    public Resources resources = new Resources();
    public FileManager fileManager = new FileManager();
    public Logger logger;

    /**
     * creates a new context for the addOn
     *
     * A context contains all the "global" or generally necessary information an addOn might need that it otherwise does
     * not have access too
     *
     * @param addOn the addOn for which to create a new context
     * @param main instance of main
     */
    public Context(AddOn addOn, Main main, String logLevel) {
        this.addOn = addOn;
        this.main = main;

        IzouLogger izouLogger = main.getIzouLogger();
        ExtendedLogger logger = null;
        if (izouLogger != null)
            this.logger = new Logger(izouLogger.createFileLogger(addOn.getID(), logLevel));
        else {
            this.logger = null;
            org.apache.logging.log4j.Logger fileLogger = LogManager.getLogger(this.getClass());
            fileLogger.error("IzouLogger has not been initialized");
            throw new NullPointerException("IzouLogger has not been initialized");
        }
    }

    /**
     * gets addOn
     *
     * @return the addOn
     */
    public AddOn getAddOn() {
        return addOn;
    }

    public class FileManager {
        /**
         * returns a FileManager-Instance
         * <p>
         * The file manager listens for events that were caused by modifications made to property files and
         * then reloads the file
         * </p>
         * @return an instance of FileManager
         */
        public intellimate.izou.system.FileManager getFileManager() {
            return main.getFileManager();
        }
    }

    public class Logger {
        private final ExtendedLogger logger;

        public Logger(ExtendedLogger logger) {
            this.logger = logger;
        }

        /**
         * gets instance of main
         *
         * @return instance of main
         */
        public Main getMain() {
            return main;
        }

        /**
         * gets logger for addOn
         *
         * @return the logger
         */
        public ExtendedLogger getLogger() {
            return logger;
        }
    }

    public class Events {
        public Distributor distributor = new Distributor();
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
        public Optional<LocalEventManager.EventCaller> registerEventCaller(Identification identification) {
            return main.getLocalEventManager().registerCaller(identification);
        }

        /**
         * Unregister with the LocalEventManager.
         * <p>
         * Method is thread-safe.
         * @param identification the Identification of the the instance
         */
        public void unregisterEventCaller(Identification identification) {
            main.getLocalEventManager().unregisterCaller(identification);
        }

        public class Distributor {
            /**
             * with this method you can register EventPublisher add a Source of Events to the System.
             * <p>
             * This method represents a higher level of abstraction! Use the EventManager to fire Events!
             * This method is intended for use cases where you have an entire new source of events (e.g. network)
             * @param identification the Identification of the Source
             * @return An Optional Object which may or may not contains an EventPublisher
             */
            public Optional<EventPublisher> registerEventPublisher(Identification identification) {
                return main.getEventDistributor().registerEventPublisher(identification);
            }

            /**
             * with this method you can unregister EventPublisher add a Source of Events to the System.
             * <p>
             * This method represents a higher level of abstraction! Use the EventManager to fire Events!
             * This method is intended for use cases where you have an entire new source of events (e.g. network)
             * @param identification the Identification of the Source
             */
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
            public void unregisterEventsController(EventsController eventsController) {
                main.getEventDistributor().unregisterEventsController(eventsController);
            }
        }
    }

    public class Resources {
        /**
         * registers a ResourceBuilder.
         * <p>
         * this method registers all the events, resourcesID etc.
         * @param resourceBuilder an instance of the ResourceBuilder
         */
        public void registerResourceBuilder(ResourceBuilder resourceBuilder) {
            main.getResourceManager().registerResourceBuilder(resourceBuilder);
        }

        /**
         * unregister a ResourceBuilder.
         * <p>
         * this method unregisters all the events, resourcesID etc.
         * @param resourceBuilder an instance of the ResourceBuilder
         */
        public void unregisterResourceBuilder(ResourceBuilder resourceBuilder) {
            main.getResourceManager().unregisterResourceBuilder(resourceBuilder);
        }

        /**
         * generates a resources
         * <p>
         * @param resource the resource to request
         * @param consumer the callback when the ResourceBuilder finishes
         */
        public void generateResource(Resource resource, Consumer<List<Resource>> consumer) {
            main.getResourceManager().generatedResource(resource, consumer);
        }
    }
}
