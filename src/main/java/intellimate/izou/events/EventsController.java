package intellimate.izou.events;

import intellimate.izou.identification.Identifiable;

/**
 * This class can control the whether Events-Dispatcher  get Fired or not.
 * It is expected, that the method-implementation gets executed quickly.
 */
public interface EventsController extends Identifiable {

    /**
     * Controls whether the fired Event should be dispatched to all the listeners
     *
     * This method should execute quickly
     *
     * @param event the ID of the event
     * @return true if events should be dispatched
     */
    public boolean controlEventDispatcher(Event event);
}
