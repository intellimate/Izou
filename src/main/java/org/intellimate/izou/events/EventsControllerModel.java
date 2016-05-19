package org.intellimate.izou.events;

import org.intellimate.izou.identification.Identifiable;
import ro.fortsoft.pf4j.AddonAccessible;

/**
 * This class can control the whether Events-Dispatcher  get Fired or not.
 * It is expected, that the method-implementation gets executed quickly.
 */
@AddonAccessible
public interface EventsControllerModel extends Identifiable {

    /**
     * Controls whether the fired Event should be dispatched to all the listeners
     *
     * This method should execute quickly
     *
     * @param event the ID of the event
     * @return true if events should be dispatched
     */
    boolean controlEventDispatcher(EventModel event);
}
