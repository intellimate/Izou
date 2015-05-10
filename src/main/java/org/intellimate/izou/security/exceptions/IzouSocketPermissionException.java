package org.intellimate.izou.security.exceptions;

/**
 * Created by julianbrendl on 5/9/15.
 */
public class IzouSocketPermissionException extends IzouPermissionException {

    /**
     * Creates a new IzouSocketPermissionException
     *
     * @param message the message to send with the exception
     */
    public IzouSocketPermissionException(String message) {
        super(message);
    }
}
