package org.intellimate.izou.security;

import org.intellimate.izou.system.file.FileSystemManager;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FilePermission;
import java.security.Permission;
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
    private final List<String> allowedReadDirectories;
    private final List<String> allowedWriteDirectories;
    private final String allowedReadFileTypesRegex;
    private final String allowedWriteFileTypesRegex;
    private boolean tempAccess = false;

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
        allowedReadDirectories = new ArrayList<>();
        allowedWriteDirectories = new ArrayList<>();
        allowedReadFileTypesRegex = "(txt|properties|xml|class|json|zip|ds_store|mf|jar|idx)";
        allowedWriteFileTypesRegex = "(txt|properties|xml|json|idx)";
        init();
    }

    private void init() {
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
        while (!workingDir.endsWith(File.separator + "Izou" + File.separator)) {
            workingDir = workingDir.substring(0, workingDir.length() - 1);
        }

        allowedReadDirectories.add(workingDir);
        allowedReadDirectories.add("." + File.separator);
        allowedReadDirectories.add(".m2");

        allowedWriteDirectories.add(workingDir);
        allowedWriteDirectories.add("." + File.separator);
    }

    @Override
    public void checkPermission(Permission perm) {
        boolean filePermission = checkFilePermission(perm);
        if (!filePermission) {
            throw new SecurityException("Access denied to " + perm.getName());
        }
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
        if (!tempAccess && !fileReadCheck(file)) {
            throw new SecurityException("Access denied to " + file);
        }
    }

    @Override
    public void checkWrite(FileDescriptor fd) {
        if (!tempAccess && !fileWriteCheck(fd.toString())) {
            throw new SecurityException("Access denied to " + fd.toString());
        }
    }

    /**
     * Checks if the permission {@code perm} is a {@link FilePermission} and if so checks is the IO operation is
     * allowed, else returns false. If the permission is not a FilePermission, true is returned as the permission has
     * nothing to do with file IO.
     *
     * @param perm the permission to check
     * @return true if permission is granted from the file IO side
     */
    private boolean checkFilePermission(Permission perm) {
        if (!(perm instanceof FilePermission)) {
            return true;
        }

        if (perm.getActions().equals("read")) {
            return fileReadCheck(perm.getName());
        }

        // If read permission is not asked, default to write permission check, which grants less rights
        return fileWriteCheck(perm.getName());

    }

    private boolean fileReadCheck(String filePath) {
        boolean allowedDirectory = false;
        for (String dir : allowedReadDirectories) {
            if (filePath.contains(dir)) {
                allowedDirectory = true;
                break;
            }
        }
        if (!allowedDirectory) {
            return false;
        }

        String[] pathParts = filePath.split("\\.");
        String fileExtension = pathParts[pathParts.length - 1].toLowerCase();

        tempAccess = true;
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            tempAccess = false;
            return true;
        }
        tempAccess = false;

        Pattern pattern = Pattern.compile(allowedReadFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        return matcher.matches();
    }

    private boolean fileWriteCheck(String filePath) {
        boolean allowedDirectory = false;
        for (String dir : allowedWriteDirectories) {
            if (filePath.contains(dir)) {
                allowedDirectory = true;
                break;
            }
        }
        if (!allowedDirectory) {
            return false;
        }

        String[] pathParts = filePath.split("\\.");
        String fileExtension = pathParts[pathParts.length - 1].toLowerCase();

        tempAccess = true;
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            tempAccess = false;
            return true;
        }
        tempAccess = false;

        Pattern pattern = Pattern.compile(allowedWriteFileTypesRegex);
        Matcher matcher = pattern.matcher(fileExtension);
        return matcher.matches();
    }
}
