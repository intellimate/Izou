package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.addon.AddOnManager;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.identification.IdentificationManagerM;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;
import org.intellimate.izou.security.storage.SecureStorage;
import org.intellimate.izou.support.SystemMail;
import ro.fortsoft.pf4j.IzouPluginClassLoader;

import java.io.FileDescriptor;
import java.security.Permission;
import java.security.Security;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * The IzouSecurityManager gives permission to all entitled components of Izou to execute or access files or commands.
 * It also blocks access to all potentially insecure actions.
 */
public final class SecurityManager extends java.lang.SecurityManager {
    private static boolean exists = false;
    private final SecureAccess secureAccess;
    private final PermissionManager permissionManager;
    private final SystemMail systemMail;
    private final Main main;
    //TODO Design: move to other class
    private final List<String> forbiddenProperties;
    private final List<String> forbiddenPackagesForAddOns;

    /**
     * Creates a SecurityManager. There can only be one single SecurityManager, so calling this method twice
     * will cause an illegal access exception.
     *
     * @param systemMail the system mail object in order to send e-mails to owner in case of emergency
     * @param main a reference to the main instance
     * @return a SecurityManager from Izou
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public static SecurityManager createSecurityManager(SystemMail systemMail, Main main) throws IllegalAccessException {
        if (!exists) {
            SecurityManager securityManager = new SecurityManager(systemMail, main);
            exists = true;
            return  securityManager;
        }
        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    /**
     * Creates a new IzouSecurityManager instance
     *
     * @param systemMail the system mail object in order to send e-mails to owner in case of emergency
     * @param main the instance of main
     */
    private SecurityManager(SystemMail systemMail, Main main) throws IllegalAccessException {
        super();
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
        }

        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());

        this.systemMail = systemMail;
        this.main = main;

        SecureStorage.createSecureStorage(main);

        SecureAccess tempSecureAccess = null;
        try {
            tempSecureAccess = SecureAccess.createSecureAccess(main, systemMail);
        } catch (IllegalAccessException e) {
            Logger logger = LogManager.getLogger(this.getClass());
            logger.fatal("Unable to create a SecureAccess object because Izou might be under attack. "
                    + "Exiting now.", e);
            getSecureAccess().doElevated(() -> System.exit(1));
        }
        permissionManager = new PermissionManager(main, this);
        secureAccess = tempSecureAccess;
        forbiddenProperties = new ArrayList<>();
        forbiddenProperties.add("jdk.lang.process.launchmechanism");

        forbiddenPackagesForAddOns = new ArrayList<>();
        forbiddenPackagesForAddOns.add(SecurityManager.class.getPackage().getName());
        forbiddenPackagesForAddOns.add(IzouPermissionException.class.getPackage().getName());
        forbiddenPackagesForAddOns.add(AddOnManager.class.getPackage().getName());
        forbiddenPackagesForAddOns.add(IdentificationManagerM.class.getPackage().getName());
    }

    SecureAccess getSecureAccess() {
        return secureAccess;
    }

    PermissionManager getPermissionManager() {
        return permissionManager;
    }

    /**
     * Gets the current AddOnModel, that is the AddOnModel for the class loader to which the class belongs that
     * triggered the security manager call, or throws a IzouPermissionException
     * @return AddOnModel or IzouPermissionException if the call was made from an AddOn, or null if no AddOn is responsible
     * @throws IzouPermissionException if the AddOnModel is not found
     */
    public Optional<AddOnModel> getAddOnModelForClassLoader() {
        Class[] classes = getClassContext();
        for (int i = classes.length - 1; i >= 0; i--) {
            if (classes[i].getClassLoader() instanceof IzouPluginClassLoader && !classes[i].getName().toLowerCase()
                    .contains(IzouPluginClassLoader.PLUGIN_PACKAGE_PREFIX_IZOU_SDK)) {
                ClassLoader classLoader = classes[i].getClassLoader();
                return main.getAddOnInformationManager().getAddOnForClassLoader(classLoader);
            }
        }
        return Optional.empty();
    }

    /**
     * Makes the {@link #getClassContext()} method of the security manager available to the entire package
     *
     * @return look at {@link #getClassContext()}
     */
    Class[] getClassContextPkg() {
        return getClassContext();
    }

    /**
     * this method first performs some basic checks and then performs the specific check
     * @param t permission or file
     * @param specific the specific check
     */
    private <T> void check(T t, BiConsumer<T, AddOnModel> specific) {
        if (!shouldCheck()) {
            return;
        }
        secureAccess.doElevated(this::getAddOnModelForClassLoader)
            .ifPresent(addOnModel ->
                    secureAccess.doElevated(() -> specific.accept(t, addOnModel)));
    }

    /**
     * performs some basic checks to determine whether to check the permission
     * @return true if should be checked, false if not
     */
    // TODO: @leander, why does this just invert checkForSecureAccess? do we really need a method for this?
    public boolean shouldCheck() {
        return !checkForSecureAccess();
    }

    /**
     * Checks if {@link SecureAccess} is included in the current class context, if so true is returned, else false
     *
     * @return true if {@link SecureAccess} is included in the current class context, else false
     */
    private boolean checkForSecureAccess() {
        Class[] classContext = getClassContext();
        for (Class clazz : classContext) {
            if (clazz.equals(SecureAccess.class) || clazz.equals(SecurityBreachHandler.class)
                    || clazz.equals(SecurityFunctions.class) || clazz.equals(SecureStorage.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if addon components are in the stack (including sdk)
     *
     * @return true if addon components are in the stack (meaning they used the current application)
     */
    private boolean checkForAddOnAccess() {
        Class[] classContext = getClassContext();
        for (Class clazz : classContext) {
            if (clazz.getClassLoader() instanceof IzouPluginClassLoader) {
                return true;
            }
        }
        return false;
    }

    /**
     * Throws an exception with the argument of {@code argument}
     * @param argument what the exception is about (Access denied to (argument goes here))
     */
    SecurityException getException(String argument) {
        SecurityException exception =  new SecurityException("Access denied to " + argument);
        Class[] classStack = getClassContext();
        secureAccess.getBreachHandler().handleBreach(exception, classStack);
        return exception;
    }

    @Override
    public void checkPermission(Permission perm) {
        check(perm, permissionManager::checkPermission);
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (!shouldCheck()) {
            return;
        }
        String canonicalKey = key.intern().toLowerCase();

        boolean allowedProperty = true;
        for (String property : forbiddenProperties) {
            if (canonicalKey.contains(property.toLowerCase())) {
                allowedProperty = false;
                break;
            }
        }

        if (!allowedProperty) {
            throw getException(key);
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (!shouldCheck()) {
            return;
        }
        throw getException(cmd);
    }

    @Override
    public void checkExit(int status) {
        if (!checkForSecureAccess()) {
            throw getException("exit");
        }
    }

    @Override
    public void checkDelete(String file) {
        if (!shouldCheck()) {
            return;
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (!shouldCheck()) {
            return;
        }
    }

    @Override
    public void checkAccess(Thread t) {
        if (!shouldCheck()) {
            return;
        }
    }

    @Override
    public void checkRead(String file) {
        if (file.endsWith("/org/intellimate/izou/security/SecurityModule.class"))
            return;
        if (!shouldCheck()) {
            return;
        }
        if (!getAddOnModelForClassLoader().isPresent()) {
            return;
        }
        permissionManager.getFilePermissionModule().fileReadCheck(file);
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        check(fd.toString(), permissionManager.getFilePermissionModule()::fileWriteCheck);
    }

    @Override
    public void checkPackageAccess(String pkg) {
        // Denies addOns access to packages in the list "forbiddenPackagesForAddOns"
        if (forbiddenPackagesForAddOns.contains(pkg) && (!checkForSecureAccess() && checkForAddOnAccess())) {
            throw getException("package: " + pkg);
        }
    }
}
