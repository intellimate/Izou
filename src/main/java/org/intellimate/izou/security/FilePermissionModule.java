package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;
import org.intellimate.izou.system.file.FileSystemManager;

import java.io.File;
import java.io.FilePermission;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The FilePermissionModule is used to check the access to files
 * @author LeanderK
 * @version 1.0
 */
public class FilePermissionModule extends PermissionModule {
    private final List<String> allowedReadDirectories;
    private final List<String> allowedReadFiles;
    private final List<File> allowedWriteDirectories;
    private final List<String> forbiddenProperties;
    private final String allowedReadFileTypesRegex;
    private final String allowedWriteFileTypesRegex;

    /**
     * Creates a new PermissionModule
     *
     * @param main the instance of main
     */
    FilePermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
        allowedReadDirectories = new ArrayList<>();
        allowedReadFiles = new ArrayList<>();
        allowedReadFiles.add("/dev/random");
        allowedReadFiles.add("/dev/urandom");
        allowedWriteDirectories = new ArrayList<>();
        forbiddenProperties = new ArrayList<>();
        allowedReadFileTypesRegex = "(txt|properties|xml|class|json|zip|ds_store|mf|jar|idx|log|dylib|mp3|dylib|certs|"
                + "so)";
        allowedWriteFileTypesRegex = "(txt|properties|xml|json|idx|log)";
        forbiddenProperties.add("jdk.lang.process.launchmechanism");
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
        allowedReadDirectories.add(workingDir);
        allowedReadDirectories.addAll(Arrays.asList(System.getProperty("java.ext.dirs").split(":")));
        allowedReadDirectories.add(System.getProperty("java.home"));
        allowedReadDirectories.add(System.getProperty("user.home"));
        allowedWriteDirectories.add(main.getFileSystemManager().getLogsLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getPropertiesLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getResourceLocation());
    }

    /**
     * returns true if able to check permissions
     *
     * @param permission the permission to check
     * @return true if able to, false if not
     */
    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof FilePermission;
    }

    /**
     * Checks if the given addOn is allowed to access the requested service and registers them if not yet registered.
     *
     * @param permission the Permission to check
     * @param addon      the identifiable to check
     * @throws IzouPermissionException thrown if the addOn is not allowed to access its requested service
     */
    @Override
    public void checkPermission(Permission permission, AddOnModel addon) throws IzouPermissionException {
        String canonicalName = permission.getName().intern().toLowerCase();
        String canonicalAction =  permission.getActions().intern().toLowerCase();

        if (canonicalName.contains("all files") || canonicalAction.equals("execute")) {
            throw getException(permission.getName());
        }
        if (canonicalAction.equals("read")) {
            fileReadCheck(canonicalName);
        }
        fileWriteCheck(canonicalName);
    }
}
