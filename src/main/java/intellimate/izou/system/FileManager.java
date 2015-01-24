package intellimate.izou.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * The file manager listens for events that were caused by modifications made to property files and
 * then reloads the file.
 *
 * You can register an {@code AddOn} or a {@code ReloadableFile} with the path to the directory it is supposed to watch
 */
public class FileManager implements Runnable {
    /**
     * Default java watching service for directories, raises events when changes happen in this directory
     */
    private WatchService watcher;

    /**
     * Map that holds watchKeys (ID's) of the directories and the addOns using the directories
     */
    private Map<WatchKey, List<FileInfo>> addOnMap;
    private FilePublisher filePublisher;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * Creates a new FileManager with a watcher and addOnMap
     *
     * @throws IOException exception is thrown by watcher service
     */
    public FileManager(FilePublisher filePublisher) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        addOnMap = new HashMap<>();
        this.filePublisher =  filePublisher;
        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * Use this method to register a file with the watcherService
     *
     * @param dir directory of file
     * @param fileType the name/extension of the file
     *                 IMPORTANT: Please try to always enter the full name with extension of the file (Ex: "test.txt"),
     *                 it would be best if the fileType is the full file name, and that the file name is clearly
     *                 distinguishable from other files.
     *                 For example, the property files are stored with the ID of the addon they belong too. That way
     *                 every property file is easily distinguishable.
     * @param reloadableFile object of interface that file belongs to
     * @throws IOException exception thrown by watcher service
     */
    public void registerFileDir(Path dir, String fileType, ReloadableFile reloadableFile) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        List<FileInfo> fileInfos = addOnMap.get(key);

        if(fileInfos != null) {
            fileInfos.add(new FileInfo(dir, fileType, reloadableFile));
        } else {
            fileInfos = new ArrayList<>();
            fileInfos.add(new FileInfo(dir, fileType, reloadableFile));
            addOnMap.put(key, fileInfos);
        }
    }

    /**
     * Checks if an event belongs to the desired file type
     *
     * @param event the event to check
     * @return the boolean value corresponding to the output
     */
    private boolean isFileType(WatchEvent event, String fileType) {
        return event.context().toString().contains(fileType);
    }

    /**
     * Writes default file to real file
     * The default file would be a file that can be packaged along with the code, from which a real file (say a
     * properties file for example) can be loaded. This is useful because there are files (like property files0 that
     * cannot be shipped with the package and have to be created at runtime. To still be able to fill these files, you
     * can create a default file (usually txt) from which the content, as mentioned above, can then be loaded into the
     * real file.
     *
     * @param defaultFilePath path to default file (or where it should be created)
     * @param realFilePath path to real file (that should be filled with content of default file)
     * @return true if operation has succeeded, else false
     */
    public boolean writeToFile(String defaultFilePath, String realFilePath) {
        try {
            Files.copy(Paths.get(defaultFilePath), Paths.get(realFilePath), StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            fileLogger.error("Unable to write to copy Properties-File", e);
            return false;
        }
        /*
        boolean outcome = true;

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(defaultFilePath));
            bufferedWriter = new BufferedWriter(new FileWriter(realFilePath));
            String line;

            // c is the character read from bufferedReader and written to bufferedWriter
            int c = 0;
            if (bufferedReader.ready()) {
                while (c != -1) {
                    c = bufferedReader.read();
                    if (!(c == (byte)'\uFFFF')) {
                        bufferedWriter.write(c);
                    }
                }
            }

        } catch (IOException e) {
            fileLogger.error("Unable to write to the Properties-File", e);
            outcome =  false;
        } finally {
            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    fileLogger.error(e);                }
            if (bufferedWriter != null)
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    fileLogger.error(e);
                }
        }
        return outcome;
        */
    }

    /**
     * Creates a default File in case it does not exist yet. Default files can be used to load other files that are
     * created at runtime (like properties file)
     *
     * @param defaultFilePath path to default file.txt (or where it should be created)
     * @param initMessage the string to write in default file
     * @throws IOException is thrown by bufferedWriter
     */
    public void createDefaultFile(String defaultFilePath, String initMessage) throws IOException {
        File file = new File(defaultFilePath);
        BufferedWriter bufferedWriterInit = null;
        try {
            if (!file.exists()) {
                file.createNewFile();
                bufferedWriterInit = new BufferedWriter(new FileWriter(defaultFilePath));
                bufferedWriterInit.write(initMessage);
            }
        } catch (IOException e) {
            fileLogger.error("unable to create the Default-File", e);
        } finally {
            if(bufferedWriterInit != null) {
                try {
                    bufferedWriterInit.close();
                } catch (IOException e) {
                    fileLogger.error("Unable to close input stream", e);
                }
            }
        }
    }

    /**
     * Checks if {@code fileInfo} and {@code key} match each other, in which case the fileInfo and key are processed
     *
     * @param key current key
     * @param fileInfos all fileInfos that match key
     */
    private void checkAndProcessFileInfo(WatchKey key, List<FileInfo> fileInfos) {
        for (WatchEvent<?> event : key.pollEvents()) {
            WatchEvent.Kind kind = event.kind();

            for (FileInfo fileInfo : fileInfos) {
                if (kind == OVERFLOW) {
                    try {
                        throw new IncompleteFileEventException();
                    } catch (IncompleteFileEventException e) {
                        fileLogger.warn(e);
                    }
                } else if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY || kind == ENTRY_DELETE)
                        && isFileType(event, fileInfo.getFileType())) {
                    try {
                        if (fileInfo.getReloadableFile() != null) {
                            fileInfo.getReloadableFile().reloadFile(kind.toString());
                            fileLogger.debug("Reloaded file: " + event.context().toString());
                            filePublisher.notifyFileSubcribers(fileInfo.getReloadableFile());
                        }
                    } catch (Exception e) {
                        fileLogger.warn(e);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        fileLogger.warn(e);
                    }
                }
            }
        }
    }

    /**
     * Main method of fileManager, it constantly waits for new events and then processes them
     */
    @Override
    public void run() {
        while(true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                fileLogger.warn(e);
                continue;
            }

            List<FileInfo> fileInfos = addOnMap.get(key);
            checkAndProcessFileInfo(key, fileInfos);

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                addOnMap.remove(key);

                // all directories are inaccessible
                if (addOnMap.isEmpty()) {
                    break;
                }
            }
        }
    }

    /**
    * Exception thrown if there are multiple Events fired at the same time.
    */
    @SuppressWarnings("WeakerAccess")
    public class IncompleteFileEventException extends Exception {
        public IncompleteFileEventException() {
            super("Fired file event has been lost or discarded");
        }
    }
}

