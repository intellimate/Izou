package intellimate.izou.system.context;

import intellimate.izou.events.Event;
import intellimate.izou.events.EventCallable;
import intellimate.izou.events.EventListener;
import intellimate.izou.events.MultipleEventsException;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IllegalIDException;

import java.util.List;
import java.util.Optional;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface Events {
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
    void registerEventListener(Event event, EventListener eventListener);

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
    void registerEventListener(List<String> ids, EventListener eventListener);

    /**
     * unregister an EventListener
     *<p>
     * It will unregister for all Descriptors individually!
     * It will also ignore if this listener is not listening to an Event.
     * Method is thread-safe.
     *
     * @param event the Event to stop listen to
     * @param eventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    void unregisterEventListener(Event event, EventListener eventListener);

    /**
     * Registers with the LocalEventManager to fire an event.
     * <p>
     * Note: the same Event can be fired from multiple sources.
     * Method is thread-safe.
     * @param identification the Identification of the the instance
     * @return an Optional, empty if already registered
     */
    Optional<EventCallable> registerEventCaller(Identification identification);

    /**
     * Unregister with the LocalEventManager.
     * <p>
     * Method is thread-safe.
     * @param identification the Identification of the the instance
     */
    void unregisterEventCaller(Identification identification);

    /**
     * This method fires an Event
     *
     * @param event the fired Event
     * @throws java.lang.IllegalAccessError not yet implemented
     */
    void fireEvent(Event event) throws IllegalIDException, MultipleEventsException;

    /**
     * returns the API for the EventsDistributor
     * @return Distributor
     */
    public EventsDistributor distributor();
}
