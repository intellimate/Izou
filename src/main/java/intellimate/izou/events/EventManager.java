package intellimate.izou.events;

import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceManager;
import intellimate.izou.system.Identification;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

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
    //ThreadPool where all the Listeners are stored
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final OutputManager outputManager;
    private final ResourceManager resourceManager;

    public EventManager(OutputManager outputManager, ResourceManager resourceManager) {
        this.outputManager = outputManager;
        this.resourceManager = resourceManager;
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
     * @param event the fired Event
     */
    private void fireEvent(Event event) throws InterruptedException {
        if(!checkEventsControllers(event)) return;

        List<EventListener> listenersTemp = event.getDescriptors().parallelStream()
                .map(listeners::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        List<CompletableFuture> futures = listenersTemp.stream()
                .map(eventListener -> CompletableFuture.runAsync(() -> eventListener.eventFired(event), executor))
                .collect(Collectors.toList());

        timeOut(futures);

        List<Resource> resources = resourceManager.generateResources(event);
        event.addResources(resources);

        outputManager.passDataToOutputPlugins(event);
    }

    /**
     * creates a 1 sec. timeout for the resource-generation
     * @param futures a List of futures running
     * @return list with all elements removed, who aren't finished after 1 sec
     */
    public List<CompletableFuture> timeOut(List<CompletableFuture> futures) {
        //Timeout
        int start = 0;
        boolean notFinished = true;
        while ( (start < 100) && notFinished) {
            notFinished = futures.stream()
                    .anyMatch(future -> !future.isDone());
            start++;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                //TODO: log
            }
        }
        //cancel all running tasks
        if(notFinished) {
            futures.stream()
                    .filter(future -> !future.isDone())
                    .forEach(future -> future.cancel(true));
        }
        return futures.stream()
                .filter(Future::isDone)
                .collect(Collectors.toList());
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
