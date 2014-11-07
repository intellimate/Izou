package intellimate.izou.addon;

import static java.nio.file.StandardWatchEventKinds.*;
import java.io.IOException;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by julianbrendl on 11/7/14.
 */
public class PropertiesManager implements Runnable {
    private WatchService watcher;
    private Map<WatchKey,Path> keys;

    public PropertiesManager() throws IOException {
        watcher = FileSystems.getDefault().newWatchService();
        keys = new HashMap<>();
    }

    public void registerProperty(Path dir) throws IOException {
        WatchKey key = dir.register(watcher, ENTRY_MODIFY);
        keys.put(key, dir);
    }


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
            if (dir == null) {
                //TODO implement error handling
            }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                if (kind == OVERFLOW) {
                    System.out.println("overflow in file events");
                } else if (kind == ENTRY_MODIFY) {
                    System.out.println("file was changed");
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
