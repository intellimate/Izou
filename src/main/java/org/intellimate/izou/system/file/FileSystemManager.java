package org.intellimate.izou.system.file;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.main.Main;

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
    private static final Logger logger = LogManager.getLogger(FileSystemManager.class);
    public static final String LOG_PATH = "." + File.separator + "logs" + File.separator;
    public static final String FULL_WORKING_DIRECTORY;
    static {
        String path = null;
        try {
            path = new File(".").getCanonicalPath();
        } catch (IOException e) {
            logger.error("Unable to get current canonical path", e);
        }
        FULL_WORKING_DIRECTORY = path;
    }

    private final File izouLocation;
    private final File libLocation;
    private final File resourceLocation;
    private final File propertiesLocation;
    private final File logsLocation;

    /**
     * creates a file system manager
     * @param main an instance of main
     */
    public FileSystemManager(Main main) {
        super(main);
        try {
            File baseLocation;
            if (Boolean.getBoolean("debug")) {
                baseLocation = new File(".");
                izouLocation = baseLocation;
            } else {
                baseLocation = new File(FileSystemManager.class.getProtectionDomain()
                        .getCodeSource().getLocation().toURI().getPath());
                izouLocation = baseLocation.getParentFile();
            }
            libLocation = new File(izouLocation.toString() + File.pathSeparator + "lib");
            resourceLocation = new File(izouLocation.toString() + File.pathSeparator + "resources");
            propertiesLocation = new File(izouLocation.toString() + File.pathSeparator + "properties");
            logsLocation = new File(izouLocation.toString() + File.pathSeparator + "logs");
        } catch (URISyntaxException e) {
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

    public File getIzouLocation() {
        return izouLocation;
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
}
