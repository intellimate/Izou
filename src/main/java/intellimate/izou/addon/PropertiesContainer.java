package intellimate.izou.addon;

import intellimate.izou.system.Context;

import java.util.Properties;

/**
 * Wrapper class for properties
 */
public class PropertiesContainer {
    private Properties properties;
    private Context context;

    /**
     * creates a new properties-container
     */
    public PropertiesContainer() {
        this.properties = new Properties();
    }

    /**
     * creates a new properties-container
     */
    public PropertiesContainer(Context context) {
        this.properties = new Properties();
        this.context = context;
    }

    /**
     * gets the properties-container
     * @return the properties-container
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * sets the properties-container
     * @param properties the properties-container
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
