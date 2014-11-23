package intellimate.izou.events;

import intellimate.izou.output.OutputManager;
import intellimate.izou.system.Identification;

import java.util.ArrayList;
import java.util.Optional;
import java.util.WeakHashMap;
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

    //here are all the Listeners stored
    private final ConcurrentHashMap<String, ArrayList<EventListener>> listeners = new ConcurrentHashMap<>();
    //here are all the Instances which fire events stored
    private final ConcurrentHashMap<Identification, EventCaller> callers = new ConcurrentHashMap<>();
    //here are all the Instances to to control the Event-dispatching stored
    private final ConcurrentLinkedQueue<EventsController> eventsControllers = new ConcurrentLinkedQueue<>();
    //the queue where all the Events are stored
    private final BlockingQueue<Event> events = new LinkedBlockingQueue<>(1);
    //if false, run() will stop
    private boolean stop = false;
    private final OutputManager outputManager;

    public EventManager(OutputManager outputManager) {
        this.outputManager = outputManager;
    }

    /**
     * Registers with the EventManager to fire an event.
     *
     * Note: the same Event can be fired from multiple sources.
     * Method is thread-safe.
     *
     * @param identification the Identification of the the instance
     * @return an Optional, empty if already registered
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public Optional<EventCaller> registerCaller(Identification identification) throws IllegalArgumentException{
        if(identification == null ||
            callers.containsKey(identification)) return Optional.empty();
        EventCaller eventCaller = new EventCaller();
        callers.put(identification, eventCaller);
        return Optional.of(eventCaller);
    }

    /**
     * Unregister with the EventManager.
     *
     * Method is thread-safe.
     *
     * @param identification the Identification of the the instance
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void unregisterCaller(Identification identification) {
        if(!callers.containsKey(identification)) return;
        callers.remove(identification);
    }

    /**
     * Adds an listener for events.
     *
     * It will register for all Descriptors individually!
     * It will also ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     *
     * @param event the Event to listen to
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void registerEventListener(Event event, EventListener eventListener) {
        for(String id : event.getDescriptors()) {
            ArrayList<EventListener> listenersList = listeners.get(id);
            if (listenersList == null) {
                listeners.put(id, new ArrayList<>());
                listenersList = listeners.get(id);
            }
            if (!listenersList.contains(eventListener)) {
                synchronized (listenersList) {
                    listenersList.add(eventListener);
                }
            }
        }
    }

    /**
     * unregister an EventListener
     *
     * It will unregister for all Descriptors individually!
     * It will also ignore if this listener is not listening to an Event.
     * Method is thread-safe.
     *
     * @param event the Event to stop listen to
     * @param eventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void unregisterEventListener(Event event, EventListener eventListener) throws IllegalArgumentException{
        for (String id : event.getDescriptors()) {
            ArrayList<EventListener> listenersList = listeners.get(id);
            if (listenersList == null) {
                return;
            }
            synchronized (listenersList) {
                listenersList.remove(eventListener);
            }
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
    public void addEventsController(EventsController controller) throws IllegalArgumentException{
        eventsControllers.add(controller);
    }

    /**
     * Removes an EventController
     *
     * Method is thread-safe.
     *
     * @param controller the EventController Interface to remove
     */
    public void removeEventsController(EventsController controller) throws IllegalArgumentException{
        eventsControllers.remove(controller);
    }

    /**
     * Checks whether to dispatch an event
     *
     * @param event the fired Event
     * @return true if the event should be fired
     */
    private boolean checkEventsControllers(Event event) {
        boolean shouldExecute = true;
        for (EventsController controller : eventsControllers) {
            if (!controller.controlEventDispatcher(event)) {
                shouldExecute = false;
                break;
            }
        }
        return shouldExecute;
    }

    /**
     * this method actually used to fire an event.
     *
     * @param id the ID of the Event, format: package.class.name
     */
    private void fireEvent(Event event) throws InterruptedException {
        checkID(id);
        if(!checkEventsControllers(id)) return;
        //registered to Event
        ArrayList<EventListener> contentGeneratorListeners = this.listeners.get(id);
        if(contentGeneratorListeners == null) contentGeneratorListeners = new ArrayList<>();
        List<Future<ContentData>> futures = new ArrayList<>();
        for (EventListener next : contentGeneratorListeners) {
            Future<ContentData> futureTemp = next.eventFired(id);
            if (futureTemp != null) {
                futures.add(futureTemp);
            }
        }
        //registered to all Events
        contentGeneratorListeners = this.listeners.get(SUBSCRIBE_TO_ALL_EVENTS);
        if(contentGeneratorListeners == null) contentGeneratorListeners = new ArrayList<>();
        for (EventListener next : contentGeneratorListeners) {
            Future<ContentData> futureTemp = next.eventFired(id);
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
                        //TODO: implement Error logging
                        e.printStackTrace();
                    }
                }
            }
        }
        outputManager.passDataToOutputPlugins(data);
    }

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            try {
                fireEvent(events.take());
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
    public BlockingQueue<Event> getEvents() {
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
     * Class used to fire events.
     *
     * To fire events a class must register with registerCaller, then this class will be returned.
     * Use fire() to fire the event;
     */
    @SuppressWarnings("SameParameterValue")
    public final class EventCaller {
        //private, so that this class can only constructed by EventManager
        private EventCaller() {}

        /**
         * This method is used to fire the event.
         *
         * @throws MultipleEventsException an Exception will be thrown if there are currently other events fired
         */
        public void fire(Event event) throws MultipleEventsException {
            if(events.isEmpty()) {
                events.add(event);
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
