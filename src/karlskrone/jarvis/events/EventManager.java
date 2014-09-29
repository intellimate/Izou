package karlskrone.jarvis.events;

import karlskrone.jarvis.contentgenerator.ContentData;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is used to manage events.
 * Activators can register with an id to fire events und ContentGenerators can subscribe to them.
 *
 * Event-IDs are used in the following form: package.class.name
 */
public class EventManager implements Runnable{
    //common Events:
    /**
     * Event for a Welcome with maximum response.
     *
     * Every component that can contribute should contribute to this Event.
     */
    public static final String fullWelcomeEvent = EventManager.class.getCanonicalName() + ".FullWelcomeEvent";
    /**
     * Event for a Welcome with major response.
     *
     * Every component that is import should contribute to this Event.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final String majorWelcomeEvent = EventManager.class.getCanonicalName() + ".MajorWelcomeEvent";
    /**
     * Event for a Welcome with major response.
     *
     * Only components that have information of great importance should contribute to this event.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final String minorWelcomeEvent = EventManager.class.getCanonicalName() + ".MinorWelcomeEvent";

    //here are all the ContentGenerators-Listeners stored
    private final ConcurrentHashMap<String, ArrayList<ActivatorEventListener>> listeners = new ConcurrentHashMap<>();
    //here are all the Instances to fire events stored
    private final ConcurrentHashMap<String, ArrayList<ActivatorEventCaller>> callers = new ConcurrentHashMap<>();
    //the queue where all the Events are stored (if there are more than one,
    private final BlockingQueue<String> events = new LinkedBlockingQueue<>(1);
    //if false, run() will stop
    private boolean stop = false;

    /**
     * Registers with the EventManager to fire an event.
     *
     * Multiple activators can register the same event and fire the same event.
     * Method is thread-safe.
     *
     * @param id the ID of the Event, format: package.class.name
     * @throws IllegalArgumentException when id is null or empty
     * @return returns an instance of ActivatorEventCaller, ActivatorEventCaller.fire() will fire an event
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public ActivatorEventCaller registerActivatorCaller(String id) throws IllegalArgumentException{
        checkID(id);
        ActivatorEventCaller activatorEventCaller = new ActivatorEventCaller(id);
        ArrayList<ActivatorEventCaller> callers = this.callers.get(id);
        if (callers == null) {
            this.callers.put(id, new ArrayList<>());
            callers = this.callers.get(id);
        }
        synchronized (callers) {
            callers.add(activatorEventCaller);
        }
        return activatorEventCaller;
    }

    /**
     * Unregisters with the EventManager.
     *
     * Method is thread-safe.
     *
     * @param id the ID of the Event, format: package.class.name
     * @param call the ActivatorEventCaller-instance which was used by the class to fire the event
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void unregisterActivatorCaller(String id, ActivatorEventCaller call) {
        checkID(id);
        call.setId("-1");
        ArrayList<ActivatorEventCaller> callers = this.callers.get(id);
        if (callers == null) {
            return;
        }
        synchronized (callers) {
            callers.remove(call);
        }
    }

    /**
     * Adds an listener for events from the activators.
     *
     * Method is thread-safe.
     *
     * @param id the ID of the Event, format: package.class.name
     * @param activatorEventListener the ActivatorEventListener-interface for receiving activator events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void addActivatorEventListener (String id, ActivatorEventListener activatorEventListener) throws IllegalArgumentException{
        checkID(id);
        ArrayList<ActivatorEventListener> listenersList = listeners.get(id);
        if (listenersList == null) {
            listeners.put(id, new ArrayList<>());
            listenersList = listeners.get(id);
        }
        if(listenersList.contains(activatorEventListener)) {
            throw new IllegalArgumentException("Listener already listening to this event");
        }
        synchronized (listenersList) {
            listenersList.add(activatorEventListener);
        }
    }

    /**
     * Removes an listener for events from the activators
     *
     * Method is thread-safe.
     *
     * @param id the ID of the Event, format: package.class.name
     * @param activatorEventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void deleteActivatorEventListener (String id, ActivatorEventListener activatorEventListener) throws IllegalArgumentException{
        checkID(id);
        ArrayList<ActivatorEventListener> listenersList = listeners.get(id);
        if (listenersList == null) {
            return;
        }
        synchronized (listenersList) {
            listenersList.remove(activatorEventListener);
        }
    }

    /**
     * this method actually used to fire an event.
     *
     * @param id the ID of the Event, format: package.class.name
     */
    private void fireActivatorEvent (String id) throws InterruptedException {
        checkID(id);
        ArrayList<ActivatorEventListener> contentGeneratorListeners = this.listeners.get(id);
        if (contentGeneratorListeners == null) {
            return;
        }
        List<Future<ContentData>> futures = new ArrayList<>();
        for (ActivatorEventListener next : contentGeneratorListeners) {
            Future futureTemp = next.activatorEventFired(id);
            if (futureTemp != null) {
                futures.add(futureTemp);
            }
        }
        boolean sleep = false;
        int count = 0;
        do {
            boolean change = false;
            for (Future future : futures) {
                if(!future.isDone()) change = true;
            }
            if(change) Thread.sleep(10);
            sleep = change;
            count++;
        }
        while(sleep && (count < 20));
        if(count >= 20)
        {
            for (Iterator<Future<ContentData>> iterator = futures.iterator(); iterator.hasNext(); ) {
                Future<ContentData> next = iterator.next();
                if(!next.isDone()) {
                    next.cancel(true);
                    iterator.remove();
                }
            }
        }
    }

    /**
     * Checks whether a ID is valid.
     *
     * Currently only throws error if id is null or empty.
     *
     * @param id the ID to check
     * @throws IllegalArgumentException if the id is null or empty
     */
    private void checkID(String id) throws IllegalArgumentException{
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException("Event-ID not Allowed");
        }
    }

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            try {
                fireActivatorEvent(events.take());
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    /**
     * returns the BlockingQueue containing the events.
     *
     * @return an instance of BlockingQueue
     */
    public BlockingQueue<String> getEvents() {
        return events;
    }

    /**
     * Should stop the EventManager.
     *
     * The method run() is a while-loop that repeats itself as long as a variable isn't true. This sets the variable true
     * but does NOT interrupt the execution! If its not processing, it is waiting for an event, so this Thread still may
     * not stop without interruption.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void stop() {
        stop = true;
    }

    /**
     * Interface for listening to events.
     *
     * To receive events a class must implements this interface and register with the addActivatorEventListener-method.
     * When the activator event occurs, that object's activatorEventFired method is invoked.
     */
    public interface ActivatorEventListener {

        /**
         * Invoked when an activator-event occurs.
         *
         * @param id the ID of the Event, format: package.class.name
         * @return a Future representing pending completion of the task
         */
        public Future<ContentData> activatorEventFired(String id);
    }

    /**
     * Class used to fire events.
     *
     * To fire events a class must register with registerActivatorCaller, then this class will be returned.
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
            if(events.isEmpty()) {
                events.add(id);
            }
            else
            {
                throw new MultipleEventsException();
            }
        }
    }

    /**
     * Exception thrown if there are multiple Events fired at the same time.
     */
    @SuppressWarnings("WeakerAccess")
    public class MultipleEventsException extends Exception {
        public MultipleEventsException() {
            super("Multiple Events fired at the same time");
        }
    }
}
