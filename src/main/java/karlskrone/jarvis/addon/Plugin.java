package karlskrone.jarvis.addon;

import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.contentgenerator.ContentGeneratorManager;
import karlskrone.jarvis.events.EventManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A Plugin must extend this Class.
 *
 * It will be instantiated and its registering-methods will be called by the PluginManager.
 * This class has method for a properties-file named pluginID.properties (PluginID in the form: package.class)
 */
public abstract class Plugin
        //extends ExtensionPoint
{
    private Properties properties;
    private String pluginID;

    /**
     * the default constructor for Plugin
     * @param pluginID the ID of the Plugin in the form: package.class
     */
    public Plugin(String pluginID) {
        this.pluginID = pluginID;

        properties = new Properties();
        String propFileName = pluginID + ".properties";

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
        try {
            properties.load(inputStream);
        } catch (IOException e) {
            //TODO: log exception
            e.printStackTrace();
        }
    }

    /**
     * use this method to register (if needed) your Activators.
     * @param activatorManager the ActivatorManager the Activators can be registered
     */
    public abstract void registerActivator(ActivatorManager activatorManager);

    /**
     * use this method to register (if needed) your ContentGenerators.
     * @param contentGeneratorManager the ContentGeneratorManager the ContentGenerators can be registered
     */
    public abstract void registerContentGenerator(ContentGeneratorManager contentGeneratorManager);

    /**
     * use this method to register (if needed) your EventControllers.
     * @param eventManager the EventManager the EventControllers can be registered
     */
    public abstract void registerEventController(EventManager eventManager);

    /**
     * use this method to register (if needed) your Output.
     */
    public abstract void registerOutput();

    /**
     * The Plugin-Id is usually the following Form: package.class
     * @return a String containing the PluginID
     */
    public String getPluginID() {
        return pluginID;
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
     * Calls the Hashtable method put.
     *
     * Provided for parallelism with the getProperty method. Enforces use of strings for
     * property keys and values. The value returned is the result of the Hashtable call to put.
     *
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to key.
     */
    public void setProperties(String key, String value) {
        properties.setProperty(key, value);
    }
}
