package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;

import java.lang.reflect.ReflectPermission;
import java.security.Permission;

/**
 * Created by julianbrendl on 6/26/15.
 */
public class ReflectionPermissionModule extends PermissionModule {

    public ReflectionPermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
    }

    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof ReflectPermission && "suppressAccessChecks".equals(permission.getName());
    }

    @Override
    public void checkPermission(Permission permission, AddOnModel addon) throws SecurityException {
        for (StackTraceElement elem : Thread.currentThread().getStackTrace()) {
            if ("Test".equals(elem.getClassName()) && "badSetAccessible".equals(elem.getMethodName())) {
                throw new SecurityException();
            }
        }
    }
}
