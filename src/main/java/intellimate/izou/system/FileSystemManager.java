package intellimate.izou.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * FileSystemManager is responsible for creating all the files and folders Izou needs to operate
 */
public class FileSystemManager {
    /**
     * Path to log files
     */
    public static final String LOG_PATH = "." + File.separator + "logs" + File.separator;

    /**
     *
     */
    public static final String PROPERTIES_PATH = "." + File.separator + "properties" + File.separator;
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
        createIzouPropertiesFiles();
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

    private void createIzouPropertiesFiles() throws IOException {
        String propertiesPath = new File(".").getCanonicalPath() + File.separator + "properties" + File.separator +
                "PopularEvents.properties";

        File file = new File(propertiesPath);
        BufferedWriter bufferedWriterInit = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                bufferedWriterInit = new BufferedWriter(new FileWriter(propertiesPath));
                bufferedWriterInit.write("# You can use this file to store an event ID with a key, or shortcut, " +
                        " so that others can easily access and\n# fire it using the key");
            }
        } catch (IOException e) {
            fileLogger.error("unable to create the Default-File", e);
        } finally {
            if(bufferedWriterInit != null)
                bufferedWriterInit.close();
        }
    }
}
