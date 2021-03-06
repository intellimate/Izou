package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.support.SystemMail;
import org.intellimate.izou.util.IzouModule;

import java.io.File;
import java.util.function.Supplier;

/**
 * SecureAccess allows the {@link SecurityManager} to access anything with full permission. If the secure access
 * class is found in the current class context, any action will be allowed. However, only the security manager has
 * access to this class
 */
final class SecureAccess extends IzouModule {
    private static boolean exists = false;
    private final SecurityBreachHandler breachHandler;
    private final SystemMail systemMail;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Creates an SecureAccess. There can only be one single SecureAccess, so calling this method twice
     * will cause an illegal access exception.
     *
     * @param main the main instance of izou
     * @param systemMail the system mail object in order to send e-mails to owner in case of emergency
     * @return a SecureAccess object
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    static SecureAccess createSecureAccess(Main main, SystemMail systemMail) throws IllegalAccessException {
        if (!exists) {
            SecureAccess secureAccess = new SecureAccess(main, systemMail);
            exists = true;
            return secureAccess;
        }

        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    /**
     * Creates a new SecureAccess instance if and only if none has been created yet
     * @param main the main instance of izou
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    private SecureAccess(Main main, SystemMail systemMail) throws IllegalAccessException {
        super(main);
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
        }

        this.systemMail = systemMail;
        SecurityBreachHandler tempBreachHandler = null;
        try {
            tempBreachHandler = SecurityBreachHandler.createBreachHandler(main, systemMail, "intellimate.izou@gmail.com");
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

    /**
     * computes the supplier with an elevated security level (eliminates checks).
     * <p>
     * Be careful not to expose this method to addons or call method from addons inside this supplier as
     * they would also gain the elevated security level.
     * </p>
     * @param supplier the supplier to call
     * @param <T> the Type
     * @return the result
     */
    <T> T doElevated(Supplier<T> supplier) {
        return supplier.get();
    }

    /**
     * runs the runnable with an elevated security level (eliminates checks).
     * @param run the runnable to run.
     */
    void doElevated(Runnable run) {
        run.run();
    }
}
