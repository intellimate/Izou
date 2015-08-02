package org.intellimate.izou.internal.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.internal.main.Main;
import org.intellimate.izou.internal.security.exceptions.IzouPermissionException;
import org.intellimate.izou.internal.security.exceptions.IzouSocketPermissionException;
import ro.fortsoft.pf4j.PluginDescriptor;

import java.lang.*;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * The SocketPermissionModule stores addOns that registered themselves to use socket connections.
 */
public final class SocketPermissionModule extends PermissionModule {

    private final List<String> allowedSocketConnections;

    /**
     * Creates a new PermissionModule
     *
     * @param main the instance of main
     */
    SocketPermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
        allowedSocketConnections = new ArrayList<>();
        //TODO: why????? I don't think this is save
        allowedSocketConnections.add(System.getProperty("host.name"));
        allowedSocketConnections.add("local");
        allowedSocketConnections.add("smtp");
    }

    /**
     * returns true if able to check permissions
     *
     * @param permission the permission to check
     * @return true if able to, false if not
     */
    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof SocketPermission;
    }

    /**
     * Checks if the given addOn is allowed to access the requested service and registers them if not yet registered.
     *
     * @param permission the Permission to check
     * @param addon      the identifiable to check
     * @throws IzouPermissionException thrown if the addOn is not allowed to access its requested service
     */
    @Override
    public void checkPermission(Permission permission, AddOnModel addon) throws IzouPermissionException {
        for (String socket : allowedSocketConnections) {
            if (permission.getName().contains(socket)) {
                return;
            }
        }

        if (isRegistered(addon))
            return;

        Function<PluginDescriptor, Boolean> checkPermission = descriptor -> {
            if (descriptor.getAddOnProperties() == null)
                throw new IzouPermissionException("addon_config.properties not found for addon:" + addon);
            try {
                return descriptor.getAddOnProperties().getProperty("socket_connection") != null
                        && descriptor.getAddOnProperties().getProperty("socket_connection").trim().equals("true")
                        && descriptor.getAddOnProperties().getProperty("socket_usage_descripton") != null
                        && !descriptor.getAddOnProperties().getProperty("socket_usage_descripton").trim().equals("null")
                        && !descriptor.getAddOnProperties().getProperty("socket_usage_descripton").trim().isEmpty();
            } catch (NullPointerException e) {
                return false;
            }
        };

        String exceptionMessage = "Socket Permission Denied: " + addon + "is not registered to "
                + "use socket connections, please add the required information to the addon_config.properties "
                + "file of your addOn.";
        registerOrThrow(addon, () -> new IzouSocketPermissionException(exceptionMessage), checkPermission);
    }
}