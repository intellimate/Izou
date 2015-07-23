package org.intellimate.izou.security;

import org.intellimate.izou.util.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;

import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * The PermissionManager handles all permission conflicts within Izou. For example, if two addOns want to play music at
 * the same time, the PermissionManager will interfere and decide who gets the play the music. The PermissionManager
 * has nothing to do with general system security, it is only there to avoid "collisions" between addOns. If you are
 * looking for system security, look at the {@link SecurityManager}.
 */
public final class PermissionManager extends IzouModule {
    private final List<PermissionModule> standardCheck;
    private final FilePermissionModule filePermissionModule;
    private final RootPermission rootPermission;

    /**
     * Creates a new PermissionManager instance if and only if none has been created yet
     *
     * @throws IllegalAccessException thrown if this method is called more than once
     * @param main an instance of Main
     * @param securityManager an instance of SecurityManager
     */
    PermissionManager(Main main, SecurityManager securityManager) throws IllegalAccessException {
        super(main);
        standardCheck = new ArrayList<>();
        standardCheck.add(new AudioPermissionModule(main, securityManager));
        standardCheck.add(new SocketPermissionModule(main, securityManager));
        filePermissionModule = new FilePermissionModule(main, securityManager);
        standardCheck.add(new ReflectionPermissionModule(main, securityManager));
        rootPermission = new RootPermission(main, securityManager);
        standardCheck.add(filePermissionModule);
    }

    public FilePermissionModule getFilePermissionModule() {
        return filePermissionModule;
    }

    public RootPermission getRootPermission() {
        return rootPermission;
    }

    /**
     * checks the permission
     * @param perm the permission
     * @param addOnModel the associated AddOnModel
     * @throws IzouPermissionException if the permission was not granted
     */
    public void checkPermission(Permission perm, AddOnModel addOnModel) throws IzouPermissionException {
        try {
            rootPermission.checkPermission(perm, addOnModel);
            //its root
            return;
        } catch (IzouPermissionException ignored) {
            //its just not root
        }
        standardCheck.stream()
                .filter(permissionModule -> permissionModule.canCheckPermission(perm))
                .forEach(permissionModule -> permissionModule.checkPermission(perm, addOnModel));
    }
}
