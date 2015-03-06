package intellimate.izou.events;

import intellimate.izou.main.Main;
import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceManager;
import intellimate.izou.identification.Identification;
import intellimate.izouSDK.events.EventImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * This class gets all the Events from all registered EventPublisher, generates Resources and passes them to the
 * OutputManager
 */
public class EventDistributor implements Runnable{
    private BlockingQueue<Event> events = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<Identification, EventPublisher> registered = new ConcurrentHashMap<>();
    private final ResourceManager resourceManager;
    private final OutputManager outputManager;
    //here are all the Instances to to control the Event-dispatching stored
    private final ConcurrentLinkedQueue<EventsController> eventsControllers = new ConcurrentLinkedQueue<>();
    //here are all the Listeners stored
    private final ConcurrentHashMap<String, ArrayList<EventListener>> listeners = new ConcurrentHashMap<>();
    //ThreadPool where all the Listeners are executed
    private final ExecutorService executor;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    public EventDistributor(Main main) {
        this.resourceManager = main.getResourceManager();
        this.outputManager = main.getOutputManager();
        executor = main.getThreadPoolManager().getAddOnsThreadPool();
        main.getThreadPoolManager().getIzouThreadPool().submit(this);
    }

    /**
     * with this method you can register EventPublisher add a Source of Events to the System.
     * <p>
     * This method represents a higher level of abstraction! Use the EventManager to fire Events!
     * This method is intended for use cases where you have an entire new source of events (e.g. network)
     * @param identification the Identification of the Source
     * @return An Optional Object which may or may not contains an EventPublisher
     */
    public Optional<EventPublisher> registerEventPublisher(Identification identification) {
        if(registered.containsKey(identification)) return Optional.empty();
        EventPublisher eventPublisher = new EventPublisher(events);
        registered.put(identification, eventPublisher);
        return Optional.of(eventPublisher);
    }

    /**
     * with this method you can unregister EventPublisher add a Source of Events to the System.
     * <p>
     * This method represents a higher level of abstraction! Use the EventManager to fire Events!
     * This method is intended for use cases where you have an entire new source of events (e.g. network)
     * @param identification the Identification of the Source
     */
    public void unregisterEventPublisher(Identification identification) {
        if(!registered.containsKey(identification)) return;
        registered.remove(identification);
    }

    /**
     * Registers an EventController to control EventDispatching-Behaviour
     * <p>
     * Method is thread-safe.
     * It is expected that this method executes quickly.
     *
     * @param controller the EventController Interface to control event-dispatching
     */
    public void registerEventsController(EventsController controller) throws IllegalArgumentException{
        eventsControllers.add(controller);
    }

    /**
     * Unregisters an EventController
     * <p>
     * Method is thread-safe.
     *
     * @param controller the EventController Interface to remove
     */
    public void unregisterEventsController(EventsController controller) {
        eventsControllers.remove(controller);
    }


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
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void registerEventListener(Event event, EventListener eventListener) {
        for(String id : event.getAllIformations()) {
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
     * Adds an listener for events.
     * <p>
     * It will register for all ids individually!
     * This method will ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     * </p>
     * @param ids this can be type, or descriptors etc.
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void registerEventListener(List<String> ids, EventListener eventListener) {
        for(String id : ids) {
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
        for (String id : event.getAllIformations()) {
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
     * Checks whether to dispatch an event
     *
     * @param event the fired Event
     * @return true if the event should be fired
     */
    private boolean checkEventsControllers(Event event) {
        if (event.getID().equals(EventImpl.NOTIFICATION))
            return true;
        boolean shouldExecute = true;
        for (EventsController controller : eventsControllers) {
            if (!controller.controlEventDispatcher(event)) {
                shouldExecute = false;
                break;
            }
        }
        return shouldExecute;
    }

    public BlockingQueue<Event> getEvents() {
        return events;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        boolean stop = false;
        //noinspection ConstantConditions
        while(!stop) {
            try {
                Event event = events.take();
                fileLogger.debug("EventFired: " + event.getID() + " from " + event.getSource().getID());
                if (checkEventsControllers(event)) {
                    List<Resource> resourceList = resourceManager.generateResources(event);
                    event.addResources(resourceList);
                    List<EventListener> listenersTemp = event.getAllIformations().parallelStream()
                            .map(listeners::get)
                            .filter(Objects::nonNull)
                            .flatMap(Collection::stream)
                            .distinct()
                            .collect(Collectors.toList());

                    List<CompletableFuture> futures = listenersTemp.stream()
                            .map(eventListener -> CompletableFuture.runAsync(() -> eventListener.eventFired(event)
                                    , executor))
                            .collect(Collectors.toList());

                    timeOut(futures);
                    outputManager.passDataToOutputPlugins(event);
                }
            } catch (InterruptedException e) {
                fileLogger.warn(e);
            }
        }
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
                fileLogger.warn(e);
            }
        }
        //cancel all running tasks
        if(notFinished) {
            futures.stream()
                    .filter(future -> !future.isDone())
                    .peek(future -> fileLogger.error(future.toString() + " timed out"))
                    .forEach(future -> future.cancel(true));
        }
        return futures.stream()
                .filter(Future::isDone)
                .collect(Collectors.toList());
    }
}
