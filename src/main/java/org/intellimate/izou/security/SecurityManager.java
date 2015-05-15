package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouSocketPermissionException;
import org.intellimate.izou.security.exceptions.IzouSoundPermissionException;
import org.intellimate.izou.support.SystemMail;
import org.intellimate.izou.system.file.FileSystemManager;
import ro.fortsoft.pf4j.IzouPluginClassLoader;
import ro.fortsoft.pf4j.PluginDescriptor;

import javax.sound.sampled.AudioPermission;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The IzouSecurityManager gives permission to all entitled components of Izou to execute or access files or commands.
 * It also blocks access to all potentially insecure actions.
 */
public final class SecurityManager extends java.lang.SecurityManager {
    private static boolean exists = false;
    private boolean exitPermission = false;
    private final List<String> allowedReadDirectories;
    private final List<String> allowedReadFiles;
    private final List<String> allowedSocketConnections;
    private final List<File> allowedWriteDirectories;
    private final List<String> forbiddenProperties;
    private final String allowedReadFileTypesRegex;
    private final String allowedWriteFileTypesRegex;
    private final SecureAccess secureAccess;
    private final PermissionManager permissionManager;
    private final SystemMail systemMail;
    private final Logger logger = LogManager.getLogger(this.getClass());

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
        this.systemMail = systemMail;

        SecureAccess tempSecureAccess = null;
        try {
            tempSecureAccess = SecureAccess.createSecureAccess(systemMail);
        } catch (IllegalAccessException e) {
            logger.fatal("Unable to create a SecureAccess object because Izou might be under attack. "
                    + "Exiting now.", e);
            exitPermission = true;
            System.exit(1);
        }
        permissionManager = PermissionManager.createPermissionManager();
        secureAccess = tempSecureAccess;
        allowedReadDirectories = new ArrayList<>();
        allowedReadFiles = new ArrayList<>();
        allowedReadFiles.add("/dev/random");
        allowedReadFiles.add("/dev/urandom");
        allowedWriteDirectories = new ArrayList<>();
        forbiddenProperties = new ArrayList<>();
        allowedSocketConnections = new ArrayList<>();
        allowedReadFileTypesRegex = "(txt|properties|xml|class|json|zip|ds_store|mf|jar|idx|log|dylib|mp3|dylib|certs|"
                + "so)";
        allowedWriteFileTypesRegex = "(txt|properties|xml|json|idx|log)";
        init(main);
    }

    /**
     * Initializes some aspects of the security manager (giving default permissions etc.)
     * @param main an instance of main
     */
    private void init(Main main) {
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;

        forbiddenProperties.add("jdk.lang.process.launchmechanism");

        allowedReadDirectories.add(workingDir);
        allowedReadDirectories.addAll(Arrays.asList(System.getProperty("java.ext.dirs").split(":")));
        allowedReadDirectories.add(System.getProperty("java.home"));
        allowedReadDirectories.add(System.getProperty("user.home"));
        allowedSocketConnections.add(System.getProperty("host.name"));
        allowedSocketConnections.add("local");
        allowedSocketConnections.add("smtp");
        allowedWriteDirectories.add(main.getFileSystemManager().getLogsLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getPropertiesLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getResourceLocation());
    }

    /**
     * Gets the current class loader, that is the class loader to which the class belongs that
     * triggered the security manager call
     *
     * @return the current class loader
     */
    private ClassLoader getCurrentClassLoader() {
        Class[] classes = getClassContext();
        for (int i = classes.length - 1; i >= 0; i--) {
            if (classes[i].getClassLoader() instanceof IzouPluginClassLoader && !classes[i].getName().toLowerCase()
                    .contains(IzouPluginClassLoader.PLUGIN_PACKAGE_PREFIX_IZOU_SDK)) {
                return classes[i].getClassLoader();
            }
        }

        return null;
    }

    /**
     * this method first performs some basic checks and then performs the specific check
     * @param t permission or file
     * @param specific the specific check
     */
    private <T> void check(T t, BiConsumer<T, IzouPluginClassLoader> specific) {
        if (!shouldCheck()) {
            return;
        }

        ClassLoader classLoader = getCurrentClassLoader();

        if (classLoader == null) {
            return;
        }

        IzouPluginClassLoader izouClassLoader;
        if (classLoader instanceof IzouPluginClassLoader) {
            izouClassLoader = (IzouPluginClassLoader) classLoader;
        } else {
            return;
        }

        specific.accept(t, izouClassLoader);
    }

    /**
     * performs some basic checks to determine whether to check the permission
     * @return true if should be checked, false if not
     */
    private boolean shouldCheck() {
        if (checkForSecureAccess()) {
            return false;
        }
        return true;
    }

    private void checkAudioPermission(Permission perm, IzouPluginClassLoader izouClassLoader) throws IzouSoundPermissionException {
        if (!(perm instanceof AudioPermission)) {
            return;
        }

        String addOnID = izouClassLoader.getPluginDescriptor().getPluginId();
        boolean accessGranted = permissionManager.getAudioPermissionModule().checkPermission(addOnID);

        if (!accessGranted) {
            PluginDescriptor descriptor = izouClassLoader.getPluginDescriptor();
            boolean canPlay = false;
            try {
                canPlay = descriptor.getAddOnProperties().get("audio_output").equals("true")
                        && !descriptor.getAddOnProperties().get("audio_usage_descripton").equals("null");
            } catch (NullPointerException e) {
                // Do nothing that is fine, it will just skip ahead and not register
            }
            if (canPlay) {

                permissionManager.getAudioPermissionModule().registerAddOn(descriptor.getPluginId());
            } else {
                throw new IzouSoundPermissionException("Audio Permission Denied: " + addOnID + "is not registered to "
                        + "play audio or there is already audio being played.");
            }
        }
    }

    /**
     * Checks if the permission {@code perm} is a {@link SocketPermission} and if so checks if the addOn has socket
     * connections properly enabled, else throws a {@link SecurityException}. If the permission is not a
     * SocketPermission, nothing is done as the permission has nothing to do with socket connections.
     *
     * @param perm the permission to check
     * @throws SecurityException thrown if the permission is not granted
     */
    private void checkSocketPermission(Permission perm, IzouPluginClassLoader izouClassLoader) throws IzouSocketPermissionException {
        if (!(perm instanceof SocketPermission)) {
            return;
        }

        for (String socket : allowedSocketConnections) {
            if (perm.getName().contains(socket)) {
                return;
            }
        }

        String addOnID = izouClassLoader.getPluginDescriptor().getPluginId();
        boolean accessGranted = permissionManager.getSocketPermissionModule().checkPermission(addOnID);

        if (!accessGranted) {
            PluginDescriptor descriptor = izouClassLoader.getPluginDescriptor();
            boolean canConnect = false;
            try {
                canConnect = descriptor.getAddOnProperties().get("socket_connection").equals("true")
                        && !descriptor.getAddOnProperties().get("socket_usage_descripton").equals("null");
            } catch (NullPointerException e) {
                // Do nothing that is fine, it will just skip ahead and not register
            }

            if (canConnect) {
                permissionManager.getSocketPermissionModule().registerAddOn(descriptor.getPluginId());
            } else {
                throw new IzouSocketPermissionException("Socket Permission Denied: " + addOnID + "is not registered to "
                        + "use socket connections, please add the required information to the addon_config.properties "
                        + "file of your addOn.");
            }
        }
    }

    /**
     * Checks if the permission {@code perm} is a {@link FilePermission} and if so checks is the IO operation is
     * allowed, else throws a {@link SecurityException}. If the permission is not a FilePermission, nothing is done as
     * the permission has nothing to do with file IO.
     *
     * @param perm the permission to check
     * @throws SecurityException thrown if the permission is not granted
     */
    private void checkFilePermission(Permission perm, IzouPluginClassLoader izouClassLoader) throws SecurityException {
        if (!(perm instanceof FilePermission)) {
            return;
        }

        String canonicalName = perm.getName().intern().toLowerCase();
        String canonicalAction =  perm.getActions().intern().toLowerCase();

        if (canonicalName.contains("all files") || canonicalAction.equals("execute")) {
            throw getException(perm.getName());
        }
        if (canonicalAction.equals("read")) {
            fileReadCheck(canonicalName);
        }
        fileWriteCheck(canonicalName);
    }

    /**
     * Determines if the file at the given file path is safe to read from in all aspects, if so returns true, else false
     *
     * @param filePath the path to the file to read from
     */
    private void fileReadCheck(String filePath) {
        File potentialFile = new File(filePath);
        String canonicalPath;
        try {
            canonicalPath = potentialFile.getCanonicalPath();
        } catch (IOException e) {
            logger.error("Error getting canonical path", e);
            throw getException(filePath);
        }

        for (String file : allowedReadFiles) {
            if (canonicalPath.contains(file)) {
                return;
            }
        }

        boolean allowedDirectory = false;
        for (String dir : allowedReadDirectories) {
            if (canonicalPath.contains(dir)) {
                allowedDirectory = true;
                break;
            }
        }
        if (!allowedDirectory) {
            throw getException(filePath);
        }

        String[] pathParts = canonicalPath.split(File.separator);
        String lastPathPart = pathParts[pathParts.length - 1].toLowerCase();

        String[] pathPeriodParts = lastPathPart.split("\\.");
        String fileExtension = pathPeriodParts[pathPeriodParts.length - 1].toLowerCase();

        if (!secureAccess.checkForExistingFileOrDirectory(canonicalPath)
                || secureAccess.checkForDirectory(canonicalPath)) {
            return;
        }

        Pattern pattern = Pattern.compile(allowedReadFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        if (!matcher.matches() || fileExtension.equals(lastPathPart))
            throw getException(filePath);
    }

    /**
     * Determines if the file at the given file path is safe to write to in all aspects, if so returns true, else false
     *
     * @param filePath the path to the file to write to
     */
    private void fileWriteCheck(String filePath) {
        File potentialFile = new File(filePath);
        String canonicalPath;
        try {
            canonicalPath = potentialFile.getCanonicalPath();
        } catch (IOException e) {
            logger.error("Error getting canonical path", e);
            throw getException(filePath);
        }

        boolean allowedDirectory = false;
        for (String dir : allowedWriteDirectories) {
            if (canonicalPath.contains(dir)) {
                allowedDirectory = true;
                break;
            }
        }
        if (!allowedDirectory) {
            throw getException(filePath);
        }

        String[] pathParts = canonicalPath.split("\\.");
        String fileExtension = pathParts[pathParts.length - 1].toLowerCase();

        if (!secureAccess.checkForExistingFileOrDirectory(canonicalPath)
                || secureAccess.checkForDirectory(canonicalPath)) {
            return;
        }

        Pattern pattern = Pattern.compile(allowedWriteFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        if (!matcher.matches())
            throw getException(filePath);
    }

    /**
     * Checks if {@link SecureAccess} is included in the current class context, if so true is returned, else false
     *
     * @return true if {@link SecureAccess} is included in the current class context, else false
     */
    private boolean checkForSecureAccess() {
        Class[] classContext = getClassContext();
        for (Class clazz : classContext) {
            if (clazz.equals(SecureAccess.class) || clazz.equals(SecurityBreachHandler.class)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Throws an exception with the argument of {@code argument}
     * @param argument what the exception is about (Access denied to (argument goes here))
     */
    private SecurityException getException(String argument) {
        SecurityException exception =  new SecurityException("Access denied to " + argument);
        Class[] classStack = getClassContext();
        secureAccess.getBreachHandler().handleBreach(exception, classStack);
        return exception;
    }

    @Override
    public void checkPermission(Permission perm) {
        BiConsumer<Permission, IzouPluginClassLoader> checker = (permi, loader) -> {
            if (permi instanceof FilePermission) checkFilePermission(permi, loader);
            else if (permi instanceof SocketPermission) checkSocketPermission(permi, loader);
            else checkAudioPermission(permi, loader);
        };
        check(perm, checker);
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
        if (!exitPermission && !checkForSecureAccess()) {
            throw getException("exit");
        } else {
            secureAccess.exitIzou();
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
        check(file, (file1, loader) -> fileReadCheck(file1));
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        check(fd.toString(), (file, loader) -> fileWriteCheck(file));
    }
}
