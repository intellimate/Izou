package intellimate.izou.system.context;

import intellimate.izou.events.EventCallable;
import intellimate.izou.events.EventsController;
import intellimate.izou.identification.Identification;

import java.util.Optional;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface EventsDistributor {
    /**
     * with this method you can register EventPublisher add a Source of Events to the System.
     * <p>
     * This method represents a higher level of abstraction! Use the EventManager to fire Events!
     * This method is intended for use cases where you have an entire new source of events (e.g. network)
     * @param identification the Identification of the Source
     * @return An Optional Object which may or may not contains an EventPublisher
     */
    Optional<EventCallable> registerEventPublisher(Identification identification);

    /**
     * with this method you can unregister EventPublisher add a Source of Events to the System.
     * <p>
     * This method represents a higher level of abstraction! Use the EventManager to fire Events!
     * This method is intended for use cases where you have an entire new source of events (e.g. network)
     * @param identification the Identification of the Source
     */
    void unregisterEventPublisher(Identification identification);

    /**
     * Registers an EventController to control EventDispatching-Behaviour
     * <p>
     * Method is thread-safe.
     * It is expected that this method executes quickly.
     *
     * @param eventsController the EventController Interface to control event-dispatching
     */
    void registerEventsController(EventsController eventsController);

    /**
     * Unregisters an EventController
     * <p>
     * Method is thread-safe.
     *
     * @param eventsController the EventController Interface to remove
     */
    void unregisterEventsController(EventsController eventsController);
}