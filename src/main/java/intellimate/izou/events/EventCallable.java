package intellimate.izou.events;

/**
 * Interface to fire events
 * @author LeanderK
 * @version 1.0
 */
public interface EventCallable {
    /**
     * This method is used to fire the event.
     * @param event the Event which should be fired
     * @throws MultipleEventsException IF the implementation doesn't allow multiple Events at once
     */
    public void fire(EventModel event) throws MultipleEventsException;
}
