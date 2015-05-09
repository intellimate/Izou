package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

/**
 * SecureAccess allows the {@link SecurityManager} to access anything with full permission. If the secure access
 * class is found in the current class context, any action will be allowed. However, only the security manager has
 * access to this class
 */
final class SecureAccess {
    private static boolean exists = false;
    private final SecurityBreachHandler breachHandler;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Creates an SecureAccess. There can only be one single SecureAccess, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return an IzouSecurityManager
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    static SecureAccess createSecureAccess() throws IllegalAccessException {
        if (!exists) {
            SecureAccess secureAccess = new SecureAccess();
            exists = true;
            return secureAccess;
        }

        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    /**
     * Creates a new SecureAccess instance if and only if none has been created yet
     *
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    private SecureAccess() throws IllegalAccessException {
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
        }

        SecurityBreachHandler tempBreachHandler = null;
        try {
            tempBreachHandler = SecurityBreachHandler.createBreachHandler("intellimate.izou@gmail.com");
        } catch (IllegalAccessException e) {
            logger.fatal("Unable to create a SecurityBreachHandler because Izou might be under attack. "
                    + "Exiting now.", e);
            exitIzou();
        }
        breachHandler = tempBreachHandler;
    }

    /**
     * Gets the SecurityBreachHandler
     *
     * @return the SecurityBreachHandler
     */
    SecurityBreachHandler getBreachHandler() {
        return breachHandler;
    }

    /**
     * Securely exits Izou
     */
    void exitIzou() {
        System.exit(1);
    }

    /**
     * Securely checks if the given string {@code dir} is a directory, then returns true if so, else false
     *
     * @param dir the string to check for a directory
     * @return true if the given string {@code dir} is a directory, else false
     */
    boolean checkForDirectory(String dir) {
        return new File(dir).isDirectory();
    }

    /**
     * Securely checks if the given string {@code dir} is exists, then returns true if so, else false
     *
     * @param dir the string to check for existence
     * @return true if the given string {@code dir} exists, else false
     */
    boolean checkForExistingFileOrDirectory(String dir) {
        return new File(dir).exists();
    }
}
