package org.intellimate.izou.security;

import org.intellimate.izou.security.exceptions.IzouPermissionException;

import java.util.ArrayList;
import java.util.List;

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
    private final List<String> registeredAddOns;

    /**
     * Creates a new PermissionModule
     */
    PermissionModule() {
        registeredAddOns = new ArrayList<>();
    }

    /**
     * Adds an addOn to the registered addOns list for this PermissionModule
     *
     * @param addOnID the ID of the addOn to add
     */
    public void registerAddOn(String addOnID) {
        registeredAddOns.add(addOnID);
    }

    /**
     * Checks if an addOn is registered with this PermissionModule
     *
     * @param addOnID the id of the addOn to check
     * @return true if the addOn is registered, else false
     */
    public boolean isRegistered(String addOnID) {
        return registeredAddOns.contains(addOnID);
    }

    /**
     * Checks if the given addOn is allowed to access the requested service
     *
     * @param addOnID the ID of the addOn to check for
     * @throws IzouPermissionException thrown if the addOn is not allowed to access its requested service
     * @return true if the addOn gets the permission granted, else false
     */
    public abstract boolean checkPermission(String addOnID) throws IzouPermissionException;
}
