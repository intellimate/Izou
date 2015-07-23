package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;
import org.intellimate.izou.security.exceptions.IzouSocketPermissionException;
import ro.fortsoft.pf4j.PluginDescriptor;

import java.io.FilePermission;
import java.security.Permission;
import java.util.function.Function;

/**
 * @author LeanderK
 * @version 1.0
 */
public class RootPermission extends PermissionModule {
    /**
     * Creates a new PermissionModule
     *
     * @param main an instance of main
     * @param securityManager an instance of security-manager
     */
    RootPermission(Main main, SecurityManager securityManager) {
        super(main, securityManager);
    }

    /**
     * returns true if able to check permissions
     *
     * @param permission the permission to check
     * @return true if able to, false if not
     */
    @Override
    public boolean canCheckPermission(Permission permission) {
        return true;
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
        if (isRegistered(addon))
            return;

        if (permission instanceof FilePermission && !permission.getActions().intern().toLowerCase().equals("read")) {
            String canonicalName = permission.getName().intern().toLowerCase();
            getSecurityManager().getPermissionManager().getFilePermissionModule().fileWriteCheck(canonicalName, addon);
        }

        Function<PluginDescriptor, Boolean> checkPermission = descriptor -> {
            try {
                return descriptor.getAddOnProperties().get("root").equals("true");
            } catch (NullPointerException e) {
                return false;
            }
        };

        String exceptionMessage = "Root permission denied for: " + addon + "is not registered to "
                + "use socket root connections.";
        registerOrThrow(addon, () -> new IzouSocketPermissionException(exceptionMessage), checkPermission);
    }

    /**
     * returns true if the AddonModel is root
     * @param addOnModel the addOnModel to check
     * @return true if root, false if not
     */
    public boolean isRoot(AddOnModel addOnModel) {
        if (isRegistered(addOnModel)) {
            return true;
        }
        try {
            if (addOnModel.getPlugin().getDescriptor().getAddOnProperties().get("root").equals("true")) {
                registerAddOn(addOnModel);
                return true;
            }
        } catch (NullPointerException e) {
            return false;
        }
        return false;
    }
}
