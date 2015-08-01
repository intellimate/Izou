package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnManager;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import ro.fortsoft.pf4j.IzouPluginClassLoader;

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
