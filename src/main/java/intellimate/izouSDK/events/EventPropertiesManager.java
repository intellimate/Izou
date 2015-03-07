package intellimate.izouSDK.events;

import intellimate.izou.system.file.FileSystemManager;
import intellimate.izou.system.file.ReloadableFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.Properties;

/**
 * EventPropertiesManager manages all events written in the local_events.properties file. You can register (add)
 * events to the file, and get events from the file. The file pretty much serves as a hub for event IDs.
 */
public class EventPropertiesManager implements ReloadableFile {

    /**
     * The path to the local_events.properties file
     */
    public static final String EVENTS_PROPERTIES_PATH = FileSystemManager.PROPERTIES_PATH + File.separator +
            "local_events.properties";
    private Properties properties;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * Creates a new EventPropertiesManager
     */
    public EventPropertiesManager() {
        properties = new Properties();
        reloadFile(null);
    }

    //TODO: call this method
    private void createIzouPropertiesFiles() throws IOException {
        String propertiesPath = new File(".").getCanonicalPath() + File.separator + "properties" + File.separator +
                "local_events.properties";

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
            //error("unable to create the local_events file", e);
        } finally {
            if(bufferedWriterInit != null)
                bufferedWriterInit.close();
        }
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
     * Registers or adds an event to the local_events.properties file
     *
     * @param description a simple description of the Event
     * @param key the key with which to store the event ID
     * @param value the complete event ID
     */
    public void registerEventID(String description, String key, String value) {
        if (getEventID(key) != null) {
            fileLogger.debug("Did not add " + key + " event ID to local_events.properties because it already exists");
            return;
        }

        BufferedWriter bufferedWriterInit = null;
        try {
            bufferedWriterInit = new BufferedWriter(new FileWriter(EVENTS_PROPERTIES_PATH, true));
        } catch (IOException e) {
            fileLogger.error("Unable to create buffered writer", e);
        }
        try {
            if (bufferedWriterInit != null) {
                bufferedWriterInit.write("\n\n" + key + "_DESCRIPTION = " + description + "\n" + key + " = " + value);
            }
        } catch (IOException e) {
            fileLogger.error("Unable to write to local_events.properties file", e);
        } finally {
            try {
                if (bufferedWriterInit != null) {
                    bufferedWriterInit.close();
                }
            } catch (IOException e) {
                fileLogger.error("Unable to close buffered writer", e);
            }
        }
    }

    /**
     * Unregisters or deletes an event from the local_events.properties file
     *
     * @param eventKey the key under which the complete event ID is stored in the properties file
     */
    public void unregisterEventID(String eventKey) {
        properties.remove(eventKey + "_DESCRIPTION");
        properties.remove(eventKey);

        BufferedWriter bufferedWriter = null;
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(EVENTS_PROPERTIES_PATH, true));
        } catch (IOException e) {
            fileLogger.error("Unable to create buffered writer", e);
        }

        try {
            if (bufferedWriter != null) {
                properties.store(bufferedWriter, null);
            }
        } catch (IOException e) {
            fileLogger.error("Unable to delete the event from the properties file", e);
        } finally {
            try {
                if (bufferedWriter != null) {
                    bufferedWriter.close();
                }
            } catch (IOException e) {
                fileLogger.error("Unable to close buffered writer", e);
            }
        }

        reloadFile(null);
    }


    @Override
    public void reloadFile(String eventType) {
        Properties temp = new Properties();
        BufferedReader in = null;
        try {
            File properties = new File(EVENTS_PROPERTIES_PATH);
            in = new BufferedReader(new InputStreamReader(new FileInputStream(properties), "UTF8"));
            temp.load(in);
            this.properties = temp;
        } catch(IOException e) {
            fileLogger.error("Error while trying to load local_events.properties", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
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
