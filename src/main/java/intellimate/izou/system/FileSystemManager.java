package intellimate.izou.system;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileSystemManager {
    public static final String LOG_PATH = "." + File.separator + "logs" + File.separator;

    /**
     *
     */
    public FileSystemManager() {
    }

    /**
     *
     * @throws IOException
     */
    public void createIzouFileSystem() throws IOException {
        createLibFolder();
        createResourceFolder();
        createPropertiesFolder();
        createLogsFolder();
    }

    /**
     *
     * @throws IOException
     */
    private void createLibFolder() throws IOException {
        String libPath = new File(".").getCanonicalPath() + File.separator + "lib";
        File libFile = new File(libPath);
        if(!Files.exists(libFile.toPath()))
            Files.createDirectories(libFile.toPath());
    }

    /**
     *
     * @throws IOException
     */
    private void createResourceFolder() throws IOException {
        String resourcePath = new File(".").getCanonicalPath() + File.separator + "resources";
        File resourceFile = new File(resourcePath);
        if(!Files.exists(resourceFile.toPath()))
            Files.createDirectories(resourceFile.toPath());
    }

    /**
     *
     * @throws IOException
     */
    private void createPropertiesFolder() throws IOException {
        String propertiesPath = new File(".").getCanonicalPath() + File.separator + "properties";
        File propertiesDir = new File(propertiesPath);
        if(!Files.exists(propertiesDir.toPath()))
            Files.createDirectories(propertiesDir.toPath());
    }

    /**
     *
     * @throws IOException
     */
    private void createLogsFolder() throws IOException {
        String logPath = new File(".").getCanonicalPath() + File.separator + "logs";
        File logFile = new File(logPath);
        if(!Files.exists(logFile.toPath()))
            Files.createDirectories(logFile.toPath());

        String logPath2 = new File(".").getCanonicalPath() + File.separator + "logs" + File.separator + "logs";
        File logFile2 = new File(logPath2);
        if(!Files.exists(logFile2.toPath()))
            Files.createDirectories(logFile2.toPath());

        String logPropertiesPath = logPath + File.separator + "IzouLoggingProperties.properties";
        File logPropFile = new File(logPropertiesPath);
        if(!logPropFile.exists())
            logPropFile.createNewFile();
    }


}
