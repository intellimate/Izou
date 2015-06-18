package org.intellimate.izou.events;

/**
 * This enum contains the different stages in the lifecycle of the Event.
 * @author LeanderK
 * @version 1.0
 */
public enum EventLiveCycle {
    /**
     * gets called while the EventDistributor started the processing.
     */
    START,
    /**
     * gets called when the EventsController approved the Event.
     */
    APPROVED,
    /**
     * gets called when the EventsController canceled the Event.
     */
    CANCELED,
    /**
     * gets called while the ResourceManager started generating the Resources.
     */
    RESOURCE,
    /**
     * gets called while the EventListeners started getting notified.
     */
    LISTENERS,
    /**
     * gets called while the OutputManager started the processing.
     */
    OUTPUT,
    /**
     * gets called while the FinishedEventListeners got notified.
     * This lifecycle does not fire when the Event got canceled.
     */
    ENDED
}
