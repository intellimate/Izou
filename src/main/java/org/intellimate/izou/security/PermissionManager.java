package org.intellimate.izou.security;

import java.security.Permission;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The PermissionManager handles all permission conflicts within Izou. For example, if two addOns want to play music at
 * the same time, the PermissionManager will interfere and decide who gets the play the music. The PermissionManager
 * has nothing to do with general system security, it is only there to avoid "collisions" between addOns. If you are
 * looking for system security, look at the {@link SecurityManager}.
 */
public final class PermissionManager {
    private static boolean exists = false;
    private HashMap<String, List<PermissionModule>> permissionModules;

    /**
     * Creates an SecureAccess. There can only be one single SecureAccess, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return an IzouSecurityManager
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    static PermissionManager createPermissionManager() throws IllegalAccessException {
        if (!exists) {
            PermissionManager permissionManager = new PermissionManager();
            exists = true;
            return permissionManager;
        }

        throw new IllegalAccessException("Cannot create more than one instance of PermissionManager");
    }

    /**
     * Creates a new SecureAccess instance if and only if none has been created yet
     *
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    private PermissionManager() throws IllegalAccessException {
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of PermissionManager");
        }

        permissionModules = new HashMap<>();
    }

    /**
     * Registers a permission module with the permission manager.
     *
     * @param sdkVersion the sdk version of the permission module
     * @param module the permission module to register
     */
    public void registerPermissionModule(String sdkVersion, PermissionModule module) {
        if (permissionModules.get(sdkVersion) == null) {
            List<PermissionModule> modules = new ArrayList<>();
            modules.add(module);
            permissionModules.put(sdkVersion, modules);
        } else {
            permissionModules.get(sdkVersion).add(module);
        }
    }

    /**
     * Checks if the given permission {@code permission} is somewhere allowed in the permission
     * @param sdkVersion
     * @param permission
     * @return
     */
    boolean checkSDKPermissions(String sdkVersion, Permission permission) {
        List<PermissionModule> modules = permissionModules.get(sdkVersion);
        if (modules == null) {
            return false;
        }

        boolean allowed = false;
        for (PermissionModule module : modules) {
            allowed = module.checkPermission(permission);
        }

        return allowed;
    }
}
