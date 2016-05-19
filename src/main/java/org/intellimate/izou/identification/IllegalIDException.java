package org.intellimate.izou.identification;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * This Exception gets fired when an Identification or Identifiable doesn't fulfil certain requirements
 * (e.g. used twice)
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
//This Exception is more of a placeholder, but in future version we will certainly check if an ID is valid
public class IllegalIDException extends RuntimeException {
    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public IllegalIDException() {
        super("The Identification does not fulfil the requirements");
    }

    /**
     * Constructs a new exception with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public IllegalIDException(String message) {
        super(message);
    }
}
