package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.system.file.FileSystemManager;
import ro.fortsoft.pf4j.IzouPluginClassLoader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.io.IOException;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
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
    private final List<String> allowedWriteDirectories;
    private final List<String> forbiddenProperties;
    private final String allowedReadFileTypesRegex;
    private final String allowedWriteFileTypesRegex;
    private final SecureAccess secureAccess;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Creates a SecurityManager. There can only be one single SecurityManager, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return a SecurityManager from Izou
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public static SecurityManager createSecurityManager() throws IllegalAccessException {
        if (!exists) {
            SecurityManager securityManager = new SecurityManager();
            exists = true;
            return  securityManager;
        }

        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    /**
     * Creates a new IzouSecurityManager instance
     */
    private SecurityManager() throws IllegalAccessException {
        super();
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
        }

        SecureAccess tempSecureAccess = null;
        try {
            tempSecureAccess = SecureAccess.createSecureAccess();
        } catch (IllegalAccessException e) {
            logger.fatal("Unable to create a SecureAccess object because Izou might be under attack. "
                    + "Exiting now.", e);
            exitPermission = true;
            System.exit(1);
        }
        secureAccess = tempSecureAccess;
        allowedReadDirectories = new ArrayList<>();
        allowedReadFiles = new ArrayList<>();
        allowedWriteDirectories = new ArrayList<>();
        forbiddenProperties = new ArrayList<>();
        allowedSocketConnections = new ArrayList<>();
        allowedReadFileTypesRegex = "(txt|properties|xml|class|json|zip|ds_store|mf|jar|idx|log|dylib|mp3|dylib|certs)";
        allowedWriteFileTypesRegex = "(txt|properties|xml|json|idx|log)";
        init();
    }

    /**
     * Initializes some aspects of the security manager (giving default permissions etc.)
     */
    private void init() {
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;

        forbiddenProperties.add("jdk.lang.process.launchmechanism");

        allowedReadDirectories.add(workingDir);
        allowedReadDirectories.add("/Users/julianbrendl/Desktop");
        allowedReadDirectories.addAll(Arrays.asList(System.getProperty("java.ext.dirs").split(":")));
        allowedReadDirectories.add(System.getProperty("java.home"));
        allowedReadDirectories.add(System.getProperty("user.home"));
        allowedSocketConnections.add(System.getProperty("host.name"));
        allowedSocketConnections.add("local");
        allowedSocketConnections.add("smtp");
        allowedWriteDirectories.add(workingDir);
    }

    /**
     * Gets the current class loader through reflection, that is the class loader to which the class belongs that
     * triggered the security manager call
     *
     * @return the current class loader
     */
    private ClassLoader getCurrentClassLoader() {
        Class[] classes = getClassContext();
        for (int i = classes.length - 1; i >= 0; i--) {
            if (classes[i].getClassLoader() instanceof IzouPluginClassLoader
                    && !classes[i].getName().toLowerCase().contains("org.intellimate.izou.sdk")) { //TODO: exchange ID with PF4J Constant
                return classes[i].getClassLoader();
            }
        }

        return null;
    }

    /**
     * Checks if the permission {@code perm} is a {@link SocketPermission} and if so checks if the addOn has socket
     * connections properly enabled, else throws a {@link SecurityException}. If the permission is not a
     * SocketPermission, nothing is done as the permission has nothing to do with socket connections.
     *
     * @param perm the permission to check
     * @throws SecurityException thrown if the permission is not granted
     */
    private void checkSocketPermission(Permission perm) throws SecurityException {
        if (!(perm instanceof SocketPermission)) {
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

        for (String socket : allowedSocketConnections) {
            if (perm.getName().contains(socket)) {
                return;
            }
        }

        Properties addOnProperties = izouClassLoader.getPluginDescriptor().getAddOnProperties();

        boolean canConnect = false;
        try {
            canConnect = addOnProperties.get("socket_connection").equals("true")
                    && !addOnProperties.get("socket_usage_descripton").equals("null");
        } catch (NullPointerException e) {
            throwException(perm.getName());
        }

        if (!canConnect) {
            throwException(perm.getName());
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
    private void checkFilePermission(Permission perm) throws SecurityException {
        if (!(perm instanceof FilePermission)) {
            return;
        }

        String canonicalName = perm.getName().intern().toLowerCase();
        String canonicalAction =  perm.getActions().intern().toLowerCase();

        if (canonicalName.contains("all files") || canonicalAction.equals("execute")) {
            throwException(perm.getName());
        }

        if (canonicalAction.equals("read") && fileReadCheck(canonicalName)) {
            return;
        } else if (fileWriteCheck(canonicalName)) {
            // If read or execute permission is not asked, default to write permission check, which grants less rights
            return;
        }

        throwException(perm.getName());
    }

    /**
     * Determines if the file at the given file path is safe to read from in all aspects, if so returns true, else false
     *
     * @param filePath the path to the file to read from
     * @return true if the filepath is considered safe to read from, else false
     */
    private boolean fileReadCheck(String filePath) {
        File potentialFile = new File(filePath);
        String canonicalPath;
        try {
            canonicalPath = potentialFile.getCanonicalPath();
        } catch (IOException e) {
            logger.error("Error getting canonical path", e);
            return false;
        }

        for (String file : allowedReadFiles) {
            if (canonicalPath.contains(file)) {
                return true;
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
            return false;
        }

        String[] pathParts = canonicalPath.split(File.separator);
        String lastPathPart = pathParts[pathParts.length - 1].toLowerCase();

        String[] pathPeriodParts = lastPathPart.split("\\.");
        String fileExtension = pathPeriodParts[pathPeriodParts.length - 1].toLowerCase();

        if (!secureAccess.checkForExistingFileOrDirectory(canonicalPath)
                || secureAccess.checkForDirectory(canonicalPath)) {
            return true;
        }

        Pattern pattern = Pattern.compile(allowedReadFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        return matcher.matches() || fileExtension.equals(lastPathPart);
    }

    /**
     * Determines if the file at the given file path is safe to write to in all aspects, if so returns true, else false
     *
     * @param filePath the path to the file to write to
     * @return true if the filepath is considered safe to write to, else false
     */
    private boolean fileWriteCheck(String filePath) {
        File potentialFile = new File(filePath);
        String canonicalPath;
        try {
            canonicalPath = potentialFile.getCanonicalPath();
        } catch (IOException e) {
            logger.error("Error getting canonical path", e);
            return false;
        }

        boolean allowedDirectory = false;
        for (String dir : allowedWriteDirectories) {
            if (canonicalPath.contains(dir)) {
                allowedDirectory = true;
                break;
            }
        }
        if (!allowedDirectory) {
            return false;
        }

        String[] pathParts = canonicalPath.split("\\.");
        String fileExtension = pathParts[pathParts.length - 1].toLowerCase();

        if (!secureAccess.checkForExistingFileOrDirectory(canonicalPath)
                || secureAccess.checkForDirectory(canonicalPath)) {
            return true;
        }

        Pattern pattern = Pattern.compile(allowedWriteFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        return matcher.matches();
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
    private void throwException(String argument) {
        Exception exception =  new SecurityException("Access denied to " + argument);
        Class[] classStack = getClassContext();
        secureAccess.getBreachHandler().handleBreach(exception, classStack);
        throw (SecurityException) exception;
    }

    @Override
    public void checkPermission(Permission perm) {
        if (checkForSecureAccess()) {
            return;
        }
        checkFilePermission(perm);
        checkSocketPermission(perm);
    }

    @Override
    public void checkPropertyAccess(String key) {
        if (checkForSecureAccess()) {
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
            throwException(key);
        }
    }

    @Override
    public void checkExec(String cmd) {
        if (checkForSecureAccess()) {
            return;
        }
        throwException(cmd);
    }

    @Override
    public void checkExit(int status) {
        if (!exitPermission && !checkForSecureAccess()) {
            throwException("exit");
        } else {
            secureAccess.exitIzou();
        }
    }

    @Override
    public void checkDelete(String file) {
        if (checkForSecureAccess()) {
            return;
        }
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        if (checkForSecureAccess()) {
            return;
        }
    }

    @Override
    public void checkAccess(Thread t) {
        if (checkForSecureAccess()) {
            return;
        }
    }

    @Override
    public void checkRead(String file) {
        if (!checkForSecureAccess() && !fileReadCheck(file)) {
            throwException(file);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (!checkForSecureAccess() && !fileWriteCheck(fd.toString())) {
            throwException(fd.toString());
        }
    }
}
