package karlskrone.jarvis.addon;

import karlskrone.jarvis.activator.Activator;
import karlskrone.jarvis.contentgenerator.ContentGenerator;
import karlskrone.jarvis.events.EventController;
import karlskrone.jarvis.output.OutputExtension;
import karlskrone.jarvis.output.OutputPlugin;
import ro.fortsoft.pf4j.ExtensionPoint;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A AddOns must extend this Class.
 *
 * It will be instantiated and its registering-methods will be called by the PluginManager.
 * This class has method for a properties-file named addOnID.properties (AddOnsID in the form: package.class)
 */
public abstract class AddOn implements ExtensionPoint {
    private final Properties properties;
    private final String addOnID;


    /**
     * the default constructor for AddOns
     * @param addOnID the ID of the Plugin in the form: package.class
     */
    public AddOn(String addOnID) {
        this.addOnID = addOnID;

        properties = new Properties();
        String propFileName = addOnID + ".properties";

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            //TODO: log exception
            e.printStackTrace();
        }
    }

    /**
     * use this method to build your instances etc.
     */
    public abstract void prepare();
    /**
     * use this method to register (if needed) your Activators.
     */
    public abstract Activator[] registerActivator();

    /**
     * use this method to register (if needed) your ContentGenerators.
     */
    public abstract ContentGenerator[] registerContentGenerator();

    /**
     * use this method to register (if needed) your EventControllers.
     */
    public abstract EventController[] registerEventController();

    /**
     * use this method to register (if needed) your OutputPlugins.
     */
    public abstract OutputPlugin[] registerOutputPlugin();

    /**
     * use this method to register (if needed) your Output.
     */
    public abstract OutputExtension[] registerOutputExtension();

    /**
     * The AddOns-Id is usually the following Form: package.class
     * @return a String containing the PluginID
     */
    public String getAddOnID() {
        return addOnID;
    }

    /**
     * Searches for the property with the specified key in this property list.
     *
     * If the key is not found in this property list, the default property list, and its defaults, recursively, are
     * then checked. The method returns null if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     */
    public String getProperties(String key) {
        return properties.getProperty(key);
    }

    /**
     * Calls the HashTable method put.
     *
     * Provided for parallelism with the getProperty method. Enforces use of strings for
     * property keys and values. The value returned is the result of the HashTable call to put.
     *
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to key.
     */
    public void setProperties(String key, String value) {
        properties.setProperty(key, value);
    }
}
