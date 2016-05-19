package org.intellimate.izou.system.context;

import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.EventCallable;
import org.intellimate.izou.events.EventListenerModel;
import org.intellimate.izou.events.MultipleEventsException;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IllegalIDException;
import ro.fortsoft.pf4j.AddonAccessible;

import java.util.List;
import java.util.Optional;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface Events {
    /**
     * Adds an listener for events that gets called before the generation of the resources and the outputPlugins..
     * <p>
     * Be careful with this method, it will register the listener for ALL the informations found in the Event. If your
     * event-type is a common event type, it will fire EACH time!.
     * It will also register for all Descriptors individually!
     * It will also ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     * </p>
     * @param event the Event to listen to (it will listen to all descriptors individually!)
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     * @throws IllegalIDException not yet implemented
     */
    void registerEventListener(EventModel event, EventListenerModel eventListener) throws IllegalIDException;

    /**
     * Adds an listener for events that gets called before the generation of the resources and the outputPlugins..
     * <p>
     * It will register for all ids individually!
     * This method will ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     * </p>
     * @param ids this can be type, or descriptors etc.
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     */
    void registerEventListener(List<String> ids, EventListenerModel eventListener);

    /**
     * unregister an EventListener that gets called before the generation of the resources and the outputPlugins.
     *<p>
     * It will unregister for all Descriptors individually!
     * It will also ignore if this listener is not listening to an Event.
     * Method is thread-safe.
     *
     * @param event the Event to stop listen to
     * @param eventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    void unregisterEventListener(EventModel event, EventListenerModel eventListener);

    /**
     * unregister an EventListener that gets called before the generation of the resources and the outputPlugins.
     *<p>
     * It will unregister for all Descriptors individually!
     * It will also ignore if this listener is not listening to an Event.
     * Method is thread-safe.
     *
     * @param eventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    void unregisterEventListener(EventListenerModel eventListener);

    /**
     * Adds an listener for events that gets called when the event finished processing.
     * <p>
     * Be careful with this method, it will register the listener for ALL the informations found in the Event. If your
     * event-type is a common event type, it will fire EACH time!.
     * It will also register for all Descriptors individually!
     * It will also ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     * </p>
     * @param event the Event to listen to (it will listen to all descriptors individually!)
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     * @throws IllegalIDException not yet implemented
     */
    void registerEventFinishedListener(EventModel event, EventListenerModel eventListener) throws IllegalIDException;

    /**
     * Adds an listener for events that gets called when the event finished processing.
     * <p>
     * It will register for all ids individually!
     * This method will ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     * </p>
     * @param ids this can be type, or descriptors etc.
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     */
    void registerEventFinishedListener(List<String> ids, EventListenerModel eventListener);

    /**
     * unregister an EventListener that got called when the event finished processing.
     *<p>
     * It will unregister for all Descriptors individually!
     * It will also ignore if this listener is not listening to an Event.
     * Method is thread-safe.
     *
     * @param event the Event to stop listen to
     * @param eventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    void unregisterEventFinishedListener(EventModel event, EventListenerModel eventListener);

    /**
     * unregister an EventListener that got called when the event finished processing.
     *<p>
     * It will unregister for all Descriptors individually!
     * It will also ignore if this listener is not listening to an Event.
     * Method is thread-safe.
     *
     * @param eventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    void unregisterEventFinishedListener(EventListenerModel eventListener);

    /**
     * Registers with the LocalEventManager to fire an event.
     * <p>
     * Note: the same Event can be fired from multiple sources.
     * Method is thread-safe.
     * @param identification the Identification of the the instance
     * @return an Optional, empty if already registered
     * @throws IllegalIDException not yet implemented
     */
    Optional<EventCallable> registerEventCaller(Identification identification) throws IllegalIDException;

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
     * @throws IllegalIDException not yet implemented
     * @throws MultipleEventsException if there is currently another Event processing
     */
    void fireEvent(EventModel event) throws IllegalIDException, MultipleEventsException;

    /**
     * returns the API for the EventsDistributor
     * @return Distributor
     */
    public EventsDistributor distributor();

    /**
     * returns the ID of the Manager (LocalEventManager)
     * @return an instance of Identification
     */
    Identification getManagerIdentification();
}
