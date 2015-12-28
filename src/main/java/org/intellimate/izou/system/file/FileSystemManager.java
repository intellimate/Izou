package org.intellimate.izou.system.file;

import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;

/**
 * FileSystemManager is responsible for creating all the files and folders Izou needs to operate
 */
public class FileSystemManager extends IzouModule {
    /**
     * Path to log files
     */
    private final File izouParentLocation;
    private final File izouJarLocation;
    private final File libLocation;
    private final File resourceLocation;
    private final File propertiesLocation;
    private final File logsLocation;
    private final File systemLocation;
    private final File systemDataLocation;

    /**
     * Creates a file system manager
     *
     * @param main an instance of main
     */
    public FileSystemManager(Main main) {
        super(main);
        try {
            izouJarLocation = new File(FileSystemManager.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI().getPath());
            if (Boolean.getBoolean("debug")) {
                izouParentLocation = new File(".");
            } else {
                izouParentLocation = izouJarLocation.getParentFile();
            }
            libLocation = new File(izouParentLocation.toString() + File.separator + "lib").getCanonicalFile();
            resourceLocation = new File(izouParentLocation.toString() + File.separator + "resources").getCanonicalFile();
            propertiesLocation = new File(izouParentLocation.toString() + File.separator + "properties").getCanonicalFile();
            logsLocation = new File(izouParentLocation.toString() + File.separator + "logs").getCanonicalFile();
            systemLocation = new File(izouParentLocation.toString() +  File.separator + "system").getCanonicalFile();
            systemDataLocation = new File(izouParentLocation.toString() +  File.separator + "system"
                    + File.separator + "data").getCanonicalFile();
        } catch (URISyntaxException | IOException e) {
            error("unable to create the Izou-file system");
            throw new IllegalStateException("unable to create the Izou-file system");
        }
    }

    /**
     * creates Izou file-system
     *
     * @throws IOException could throw IOException because working with files
     */
    public void createIzouFileSystem() throws IOException {
        createLibFolder();
        createResourceFolder();
        createPropertiesFolder();
        createLogsFolder();
        createSystemFolder();
        createSystemDataFolder();
    }

    /**
     * create lib folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createLibFolder() throws IOException {
        if(!Files.exists(libLocation.toPath()))
            Files.createDirectories(libLocation.toPath());
    }

    /**
     * create resource folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createResourceFolder() throws IOException {
        if(!Files.exists(resourceLocation.toPath()))
            Files.createDirectories(resourceLocation.toPath());
    }

    /**
     * create properties folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createPropertiesFolder() throws IOException {
        if(!Files.exists(propertiesLocation.toPath()))
            Files.createDirectories(propertiesLocation.toPath());
    }

    /**
     * Create logs folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createLogsFolder() throws IOException {
        if(!Files.exists(logsLocation.toPath()))
            Files.createDirectories(logsLocation.toPath());
    }

    /**
     * Create system folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createSystemFolder() throws IOException {
        if(!Files.exists(systemLocation.toPath()))
            Files.createDirectories(systemLocation.toPath());
    }

    /**
     * Create system/data folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createSystemDataFolder() throws IOException {
        if(!Files.exists(systemDataLocation.toPath()))
            Files.createDirectories(systemDataLocation.toPath());
    }

    public File getIzouParentLocation() {
        return izouParentLocation;
    }

    public File getLibLocation() {
        return libLocation;
    }

    public File getResourceLocation() {
        return resourceLocation;
    }

    public File getPropertiesLocation() {
        return propertiesLocation;
    }

    public File getLogsLocation() {
        return logsLocation;
    }

    public File getIzouJarLocation() {
        return izouJarLocation;
    }

    public File getSystemLocation() {
        return systemLocation;
    }

    public File getSystemDataLocation() {
        return systemDataLocation;
    }
}
