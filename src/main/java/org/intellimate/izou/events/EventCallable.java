package org.intellimate.izou.events;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * Interface to fire events
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
public interface EventCallable {
    /**
     * This method is used to fire the event.
     * @param event the Event which should be fired
     * @throws MultipleEventsException IF the implementation doesn't allow multiple Events at once
     */
    void fire(EventModel event) throws MultipleEventsException;
}
