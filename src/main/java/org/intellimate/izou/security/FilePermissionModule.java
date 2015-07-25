package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;

import java.io.File;
import java.io.FilePermission;
import java.io.IOException;
import java.net.URL;
import java.security.Permission;
import java.util.ArrayList;
import java.util.List;

/**
 * The FilePermissionModule is used to check the access to files
 * @author LeanderK
 * @version 1.0
 */
public class FilePermissionModule extends PermissionModule {
    private final List<String> forbiddenReadFiles;
    private final List<File> allowedWriteDirectories;
    private final List<File> forbiddenWriteDirectories;
    private final List<String> forbiddenWriteFilesNames;

    /**
     * Creates a new PermissionModule
     *
     * @param main the instance of main
     */
    FilePermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
        forbiddenReadFiles = new ArrayList<>();
        forbiddenWriteDirectories = new ArrayList<>();
        forbiddenWriteFilesNames = new ArrayList<>();
        forbiddenWriteFilesNames.add("addon_config.properties");
        allowedWriteDirectories = new ArrayList<>();
        allowedWriteDirectories.add(main.getFileSystemManager().getResourceLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getLogsLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getPropertiesLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getResourceLocation());
        allowedWriteDirectories.add(main.getFileSystemManager().getLibLocation());
        if (getMain().getFileSystemManager().getIzouJarLocation().isDirectory()) {
            allowedWriteDirectories.add(new File(getMain().getFileSystemManager().getIzouJarLocation() + File.separator + "META-INF" + File.separator + "services"));
        } else {
            URL metaInfo = this.getClass().getClassLoader().getResource("META-INF/services");
            if (metaInfo != null)
                allowedWriteDirectories.add(new File(metaInfo.getFile()));
        }

        if (Boolean.getBoolean("debug")) {
            allowedWriteDirectories.add(new File(System.getProperty("user.home") + File.separator + ".m2"));
            allowedWriteDirectories.add(main.getFileSystemManager().getIzouJarLocation());
            allowedWriteDirectories.add(new File(System.getProperty("java.home")));
        }
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
        fileWriteCheck(canonicalName, addon);
    }

    /**
     * Determines if the file at the given file path is safe to read from in all aspects, if so returns true, else false
     *
     * @param filePath the path to the file to read from
     */
    void fileReadCheck(String filePath) {
        File potentialFile = new File(filePath);
        String canonicalPath;
        try {
            canonicalPath = potentialFile.getCanonicalPath();
        } catch (IOException e) {
            error("Error getting canonical path", e);
            throw getException(filePath);
        }

        if (forbiddenReadFiles.stream().anyMatch(canonicalPath::startsWith)) {
            throw getException(filePath);
        }
    }

    /**
     * Determines if the file at the given file path is safe to write to in all aspects, if so returns true, else false
     *
     * @param filePath the path to the file to write to
     * @param addOnModel the AddonModel
     */
    void fileWriteCheck(String filePath, AddOnModel addOnModel) {
        File request;
        try {
            request =  new File(filePath).getCanonicalFile();
        } catch (IOException e) {
            error("Error getting canonical path", e);
            throw getException(filePath);
        }

        isForbidden(request, addOnModel);

        boolean success = false;
        if (allowedWriteDirectories.stream()
                .anyMatch(compare -> request.toPath().startsWith(compare.toPath()))) {
            success = true;
        }

        for (String name : forbiddenWriteFilesNames) {
            if (request.getName().equals(name)) {
                success = false;
            }
        }

        if (!success) {
            throw getException(filePath);
        }

        if (!getSecurityManager().getSecureAccess().checkForExistingFileOrDirectory(request.toString())
                || getSecurityManager().getSecureAccess().checkForDirectory(request.toString())) {
            return;
        }
    }

    /**
     * throws an Exception if the
     * @param request the requested File
     * @param addOnModel the AddonModel
     */
    private void isForbidden(File request, AddOnModel addOnModel) {
        if (forbiddenWriteDirectories.stream()
                .anyMatch(compare -> request.toPath().startsWith(compare.toPath()))) {
            throw getException("file: " + request.toString() + " is forbidden. Attempt made by: "
                    + addOnModel.getID());
        }
    }
}
