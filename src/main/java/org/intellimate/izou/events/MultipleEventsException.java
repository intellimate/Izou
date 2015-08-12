package org.intellimate.izou.events;

/**
 * Exception thrown if there are multiple Events fired at the same time.
 */
//extends because evil hack for backward-compatibility
@SuppressWarnings({"WeakerAccess", "deprecation"})
public class MultipleEventsException extends Exception {
    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     */
    public MultipleEventsException() {
        super("Multiple Events fired at the same time");
    }
}
