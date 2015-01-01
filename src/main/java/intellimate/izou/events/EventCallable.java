package intellimate.izou.events;

/**
 * @author LeanderK
 * @version 1.0
 */
public interface EventCallable {
    /**
     * This method is used to fire the event.
     *
     * @throws MultipleEventsException if the implementation doesn't allow multiple Events at once
     */
    public void fire(Event event) throws MultipleEventsException;
}
