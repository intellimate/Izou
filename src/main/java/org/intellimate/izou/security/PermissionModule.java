package org.intellimate.izou.security;

import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.security.exceptions.IzouPermissionException;

/**
 * A PermissionModule defines basic permissions in Izou. A permission in Izou is defined as service that is generally
 * allowed, yet the addOn has to be registered in order to use the service.
 * <p>
 *     For example, socket connections are generally allowed for Izou addOns yet in order to use them their usage has
 *     to be declared in the addon_config.properties file of the addOn.
 * </p>
 * <p>
 *     Thus {@code PermissionModule} implements a registration system for services like socket connection.
 * </p>
 */
public abstract class PermissionModule extends SecurityModule {
    private final IdentifiableSet<Identifiable> registeredAddOns;

    /**
     * Creates a new PermissionModule
     */
    PermissionModule() {
        registeredAddOns = new IdentifiableSet<>();
    }

    /**
     * Adds an addOn to the registered addOns list for this PermissionModule
     *
     * @param addon the Identifiable to add
     */
    public void registerAddOn(Identifiable addon) {
        registeredAddOns.add(addon);
    }

    /**
     * Checks if an addOn is registered with this PermissionModule
     *
     * @param addon the identifiable to check
     * @return true if the addOn is registered, else false
     */
    public boolean isRegistered(Identifiable addon) {
        return registeredAddOns.contains(addon);
    }

    /**
     * Checks if the given addOn is allowed to access the requested service
     *
     * @param addon the identifiable to check
     * @throws IzouPermissionException thrown if the addOn is not allowed to access its requested service
     * @return true if the addOn gets the permission granted, else false
     */
    public abstract boolean checkPermission(Identifiable addon) throws IzouPermissionException;
}
