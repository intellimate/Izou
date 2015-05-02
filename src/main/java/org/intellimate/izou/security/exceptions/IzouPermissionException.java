package org.intellimate.izou.security.exceptions;

/**
 * The IzouPermissionException indicates that a certain permission has not been granted. Unlike the
 * {@link SecurityException} in Izou which indicates that the specific service will never be available under any
 * condition, the requested service can be available at times.
 * <p>
 *     For instance, an addOn might attempt to play a sound, and
 *     instead the IzouPermissionException is thrown. This means that currently the addOn does not have permission to
 *     play a sound, probably because another sound is already being played. However once that other sound is done
 *     playing, the original addOn is fully entitled to play its own sound.
 * </p>
 * <p>
 *     So the IzouPermissionException is a temporary denial of service so to say, and not a permanent one, which the
 *     SecurityException is.
 * </p>
 */
public class IzouPermissionException extends Exception {

    /**
     * Creates a new IzouPermissionException
     *
     * @param message the message to send with the exception
     */
    public IzouPermissionException(String message) {
        super(message);
    }
}
