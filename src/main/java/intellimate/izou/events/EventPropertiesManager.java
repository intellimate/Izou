package intellimate.izou.events;

import intellimate.izou.system.FileSystemManager;
import intellimate.izou.system.ReloadableFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * EventPropertiesManager manages all events written in the PopularEvents.properties file. You can register (add)
 * events to the file, and get events from the file. The file pretty much serves as a hub for event IDs.
 */
public class EventPropertiesManager implements ReloadableFile {
    /**
     * The path to the PopularEvents.properties file
     */
    public static final String EVENTS_PROPERTIES_PATH = FileSystemManager.PROPERTIES_PATH + File.separator +
            "PopularEvents.properties";
    private Properties properties;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * Creates a new EventPropertiesManager
     */
    public EventPropertiesManager() {
        properties = new Properties();
    }

    /**
     * Gets the full event ID associated with the key {@code key}
     *
     * @param key the key of the full event ID
     * @return the complete the event ID, or null if none is found
     */
    public String getEventID(String key) {
        return (String) properties.get(key);
    }

    /**
     * Registers or adds an event to the PopularEvents.properties file
     *
     * @param key the key with which to store the event ID
     * @param value the complete event ID
     */
    public void registerEventID(String key, String value) {
        BufferedWriter bufferedWriterInit = null;
        try {
            bufferedWriterInit = new BufferedWriter(new FileWriter(EVENTS_PROPERTIES_PATH));
        } catch (IOException e) {
            fileLogger.error("Unable to create buffered writer", e);
        }
        try {
            if (bufferedWriterInit != null) {
                bufferedWriterInit.write("\n" + key + ":" + value);
            }
        } catch (IOException e) {
            fileLogger.error("Unable to write to PopularEvents.properties file", e);
        }
    }

    @Override
    public void reloadFile(String eventType) {
        Properties temp = new Properties();
        InputStream inputStream = null;
        try {
            File properties = new File(EVENTS_PROPERTIES_PATH);
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(properties), "UTF8"));
            temp.load(in);
            this.properties = temp;
        } catch(IOException e) {
            fileLogger.error("Error while trying to load PopularEvents.properties", e);
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    fileLogger.error("Unable to close input stream", e);
                }
            }
        }
    }

    @Override
    public String getID() {
        return null;
    }
}
