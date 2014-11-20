package intellimate.izou.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * FileSystemManager is responsible for creating all the files and folders Izou needs to operate
 */
public class FileSystemManager {
    public static final String LOG_PATH = "." + File.separator + "logs" + File.separator;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * creates a file system manager
     */
    public FileSystemManager() {
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
     * create logs folder
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
