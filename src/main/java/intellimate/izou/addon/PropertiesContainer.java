package intellimate.izou.addon;

import java.util.Properties;

/**
 * Wrapper class for properties
 */
public class PropertiesContainer {
    Properties properties;

    public PropertiesContainer() {
        this.properties = new Properties();
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }
}
