package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;
import org.intellimate.izou.util.IdentifiableSet;
import org.intellimate.izou.util.IzouModule;
import ro.fortsoft.pf4j.PluginDescriptor;
import ro.fortsoft.pf4j.PluginWrapper;

import java.security.Permission;
import java.util.function.Function;
import java.util.function.Supplier;

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
    private final SecurityManager securityManager;

    /**
     * Creates a new PermissionModule
     * @param main an instance of main
     * @param securityManager an instance of securityManager
     */
    PermissionModule(Main main, SecurityManager securityManager) {
        super(main);
        registeredAddOns = new IdentifiableSet<>();
        this.securityManager = securityManager;
    }

    /**
     * returns an instance of SecurityManager
     * @return the SecurityManager
     */
    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    /**
     * Throws an exception with the argument of {@code argument}
     * @param argument what the exception is about (Access denied to (argument goes here))
     */
    SecurityException getException(String argument) {
        return securityManager.getException(argument);
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
    public synchronized void registerAddOn(AddOnModel addon) {
        registeredAddOns.add(addon);
    }

    /**
     * Checks if an addOn is registered with this PermissionModule
     *
     * @param addon the identifiable to check
     * @return true if the addOn is registered, else false
     */
    public synchronized boolean isRegistered(AddOnModel addon) {
        return registeredAddOns.contains(addon);
    }

    /**
     * Checks if the given addOn is allowed to access the requested service.
     *
     * @param addon the identifiable to check
     * @param permission the Permission to check
     * @throws SecurityException thrown if the addOn is not allowed to access its requested service
     */
    public abstract void checkPermission(Permission permission, AddOnModel addon) throws SecurityException;

    /**
     * registers the addon if checkPermission returns true, else throws the exception provided by the exceptionSupplier.
     * If the Addon was not added through PF4J it gets ignored
     * @param addOn the addon to check
     * @param checkPermission returns true if eligible for registering
     */
    protected <X extends IzouPermissionException> void registerOrThrow(AddOnModel addOn, Supplier<X> exceptionSupplier,
                                                                       Function<PluginDescriptor, Boolean> checkPermission) {
        getMain().getAddOnManager().getPluginWrapper(addOn)
                .map(PluginWrapper::getDescriptor)
                .map(checkPermission)
                .ifPresent(allowedToRun -> {
                    if (allowedToRun) {
                        registerAddOn(addOn);
                    } else {
                        throw exceptionSupplier.get();
                    }
                });
    }
}
