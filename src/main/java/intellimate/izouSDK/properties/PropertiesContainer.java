package intellimate.izouSDK.properties;

import java.util.Properties;

/**
 * Wrapper class for properties
 */
public class PropertiesContainer implements intellimate.izou.properties.PropertiesContainer{
    private Properties properties;
    private intellimate.izou.system.context.Context context;

    /**
     * Creates a new properties-container
     *
     * @param context instance of Context
     */
    public PropertiesContainer(intellimate.izou.system.context.Context context) {
        this.properties = new Properties();
        this.context = context;
    }

    /**
     * Gets the properties-container
     *
     * @return the properties-container
     */
    public Properties getProperties() {
        return properties;
    }

    /**
     * Sets the properties-container
     *
     * @param properties the properties-container
     */
    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    /**
     * Gets the context of the current addOn
     *
     * @return context of the current addOn
     */
    public intellimate.izou.system.context.Context getContext() {
        return context;
    }
}
