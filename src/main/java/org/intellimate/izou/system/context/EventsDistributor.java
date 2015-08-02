package org.intellimate.izou.system.context;

import org.intellimate.izou.events.EventCallable;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.EventsControllerModel;
import org.intellimate.izou.internal.identification.Identification;
import org.intellimate.izou.identification.IllegalIDException;

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
     * @throws IllegalIDException not yet implemented
     */
    Optional<EventCallable> registerEventPublisher(Identification identification) throws IllegalIDException;

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
     * @throws IllegalIDException not yet implemented
     */
    void registerEventsController(EventsControllerModel eventsController) throws IllegalIDException;

    /**
     * Unregisters an EventController
     * <p>
     * Method is thread-safe.
     *
     * @param eventsController the EventController Interface to remove
     */
    void unregisterEventsController(EventsControllerModel eventsController);

    /**
     * fires the event concurrently, this is generally discouraged.
     * <p>
     * This method should not be used for normal Events, for for events which obey the following laws:<br>
     * 1. they are time critical.<br>
     * 2. addons are not expected to react in any way beside a small update<br>
     * 3. they are few.<br>
     * if your event matches the above laws, you may consider firing it concurrently.
     * </p>
     * @param eventModel the EventModel
     */
    void fireEventConcurrently(EventModel<?> eventModel);

    /**
     * returns the ID of the Manager (EventsDistributor)
     * @return an instance of Identification
     */
    Identification getManagerIdentification();
}
