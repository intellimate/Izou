package org.intellimate.izou.events;

/**
 * Interface for listening to events.
 *
 * To receive events a class must implements this interface and register with the registerEventListener-method.
 * When the activator event occurs, that object's eventFired method is invoked.
 */
public interface EventListenerModel {

    /**
     * Invoked when an activator-event occurs.
     *
     * @param event an instance of Event
     */
    public void eventFired(EventModel event);
}
