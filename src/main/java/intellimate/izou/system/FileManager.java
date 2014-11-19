package intellimate.izou.system;

import intellimate.izou.addon.AddOn;

import static java.nio.file.StandardWatchEventKinds.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The file manager listens for events that were caused by modifications made to property files and
 * then reloads the properties file
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
     * @param dir directory of properties file
     * @param addOn addOn properties file belongs to
     * @throws IOException exception thrown by watcher service
     */
    public void registerFileDir(Path dir, String fileType, AddOn addOn) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        addOnMap.put(key, new FileInfo(dir, fileType, addOn));
    }

    /**
     *
     *
     * @param dir
     * @param fileType
     * @param addOn
     * @param reloadableFiles
     * @throws IOException
     */
    public void registerFileDir(Path dir, String fileType, AddOn addOn, ReloadableFiles reloadableFiles) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        addOnMap.put(key, new FileInfo(dir, fileType, addOn, reloadableFiles));
    }

    /**
     *
     * @param dir
     * @param fileType
     * @param reloadableFiles
     * @throws IOException
     */
    public void registerFileDir(Path dir, String fileType, ReloadableFiles reloadableFiles) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        addOnMap.put(key, new FileInfo(dir, fileType, reloadableFiles));
    }

    /**
     * checks if an event belongs to a properties file
     * @param event the event to check
     * @return the boolean value corresponding to the output
     */
    private boolean isFileType(WatchEvent event, String fileType) {
        return event.context().toString().endsWith(fileType);
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

