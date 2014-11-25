package intellimate.izou.events;

import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceManager;
import intellimate.izou.system.Identification;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

    public EventDistributor(ResourceManager resourceManager, OutputManager outputManager) {
        this.resourceManager = resourceManager;
        this.outputManager = outputManager;
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
        if(!registered.containsKey(identification)) return Optional.empty();
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
    public void unregisterEventsController(EventsController controller) throws IllegalArgumentException{
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
                if (checkEventsControllers(event)) {
                    List<Resource> resourceList = resourceManager.generateResources(event);
                    event.addResources(resourceList);
                    outputManager.passDataToOutputPlugins(event);
                }
            } catch (InterruptedException e) {
                //Todo print exception
                e.printStackTrace();
            }
        }
    }
}
