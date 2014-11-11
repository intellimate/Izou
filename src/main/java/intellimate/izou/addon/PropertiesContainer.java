package intellimate.izou.addon;

import java.util.Properties;

/**
 * Wrapper class for properties
 */
public class PropertiesContainer {
    Properties properties;

    /**
     * creates a new properties-container
     */
    public PropertiesContainer() {
        this.properties = new Properties();
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
