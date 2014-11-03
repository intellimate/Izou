package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventController;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import ro.fortsoft.pf4j.ExtensionPoint;

import java.io.*;
import java.net.URLDecoder;
import java.util.Properties;

/**
 * All AddOns must extend this Class.
 *
 * It will be instantiated and its registering-methods will be called by the PluginManager.
 * This class has method for a properties-file named addOnID.properties (AddOnsID in the form: package.class)
 */
public abstract class AddOn implements ExtensionPoint {
    private Properties properties;
    private final String addOnID;


    /**
     * the default constructor for AddOns
     * @param addOnID the ID of the Plugin in the form: package.class
     */
    public AddOn(String addOnID) {
        this.addOnID = addOnID;

        properties = new Properties();
        try {
            String path = new File(".").getCanonicalPath();
            File properties = new File(path + File.separator + addOnID + ".properties");
            if(!properties.exists()) try {
                properties.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            InputStream inputStream = null;
            try {
                inputStream = new FileInputStream(properties);
                try {
                    this.properties.load(inputStream);
                } catch (IOException e) {
                    //TODO: log exception
                    e.printStackTrace();
                }
            } catch (FileNotFoundException e) {
                //TODO: log exception
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * use this method to build your instances etc.
     */
    public abstract void prepare();
    /**
     * use this method to register (if needed) your Activators.
     * @return Array containing Instances of Activators
     */
    public abstract Activator[] registerActivator();

    /**
     * use this method to register (if needed) your ContentGenerators.
     * @return Array containing Instances of ContentGenerators
     */
    public abstract ContentGenerator[] registerContentGenerator();

    /**
     * use this method to register (if needed) your EventControllers.
     * @return Array containing Instances of EventControllers
     */
    public abstract EventController[] registerEventController();

    /**
     * use this method to register (if needed) your OutputPlugins.
     * @return Array containing Instances of OutputPlugins
     */
    public abstract OutputPlugin[] registerOutputPlugin();

    /**
     * use this method to register (if needed) your Output.
     * @return Array containing Instances of OutputExtensions
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
     * Returns an Instance of Properties, if found
     *
     * @return an Instance of Properties or null;
     */
    public Properties getProperties() {
        return properties;
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

    /**
     * Sets properties
     *
     * @param properties instance of properties, not null
     */
    public void setProperties(Properties properties) {
        if(properties == null) return;
        this.properties = properties;
    }
}
