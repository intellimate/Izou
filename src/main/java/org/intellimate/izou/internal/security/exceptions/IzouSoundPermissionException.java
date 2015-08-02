package org.intellimate.izou.internal.security.exceptions;

/**
 * The IzouSoundPermissionException is thrown when sound is attempted to be played without permission. This usually
 * happens when another sound is currently being played to avoid 2.
 */
public class IzouSoundPermissionException extends IzouPermissionException {

    /**
     * Creates a new IzouSoundPermissionException
     *
     * @param message the message to send with the exception
     */
    public IzouSoundPermissionException(String message) {
        super(message);
    }
}
