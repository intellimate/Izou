package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.*;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The file manager listens for events that were caused by modifications made to property files and
 * then reloads the file
 */
public class FileManager implements Runnable {
    /**
     * default java watching service for directories, raises events when changes happen in this directory
     */
    private WatchService watcher;

    /**
     * Map that holds watchKeys (ID's) of the directories and the addOns using the directories
     */
    private Map<WatchKey, FileInfo> addOnMap;

    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * creates a new FileManager with a watcher and addOnMap
     *
     * @throws IOException exception is thrown by watcher service
     */
    public FileManager() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        addOnMap = new HashMap<>();

        Thread thread = new Thread(this);
        thread.start();
    }

    /**
     * use this method to register a file with the watcherService
     *
     * @param dir directory of the file
     * @param fileType the name/extension of the file
     *                 IMPORTANT: Please try to always enter the full name with extension of the file (Ex: "test.txt")
     * @param addOn addOn the file belongs to
     * @throws IOException exception thrown by watcher service
     */
    public void registerFileDir(Path dir, String fileType, AddOn addOn) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        addOnMap.put(key, new FileInfo(dir, fileType, addOn));
    }

    /**
     * use this method to register a file with the watcherService
     *
     * @param dir directory of file
     * @param fileType the name/extension of the file
     *                 IMPORTANT: Please try to always enter the full name with extension of the file (Ex: "test.txt")
     * @param addOn addOn that file belongs to
     * @param reloadableFiles object of interface that file belongs to
     * @throws IOException exception thrown by watcher service
     */
    public void registerFileDir(Path dir, String fileType, AddOn addOn, ReloadableFiles reloadableFiles) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        addOnMap.put(key, new FileInfo(dir, fileType, addOn, reloadableFiles));
    }

    /**
     * use this method to register a file with the watcherService
     *
     * @param dir directory of file
     * @param fileType the name/extension of the file
     *                 IMPORTANT: Please try to always enter the full name with extension of the file (Ex: "test.txt")
     * @param reloadableFiles object of interface that file belongs to
     * @throws IOException exception thrown by watcher service
     */
    public void registerFileDir(Path dir, String fileType, ReloadableFiles reloadableFiles) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        addOnMap.put(key, new FileInfo(dir, fileType, reloadableFiles));
    }

    /**
     * checks if an event belongs to the desired file type
     * @param event the event to check
     * @return the boolean value corresponding to the output
     */
    private boolean isFileType(WatchEvent event, String fileType) {
        return event.context().toString().endsWith(fileType);
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
        boolean outcome = true;

        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try {
            bufferedReader = new BufferedReader(new FileReader(defaultFilePath));
            bufferedWriter = new BufferedWriter(new FileWriter(realFilePath));

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
            e.printStackTrace();
            outcome =  false;
        } finally {
            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            if (bufferedWriter != null)
                try {
                    bufferedWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
        return outcome;
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
            e.printStackTrace();
        } finally {
            if(bufferedWriterInit != null)
                bufferedWriterInit.close();
        }
    }

    /**
     * main method of fileManager, it constantly waits for new events and then processes them
     */
    @Override
    public void run() {
        while(true) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException e) {
                return;
            }

            FileInfo fileInfo = addOnMap.get(key);
            if (fileInfo.getPath() == null) {
                //TODO implement error handling
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    try {
                        throw new IncompletePropertyEventException();
                    } catch (IncompletePropertyEventException e) {
                        e.printStackTrace();
                    }
                } else if ((kind == ENTRY_CREATE || kind == ENTRY_MODIFY || kind == ENTRY_DELETE)
                        && isFileType(event, fileInfo.getFileType())) {
                    try {
                        fileInfo.getAddOn().reloadFiles();
                        if(fileInfo.getReloadableFiles() != null)
                            fileInfo.getReloadableFiles().reloadFile(kind.toString());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

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
    }

    /**
    * Exception thrown if there are multiple Events fired at the same time.
    */
    @SuppressWarnings("WeakerAccess")
    public class IncompletePropertyEventException extends Exception {
        public IncompletePropertyEventException() {
            super("Fired property event has been lost or discarded");
        }
    }
}

