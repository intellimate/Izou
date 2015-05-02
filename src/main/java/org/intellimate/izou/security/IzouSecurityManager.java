package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.system.file.FileSystemManager;
import ro.fortsoft.pf4j.IzouPluginClassLoader;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketPermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The IzouSecurityManager gives permission to all entitled components of Izou to execute or access files or commands.
 * It also blocks access to all potentially insecure actions.
 */
public class IzouSecurityManager extends SecurityManager {
    private static boolean exists = false;
    private boolean tempFileAccess = false;
    private final List<String> allowedReadDirectories;
    private final List<String> allowedWriteDirectories;
    private final List<String> forbiddenProperties;
    private final String allowedReadFileTypesRegex;
    private final String allowedWriteFileTypesRegex;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Creates an IzouSecurityManager. There can only be one single IzouSecurityManager, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return an IzouSecurityManager
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public static IzouSecurityManager createSecurityManager() throws IllegalAccessException {
        if (!exists) {
            exists = true;
            return new IzouSecurityManager();
        }

        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    /**
     * Creates a new IzouSecurityManager instance
     */
    private IzouSecurityManager() {
        super();
        allowedReadDirectories = new ArrayList<>();
        allowedWriteDirectories = new ArrayList<>();
        forbiddenProperties = new ArrayList<>();
        allowedReadFileTypesRegex = "(txt|properties|xml|class|json|zip|ds_store|mf|jar|idx|log|dylib|mp3)";
        allowedWriteFileTypesRegex = "(txt|properties|xml|json|idx|log)";
        init();
    }

    /**
     * Initializes some aspects of the security manager (giving default permissions etc.)
     */
    private void init() {
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
        while (!workingDir.endsWith(File.separator + "Izou" + File.separator)) {
            workingDir = workingDir.substring(0, workingDir.length() - 1);
        }

        forbiddenProperties.add("jdk.lang.process.launchmechanism");

        allowedReadDirectories.add(workingDir);
        allowedReadDirectories.add(".m2");
        allowedReadDirectories.add("/Users/julianbrendl/Desktop");
        allowedReadDirectories.add(System.getProperty("java.home"));

        allowedWriteDirectories.add(workingDir);
    }

    /**
     * Gets the current class loader through reflection, that is the class loader to which the class belongs that
     * triggered the security manager call
     *
     * @return the current class loader
     */
    private ClassLoader getCurrentClassLoader() {
        Method privateStringMethod;
        try {
            privateStringMethod = SecurityManager.class.getDeclaredMethod("currentClassLoader0");
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return null;
        }

        privateStringMethod.setAccessible(true);

        ClassLoader loader = null;
        try {
            loader = (ClassLoader) privateStringMethod.invoke(this, null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

        return loader;
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

        IzouPluginClassLoader izouClassLoader;
        if (classLoader instanceof IzouPluginClassLoader) {
            izouClassLoader = (IzouPluginClassLoader) classLoader;
        } else {
            return;
        }

        Properties addOnProperties = izouClassLoader.getPluginDescriptor().getAddOnProperties();

        boolean canConnect;
        try {
            canConnect = addOnProperties.get("socket_connection").equals("true")
                    && !addOnProperties.get("socket_usage_descripton").equals("null");
        } catch (NullPointerException e) {
            throw new SecurityException("Access denied to " + perm.getName());
        }

        if (!canConnect) {
             throw new SecurityException("Access denied to " + perm.getName());
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
            throw new SecurityException("Access denied to " + perm.getName());
        }

        if (canonicalAction.equals("read") && fileReadCheck(canonicalName)) {
            return;
        } else if (fileWriteCheck(canonicalName)) {
            // If read or execute permission is not asked, default to write permission check, which grants less rights
            return;
        }

        throw new SecurityException("Access denied to " + perm.getName());
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

        String[] pathParts = canonicalPath.split("\\.");
        String fileExtension = pathParts[pathParts.length - 1].toLowerCase();

        tempFileAccess = true;
        File file = new File(canonicalPath);
        if (!file.exists() || file.isDirectory()) {
            tempFileAccess = false;
            return true;
        }
        tempFileAccess = false;

        Pattern pattern = Pattern.compile(allowedReadFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        return matcher.matches();
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

        tempFileAccess = true;
        File file = new File(canonicalPath);
        if (!file.exists() || file.isDirectory()) {
            tempFileAccess = false;
            return true;
        }
        tempFileAccess = false;

        Pattern pattern = Pattern.compile(allowedWriteFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        return matcher.matches();
    }

    @Override
    public void checkPermission(Permission perm) {
        checkFilePermission(perm);
        checkSocketPermission(perm);
    }

    @Override
    public void checkPropertyAccess(String key) {
        String canonicalKey = key.intern().toLowerCase();

        boolean allowedProperty = true;
        for (String property : forbiddenProperties) {
            if (canonicalKey.contains(property.toLowerCase())) {
                allowedProperty = false;
                break;
            }
        }

        if (!allowedProperty) {
            throw new SecurityException("Access denied to " + key);
        }
    }

    @Override
    public void checkExec(String cmd) {
        throw new SecurityException("Access denied to " + cmd);
    }

    @Override
    public void checkExit(int status) {
        throw new SecurityException("Access denied to exit");
    }

    @Override
    public void checkDelete(String file) {
        super.checkDelete(file);
    }

    @Override
    public void checkAccess(ThreadGroup g) {
        System.out.println("Checked");
    }

    @Override
    public void checkAccess(Thread t) {
        System.out.println("Checked");
    }

    @Override
    public void checkRead(String file) {
        if (!tempFileAccess && !fileReadCheck(file)) {
            throw new SecurityException("Access denied to " + file);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (!tempFileAccess && !fileWriteCheck(fd.toString())) {
            throw new SecurityException("Access denied to " + fd.toString());
        }
    }
}
