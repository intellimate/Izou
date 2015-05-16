package org.intellimate.izou.security.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.fortsoft.pf4j.PluginDescriptor;

import java.util.HashMap;

/**
 * The SecureStorage class offers a way for addOns to store data so that other addOns cannot access it. For example if
 * addOn A wants to store the users username and password to some service, it can do so using this class without any
 * other addOn having access to that information.
 * <p>
 *     While the stored information is encrypted, it is not safe from the user. In theory and with a lot of effort, the
 *     user could extract the keys and decrypt the stored information since the keys are not hidden from the user (only
 *     from addOns).
 * </p>
 */
public final class SecureStorage {
    private static boolean exists = false;
    private HashMap<String, SecureContainer> containers;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Creates an SecureStorage. There can only be one single SecureStorage, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return a SecureAccess object
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public static SecureStorage createSecureStorage() throws IllegalAccessException {
        if (!exists) {
            SecureStorage secureStorage = new SecureStorage();
            exists = true;
            return secureStorage;
        }

        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    /**
     * Creates a new SecureStorage instance if and only if none has been created yet
     *
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    private SecureStorage() throws IllegalAccessException {
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
        }

        containers = new HashMap<>();
    }

    /**
     * Stores a {@link SecureContainer} with the given secure ID of the plugin descriptor. Each addOn can only have 1
     * secure container, so in order to update it, retrieve it and store it again.
     *
     * @param descriptor The plugin descriptor belonging to an addOn
     * @param container The secure container to be stored with an addOn
     */
    public void store(PluginDescriptor descriptor, SecureContainer container) {
        containers.put(descriptor.getSecureID(), container);
    }

    /**
     * Retrieves a {@link SecureContainer} with the given secure ID of the plugin descriptor
     *
     * @param descriptor The plugin descriptor belonging to an addOn
     * @return  container The secure container that was retrieved
     */
    public SecureContainer retrieve(PluginDescriptor descriptor) {
        return containers.get(descriptor.getSecureID());
    }
}
