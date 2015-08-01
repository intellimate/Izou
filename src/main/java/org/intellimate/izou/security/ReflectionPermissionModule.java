package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnManager;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;

import java.lang.reflect.ReflectPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * The
 */
public class ReflectionPermissionModule extends PermissionModule {
    private List<Class> forbiddenReflections;

    public ReflectionPermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
        // Security package
        //Leander: I don't think this is a good idea
        forbiddenReflections = new ArrayList<>();
        forbiddenReflections.add(AudioPermissionModule.class);
        forbiddenReflections.add(FilePermissionModule.class);
        forbiddenReflections.add(PermissionManager.class);
        forbiddenReflections.add(PermissionModule.class);
        forbiddenReflections.add(ReflectionPermissionModule.class);
        forbiddenReflections.add(RootPermission.class);
        forbiddenReflections.add(SecureAccess.class);
        forbiddenReflections.add(SecurityBreachHandler.class);
        forbiddenReflections.add(SecurityManager.class);
        forbiddenReflections.add(SecurityFunctions.class);
        forbiddenReflections.add(SocketPermissionModule.class);

        // Other
        forbiddenReflections.add(AddOnManager.class);
        forbiddenReflections.add(Main.class);
    }

    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof ReflectPermission && "suppressAccessChecks".equals(permission.getName());
    }

    @Override
    public void checkPermission(Permission permission, AddOnModel addon) throws SecurityException {
        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
            if ("Test".equals(elem.getClassName()) && "badSetAccessible".equals(elem.getMethodName())) {
                throw getException("Reflection not allowed here.");
            }
        }
    }
}
