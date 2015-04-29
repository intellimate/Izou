package org.intellimate.izou.security;

import org.intellimate.izou.system.file.FileSystemManager;

import java.io.File;
import java.io.FileDescriptor;
import java.security.AccessController;
import java.security.Permission;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The IzouSecurityManager gives permission to all entitled components of Izou to execute or access files or commands.
 * It also blocks access to all potentially insecure actions.
 */
public class IzouSecurityManager extends SecurityManager {
    private static boolean exists = false;
    private final List<String> allowedDirectories;
    private final String allowedFileTypesRegex;

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

    private IzouSecurityManager() {
        super();
        allowedDirectories = new ArrayList<>();
        allowedFileTypesRegex = "(txt|properties|xml|class|json|zip)";
        init();
    }

    private void init() {
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
        while (!workingDir.endsWith(File.separator + "Izou" + File.separator)) {
            workingDir = workingDir.substring(0, workingDir.length() - 1);
        }
        allowedDirectories.add(workingDir);
        allowedDirectories.add(".m2");
    }

    @Override
    public void checkPermission(Permission perm) {
        System.out.println("Checked");
    }

    @Override
    public void checkPropertyAccess(String key) {
        System.out.println("Checked");
    }

    @Override
    public void checkPropertiesAccess() {
        System.out.println("Checked");
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
        if (!fileCheck(file)) {
            throw new SecurityException("Access denied to " + file);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (!fileCheck(fd.toString())) {
            throw new SecurityException("Access denied to " + fd.toString());
        }
    }

    private boolean fileCheck(String filePath) {
        boolean allowedDirectory = false;
        for (String dir : allowedDirectories) {
            if (filePath.contains(dir)) {
                allowedDirectory = true;
                break;
            }
        }
        if (!allowedDirectory) {
            return false;
        }

        String[] pathParts = filePath.split("\\.");
        String fileName = pathParts[pathParts.length - 1].toLowerCase();
        final boolean isDir = (boolean) AccessController.doPrivileged((PrivilegedAction) () ->
                new File(filePath).isDirectory());

        if (isDir) {
            return true;
        }

        Pattern pattern = Pattern.compile(allowedFileTypesRegex);
        Matcher matcher = pattern.matcher(fileName);
        return matcher.matches();
    }
}
