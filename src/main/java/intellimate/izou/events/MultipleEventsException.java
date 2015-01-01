package intellimate.izou.events;

/**
 * Exception thrown if there are multiple Events fired at the same time.
 */
@SuppressWarnings("WeakerAccess")
public class MultipleEventsException extends Exception {
    public MultipleEventsException() {
        super("Multiple Events fired at the same time");
    }
}
