package org.intellimate.izou.security;

import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;

import java.security.Permission;

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
public abstract class PermissionModule extends IzouModule {
    private final IdentifiableSet<AddOnModel> registeredAddOns;

    /**
     * Creates a new PermissionModule
     */
    PermissionModule(Main main) {
        super(main);
        registeredAddOns = new IdentifiableSet<>();
    }

    /**
     * returns true if able to check permissions
     * @param permission the permission to check
     * @return true if able to, false if not
     */
    public abstract boolean canCheckPermission(Permission permission);

    /**
     * Adds an addOn to the registered addOns list for this PermissionModule
     *
     * @param addon the Identifiable to add
     */
    public void registerAddOn(AddOnModel addon) {
        registeredAddOns.add(addon);
    }

    /**
     * Checks if an addOn is registered with this PermissionModule
     *
     * @param addon the identifiable to check
     * @return true if the addOn is registered, else false
     */
    public boolean isRegistered(AddOnModel addon) {
        return registeredAddOns.contains(addon);
    }

    /**
     * Checks if the given addOn is allowed to access the requested service and registers them if not yet registered.
     *
     * @param addon the identifiable to check
     * @param permission the Permission to check
     * @throws IzouPermissionException thrown if the addOn is not allowed to access its requested service
     */
    public abstract void checkPermission(Permission permission, AddOnModel addon) throws IzouPermissionException;
}
