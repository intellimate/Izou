package org.intellimate.izou.system.file;

import org.intellimate.izou.IzouModule;
import org.intellimate.izou.main.Main;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

/**
 * FileSystemManager is responsible for creating all the files and folders Izou needs to operate
 */
public class FileSystemManager extends IzouModule {
    /**
     * Path to log files
     */
    public static final String LOG_PATH = "." + File.separator + "logs" + File.separator;
    public static final String FULL_WORKING_DIRECTORY;
    static {
        String path = null;
        try {
            path = new File(".").getCanonicalPath();
        } catch (IOException e) {
            // do whatever you have to do
        }
        FULL_WORKING_DIRECTORY = path;
    }

    /**
     * path to the proterties files
     */
    public static final String PROPERTIES_PATH = "." + File.separator + "properties" + File.separator;

    /**
     * creates a file system manager
     * @param main an instance of main
     */
    public FileSystemManager(Main main) {
        super(main);
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
        String libPath = new File(".").getCanonicalPath() + File.separator + "lib";
        File libFile = new File(libPath);
        if(!Files.exists(libFile.toPath()))
            Files.createDirectories(libFile.toPath());
    }

    /**
     * create resource folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createResourceFolder() throws IOException {
        String resourcePath = new File(".").getCanonicalPath() + File.separator + "resources";
        File resourceFile = new File(resourcePath);
        if(!Files.exists(resourceFile.toPath()))
            Files.createDirectories(resourceFile.toPath());
    }

    /**
     * create properties folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createPropertiesFolder() throws IOException {
        String propertiesPath = new File(".").getCanonicalPath() + File.separator + "properties";
        File propertiesDir = new File(propertiesPath);
        if(!Files.exists(propertiesDir.toPath()))
            Files.createDirectories(propertiesDir.toPath());
    }

    /**
     * Create logs folder
     *
     * @throws IOException could throw IOException because working with files
     */
    private void createLogsFolder() throws IOException {
        String logPath = new File(".").getCanonicalPath() + File.separator + "logs";
        File logFile = new File(logPath);
        if(!Files.exists(logFile.toPath()))
            Files.createDirectories(logFile.toPath());
    }
}
