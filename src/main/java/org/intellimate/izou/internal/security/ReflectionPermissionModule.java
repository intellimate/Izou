package org.intellimate.izou.internal.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.internal.main.Main;
import ro.fortsoft.pf4j.IzouPluginClassLoader;

import java.lang.reflect.ReflectPermission;
import java.security.Permission;

/**
 * The ReflectionPermissionModule stops any illegal reflection calls on Izou.
 * <p>
 *     For example, if an addOn were to reflect on the secure access class to get root access, then the
 *     ReflectionPermissionModule would stop that reflection.
 * </p>
 */
public class ReflectionPermissionModule extends PermissionModule {

    /**
     * Creates a new ReflectionPermissionModule
     *
     * @param main the main class of Izou
     * @param securityManager the security manager in Izou
     */
    public ReflectionPermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
    }

    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof ReflectPermission && "suppressAccessChecks".equals(permission.getName());
    }

    @Override
    public void checkPermission(Permission permission, AddOnModel addon) throws SecurityException {
        boolean reflectionUsedInStack = false;
        boolean addOnInStack = false;

        SecurityManager securityManager = (SecurityManager) System.getSecurityManager();
        Class[] classes = securityManager.getClassContextPkg();

        for (Class clazz : classes) {
            if (clazz.getPackage().getName().toLowerCase().contains("reflect")) {
                reflectionUsedInStack = true;
            } else if (clazz.getClassLoader() instanceof IzouPluginClassLoader) {
                addOnInStack = true;
            }

            if (reflectionUsedInStack && addOnInStack) {
                throw getException("reflection");
            }
        }
    }
}
