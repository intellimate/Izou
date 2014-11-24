package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.Event;
import intellimate.izou.events.EventListener;
import intellimate.izou.events.EventManager;
import intellimate.izou.main.Main;
import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceBuilder;

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

    public Context(AddOn addOn, Main main) {
        this.addOn = addOn;
        this.main = main;
    }

    private class Events {
        /**
         * Adds an listener for events.
         * <p>
         * It will register for all Descriptors individually!
         * It will also ignore if this listener is already listening to an Event. Method is thread-safe.
         * @param event the Event to listen to (it will listen to all descriptors individually!)
         * @param eventListener the ActivatorEventListener-interface for receiving activator events
         */
        public void registerEventListener(Event event, EventListener eventListener) {
            main.getEventManager().registerEventListener(event, eventListener);
        }

        /**
         * unregister an EventListener.
         * <p>
         *  It will unregister for all Descriptors individually!
         *  It will also ignore if this listener is not listening to an Event.
         *  Method is thread-safe.
         * @param event the Event to stop listen to
         * @param eventListener the ActivatorEventListener used to listen for events
         */
        public void unregisterEventListener(Event event, EventListener eventListener) {
            main.getEventManager().unregisterEventListener(event, eventListener);
        }

        /**
         * Registers with the EventManager to fire an event.
         * <p>
         * Note: the same Event can be fired from multiple sources.
         * Method is thread-safe.
         * @param identification the Identification of the the instance
         * @return an Optional, empty if already registered
         */
        public Optional<EventManager.EventCaller> registerEventCaller(Identification identification) {
            return main.getEventManager().registerCaller(identification);
        }

        /**
         * Unregister with the EventManager.
         * <p>
         * Method is thread-safe.
         * @param identification the Identification of the the instance
         */
        public void unregisterEventCaller(Identification identification) {
            main.getEventManager().unregisterCaller(identification);
        }
    }

    private class Resources {
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
