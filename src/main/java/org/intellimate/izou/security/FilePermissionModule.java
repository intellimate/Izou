package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;

import java.io.FilePermission;
import java.security.Permission;

/**
 * The FilePermissionModule is used to check the access to files
 * @author LeanderK
 * @version 1.0
 */
public class FilePermissionModule extends PermissionModule {
    /**
     * Creates a new PermissionModule
     *
     * @param main the instance of main
     */
    FilePermissionModule(Main main) {
        super(main);
    }

    /**
     * returns true if able to check permissions
     *
     * @param permission the permission to check
     * @return true if able to, false if not
     */
    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof FilePermission;
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

    }
}
