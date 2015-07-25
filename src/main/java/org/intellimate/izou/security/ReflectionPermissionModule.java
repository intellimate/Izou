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
    private List<String> forbiddenReflections;

    public ReflectionPermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
        // Security package
        forbiddenReflections = new ArrayList<>();
        forbiddenReflections.add(AudioPermissionModule.class.toString());
        forbiddenReflections.add(FilePermissionModule.class.toString());
        forbiddenReflections.add(PermissionManager.class.toString());
        forbiddenReflections.add(PermissionModule.class.toString());
        forbiddenReflections.add(ReflectionPermissionModule.class.toString());
        forbiddenReflections.add(RootPermission.class.toString());
        forbiddenReflections.add(SecureAccess.class.toString());
        forbiddenReflections.add(SecurityBreachHandler.class.toString());
        forbiddenReflections.add(SecurityManager.class.toString());
        forbiddenReflections.add(SecurityFunctions.class.toString());
        forbiddenReflections.add(SocketPermissionModule.class.toString());

        // Other
        forbiddenReflections.add(AddOnManager.class.toString());
        forbiddenReflections.add(Main.class.toString());
    }

    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof ReflectPermission && "suppressAccessChecks".equals(permission.getName());
    }

    @Override
    public void checkPermission(Permission permission, AddOnModel addon) throws SecurityException {
        for (Thread thread : Thread.getAllStackTraces().keySet()) {
            for (StackTraceElement elem : thread.getStackTrace()) {
                Class self = sun.reflect.Reflection.getCallerClass(1);
                Class caller = sun.reflect.Reflection.getCallerClass(2);
                if (self != caller && forbiddenReflections.contains(elem.getMethodName())) {
                    throw getException("Reflection not allowed here.");
                }
            }
        }
    }
}
