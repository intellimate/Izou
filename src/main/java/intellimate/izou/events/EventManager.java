package intellimate.izou.events;

import intellimate.izou.contentgenerator.ContentData;
import intellimate.izou.output.OutputManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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
    public static final String FULL_WELCOME_EVENT = EventManager.class.getCanonicalName() + ".FullWelcomeEvent";
    /**
     * Event for a Welcome with major response.
     *
     * Every component that is import should contribute to this Event.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final String MAJOR_WELCOME_EVENT = EventManager.class.getCanonicalName() + ".MajorWelcomeEvent";
    /**
     * Event for a Welcome with major response.
     *
     * Only components that have information of great importance should contribute to this event.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final String MINOR_WELCOME_EVENT = EventManager.class.getCanonicalName() + ".MinorWelcomeEvent";
    /**
     * Event for a Welcome with major response.
     *
     * Only components that have information of great importance should contribute to this event.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final String SUBSCRIBE_TO_ALL_EVENTS = EventManager.class.getCanonicalName() + ".SubscribeToAllEvents";

    //here are all the ContentGenerators-Listeners stored
    private final ConcurrentHashMap<String, ArrayList<ActivatorEventListener>> listeners = new ConcurrentHashMap<>();
    //here are all the Instances to fire events stored
    private final ConcurrentHashMap<String, ArrayList<ActivatorEventCaller>> callers = new ConcurrentHashMap<>();
    //here are all the Instances to to control the Event-dispatching stored
    private final ConcurrentLinkedQueue<EventController> eventControllers = new ConcurrentLinkedQueue<>();
    //the queue where all the Events are stored (if there are more than one,
    private final BlockingQueue<String> events = new LinkedBlockingQueue<>(1);
    //if false, run() will stop
    private boolean stop = false;
    private final OutputManager outputManager;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    public EventManager(OutputManager outputManager) {
        this.outputManager = outputManager;
    }

    /**
     * Registers with the EventManager to fire an event.
     *
     * Multiple activators can register the same event and fire the same event.
     * Method is thread-safe.
     *
     * @param id the ID of the Event, format: package.class.name
     * @throws IllegalArgumentException when id is null or empty, or the
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
     * Unregister with the EventManager.
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
     * Adds an EventController to control EventDispatching-Behaviour
     *
     * Method is thread-safe.
     * It is expected that this method executes quickly.
     *
     * @param controller the EventController Interface to control event-dispatching
     */
    public void addEventController (EventController controller) throws IllegalArgumentException{
        eventControllers.add(controller);
    }

    /**
     * Removes an EventController
     *
     * Method is thread-safe.
     *
     * @param controller the EventController Interface to remove
     */
    public void removeEventController (EventController controller) throws IllegalArgumentException{
        eventControllers.remove(controller);
    }

    /**
     * Checks whether to dispatch an event
     *
     * @param id the ID of the Event, format: package.class.name
     * @return true if the event should be fired
     */
    private boolean checkEventControllers(String id) {
        boolean shouldExecute = true;
        for (EventController controller : eventControllers) {
            if (!controller.controlEventDispatcher(id)) {
                shouldExecute = false;
            }
        }
        return shouldExecute;
    }

    /**
     * this method actually used to fire an event.
     *
     * @param id the ID of the Event, format: package.class.name
     */
    private void fireActivatorEvent (String id) throws InterruptedException {
        checkID(id);
        if(!checkEventControllers(id)) return;
        //registered to Event
        ArrayList<ActivatorEventListener> contentGeneratorListeners = this.listeners.get(id);
        if(contentGeneratorListeners == null) contentGeneratorListeners = new ArrayList<>();
        List<Future<ContentData>> futures = new ArrayList<>();
        for (ActivatorEventListener next : contentGeneratorListeners) {
            Future<ContentData> futureTemp = next.activatorEventFired(id);
            if (futureTemp != null) {
                futures.add(futureTemp);
            }
        }
        //registered to all Events
        contentGeneratorListeners = this.listeners.get(SUBSCRIBE_TO_ALL_EVENTS);
        if(contentGeneratorListeners == null) contentGeneratorListeners = new ArrayList<>();
        for (ActivatorEventListener next : contentGeneratorListeners) {
            Future<ContentData> futureTemp = next.activatorEventFired(id);
            if (futureTemp != null) {
                futures.add(futureTemp);
            }
        }
        if (futures.isEmpty()) {
            return;
        }
        //workaround -> timeout for ALL futures of a little bit less than 1 sec
        boolean change;
        int countLimit = 80;
        int count = 0;
        do {
            change = false;
            for (Future future : futures) {
                if(future.isDone()) change = true;
            }
            if(!change) Thread.sleep(10);
            count++;
        }
        while(!change && (count < countLimit));
        List<ContentData> data = new ArrayList<>();
        if(count <= countLimit)
        {
            for (Future<ContentData> next : futures) {
                if (!next.isDone()) {
                    next.cancel(true);
                } else {
                    try {
                        data.add(next.get(10, TimeUnit.MILLISECONDS));
                    } catch (Exception e) {
                        fileLogger.warn(e.getMessage());
                    }
                }
            }
        }
        outputManager.passDataToOutputPlugins(data);
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
    @SuppressWarnings("SameParameterValue")
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
