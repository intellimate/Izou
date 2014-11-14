package intellimate.izou.addon;

import org.w3c.dom.events.EventException;

import static java.nio.file.StandardWatchEventKinds.*;

import java.awt.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * The properties manager listens for events that were caused by modifications made to property files and
 * then reloads the properties file
 */
public class PropertiesManager implements Runnable {
    /**
     * default java watching service for directories, raises events when changes happen in this directory
     */
    private WatchService watcher;

    /**
     * Map that holds watchKeys (ID's) of the directories and paths to the directories themselves
     */
    private Map<WatchKey,Path> keys;

    /**
     * Map that holds watchKeys (ID's) of the directories and the addOns using the directories
     */
    private Map<WatchKey, AddOn> addOnMap;

    /**
     * creates a new PropertiesManager with a watcher, keys Map and addOnMap
     *
     * @throws IOException exception is thrown by watcher service
     */
    public PropertiesManager() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        keys = new HashMap<>();
        addOnMap = new HashMap<>();
    }

    /**
     * use this method to register a properties file with the watcherService
     *
     * @param dir directory of properties file
     * @param addOn addOn properties file belongs to
     * @throws IOException exception thrown by watcher service
     */
    public void registerProperty(Path dir, AddOn addOn) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        keys.put(key, dir);
        addOnMap.put(key, addOn);
    }

    /**
     * checks if an event belongs to a properties file
     * @param event the event to check
     * @return the boolean value corresponding to the output
     */
    private boolean isProperty(WatchEvent event) {
        return event.context().toString().endsWith("properties");
    }

    /**
     * main method of propertiesManager, it constantly waits for new events and then processes them
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

            Path dir = keys.get(key);
            synchronized (dir) {
                if (dir == null) {
                    //TODO implement error handling
                }

                for (WatchEvent<?> event : key.pollEvents()) {
                    WatchEvent.Kind kind = event.kind();

                    if (kind == OVERFLOW) {
                        try {
                            throw new IncompleteEventException();
                        } catch (IncompleteEventException e) {
                            e.printStackTrace();
                        }
                    } else if ((kind == ENTRY_MODIFY) && isProperty(event)) {
                        AddOn addOn = addOnMap.get(key);
                        try {
                            addOn.reloadProperties();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }

                // reset key and remove from set if directory no longer accessible
                boolean valid = key.reset();
                if (!valid) {
                    keys.remove(key);

                    // all directories are inaccessible
                    if (keys.isEmpty()) {
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
    public class IncompleteEventException extends Exception {
        public IncompleteEventException() {
            super("Fired event has been lost or discarded");
        }
    }
}

