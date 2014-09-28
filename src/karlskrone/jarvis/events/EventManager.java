package karlskrone.jarvis.events;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class is used to manage events.
 * Activators can register with an id to fire events und ContentGenerators can subscribe to them.
 *
 * Event-IDs are used in the following form: package.class.nameOfTheEvent
 */
public class EventManager {

    //here are all the ContentGenerators-Listeners stored
    private final HashMap<String, ArrayList<ActivatorEventListener>> listeners = new HashMap<>();
    //here are all the Instances to fire events stored
    private final HashMap<String, ArrayList<ActivatorEventCaller>> callers = new HashMap<>();

    /**
     * Registers with the EventManager to fire an event.
     *
     * Multiple activators can register the same event and fire the same event.
     *
     * @param id the ID of the Event, format: package.class.name
     * @throws IllegalArgumentException when id is null or empty
     * @return returns an ActivatorEventCaller, ActivatorEventCaller.fire() will fire an event
     */
    public ActivatorEventCaller registerActivatorEvent(String id) throws IllegalArgumentException{
        if (id == null || id.isEmpty()) {
           throw new IllegalArgumentException();
        }
        ActivatorEventCaller activatorEventCaller = new ActivatorEventCaller(id);
        ArrayList<ActivatorEventCaller> callers = this.callers.get(id);
        if (callers == null) {
            this.callers.put(id, new ArrayList<>());
            callers = this.callers.get(id);
        }
        callers.add(activatorEventCaller);
        return activatorEventCaller;
    }

    /**
     * Unregisters with the EventManager.
     *
     * @param id the ID of the Event, format: package.class.name
     * @param call the ActivatorEventCaller-instance which was used by the class to fire the event
     */
    public void unregisterActivatorEvent(String id, ActivatorEventCaller call) {
        call.setId("-1");
        ArrayList<ActivatorEventCaller> callers = this.callers.get(id);
        if (callers == null) {
            return;
        }
        callers.remove(call);
    }

    /**
     * Adds an listener for events from the activators.
     *
     * @param id the ID of the Event, format: package.class.name
     * @param activatorEventListener the ActivatorEventListener-interface for receiving activator events
     */
    public void addActivatorEventListener (String id, ActivatorEventListener activatorEventListener) {
        ArrayList<ActivatorEventListener> listenersList = listeners.get(id);
        if (listenersList == null) {
            listeners.put(id, new ArrayList<>());
            listenersList = listeners.get(id);
        }
        listenersList.add(activatorEventListener);
    }

    /**
     * Removes an listener for events from the activators
     *
     * @param id the ID of the Event, format: package.class.name
     * @param activatorEventListener the ActivatorEventListener used to listen for events
     */
    public void deleteActivatorEventListener (String id, ActivatorEventListener activatorEventListener) {
        ArrayList<ActivatorEventListener> listenersList = listeners.get(id);
        if (listenersList == null) {
            return;
        }
        listenersList.remove(activatorEventListener);
    }

    /**
     * this method actually used to fire an event.
     *
     * @param id the ID of the Event, format: package.class.name
     * @throws MultipleEventsException an Exception will be thrown if there are currently other events fired
     */
    @SuppressWarnings("RedundantThrows")
    private void fireActivatorEvent(String id) throws MultipleEventsException{
        ArrayList<ActivatorEventListener> contentGeneratorListeners = this.listeners.get(id);
        //TODO: Protect EventManager from multiple events fired simultaneously
        if (contentGeneratorListeners == null) {
            return;
        }
        for (ActivatorEventListener next : contentGeneratorListeners) {
            next.activatorEventFired(id);
        }

    }

    /**
     * The listener interface for receiving activator events.
     *
     * To receive events a class must implements this interface and register with the addActivatorEventListener-method.
     * When the activator event occurs, that object's activatorEventFired method is invoked.
     */
    public interface ActivatorEventListener {

        /**
         * Invoked when an activator-event occurs.
         * @param id the ID of the Event, format: package.class.name
         */
        public void activatorEventFired(String id);
    }

    /**
     * The class for firing events.
     *
     * To fire events a class must register with registerActivatorEvent, then this class will be returned.
     * Use fire() to fire the event;
     */
    public final class ActivatorEventCaller {
        private String id;
        //private, so that this class can only constructed by EventManager
        private ActivatorEventCaller(String id) {
            this.id = id;
        }

        //private, so that this class can only called by EventManager
        private void setId (String id) {
            this.id = id;
        }

        /**
         * This method is used to fire the event.
         *
         * @throws MultipleEventsException an Exception will be thrown if there are currently other events fired
         */
        public void fire() throws MultipleEventsException {
            fireActivatorEvent(id);
        }
    }

    /**
     * The class that holds all the Event-IDs for the common Events.
     */
    public static class CommonEvents {
        public static final String fullWelcomeEvent = CommonEvents.class.getCanonicalName() + ".FullWelcomeEvent";
        public static final String majorWelcomeEvent = CommonEvents.class.getCanonicalName() + ".MajorWelcomeEvent";
        public static final String minorWelcomeEvent = CommonEvents.class.getCanonicalName() + ".MinorWelcomeEvent";
    }

    /**
     * Thrown if there are multiple Events fired at the same time
     */
    public class MultipleEventsException extends Exception {
    }
}
