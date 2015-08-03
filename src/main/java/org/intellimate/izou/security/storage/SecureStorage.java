package org.intellimate.izou.security.storage;

import ro.fortsoft.pf4j.PluginDescriptor;

/**
 * @author LeanderK
 * @version 1.0
 */
public interface SecureStorage {
    /**
     * Stores a {@link SecureContainer} with the given secure ID of the plugin descriptor. Each addOn can only have 1
     * secure container, so in order to update it, retrieve it and store it again.
     *
     * @param descriptor The plugin descriptor belonging to an addOn
     * @param container The secure container to be stored with an addOn
     */
    void store(PluginDescriptor descriptor, SecureContainer container);

    /**
     * Retrieves a {@link SecureContainer} with the given secure ID of the plugin descriptor
     *
     * @param descriptor The plugin descriptor belonging to an addOn
     * @return  container The secure container that was retrieved
     */
    SecureContainer retrieve(PluginDescriptor descriptor);
}
