package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventsController;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import intellimate.izou.properties.PropertiesContainer;
import intellimate.izou.system.Context;
import intellimate.izou.system.Identifiable;
import intellimate.izou.threadpool.ExceptionCallback;
import ro.fortsoft.pf4j.ExtensionPoint;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * All AddOns must extend this Class.
 *
 * It will be instantiated and its registering-methods will be called by the PluginManager.
 * This class has method for a properties-file named addOnID.properties (AddOnsID in the form: package.class)
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AddOn implements ExtensionPoint, Identifiable, ExceptionCallback {
    @SuppressWarnings("FieldCanBeLocal")
    private final String addOnID;
    private Context context;
    private PluginWrapper plugin;

    /**
     * the default constructor for AddOns
     * @param addOnID the ID of the Plugin in the form: package.class
     */
    public AddOn(String addOnID) {
        this.addOnID = addOnID;
    }



    /**
     * Internal initiation of addOn - fake constructor, comes before prepare
     * @param context the context to initialize with
     */
    protected void initAddOn(Context context) {
        this.context = context;
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
    public abstract EventsController[] registerEventController();

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
     * returns the Context of the AddOn.
     *
     * Context provides some general Communications.
     *
     * @return an instance of Context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * gets the associated Plugin.
     * @return the Plugin.
     */
    public PluginWrapper getPlugin() {
        return plugin;
    }

    /**
     * sets the Plugin IF it is not already set.
     * @param plugin the plugin
     */
    protected void setPlugin(PluginWrapper plugin) {
        this.plugin = plugin;
    }

    /**
     * Initializes properties in the addOn. Creates new properties file with default properties.
     *
     * @deprecated since version 0.9.9.9 - use {@link intellimate.izou.properties.PropertiesManager#initProperties()}
     */
    @Deprecated
    public void initPoperties() {
        context.properties.getPropertiesManger().initProperties();
    }

    /**
     * You should probably use getPropertiesContainer() unless you have a very good reason not to.
     *
     * Searches for the property with the specified key in this property list.
     *
     * If the key is not found in this property list, the default property list, and its defaults, recursively, are
     * then checked. The method returns null if the property is not found.
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#getProperties}
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     */
    @Deprecated
    public String getProperties(String key) {
        return context.properties.getPropertiesContainer().getProperties().getProperty(key);
    }

    /**
     * Returns an Instance of Properties, if found
     *
     * @return an Instance of Properties or null;
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#getPropertiesContainer}
     */
    @Deprecated
    public PropertiesContainer getPropertiesContainer() {
        return context.properties.getPropertiesContainer();
    }

    /**
     * Calls the HashTable method put.
     *
     * Provided for parallelism with the getProperty method. Enforces use of strings for
     * property keys and values. The value returned is the result of the HashTable call to put.
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#setProperties}
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to key.
     */
    @Deprecated
    public void setProperties(String key, String value) {
        context.properties.getPropertiesContainer().getProperties().setProperty(key, value);
    }

    /**
     * Sets properties
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#setProperties}
     * @param properties instance of properties, not null
     */
    @Deprecated
    public void setProperties(java.util.Properties properties) {
        context.properties.setProperties(properties);
    }

    /**
     * Sets properties-container
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#setPropertiesContainer}
     * @param propertiesContainer the properties-container
     */
    @Deprecated
    public void setPropertiesContainer(PropertiesContainer propertiesContainer) {
        context.properties.setPropertiesContainer(propertiesContainer);
    }

    /**
     * Gets the path to properties file (the real properties file - as opposed to the {@code defaultProperties.txt} file)
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#getPropertiesPath}
     * @return path to properties file
     */
    @Deprecated
    public String getPropertiesPath() {
        return context.properties.getPropertiesPath();
    }

    /**
     * Sets the path to properties file (the real properties file - as opposed to the {@code defaultProperties.txt} file)
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#setPropertiesPath}
     * @param propertiesPath to properties file
     */
    @Deprecated
    public void setPropertiesPath(String propertiesPath) {
        context.properties.setPropertiesPath(propertiesPath);
    }

    /**
     * Gets the path to default properties file (the file which is copied into the real properties on start)
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#getDefaultPropertiesPath}
     * @return path to default properties file
     */
    @Deprecated
    public String getDefaultPropertiesPath() {
        return context.properties.getDefaultPropertiesPath();
    }

    /**
     * this method gets called when the task submitted to the ThreadPool crashes
     *
     * @deprecated since version 0.9.9.9 - use {@link Context.Properties#getPropertiesContainer}
     * @param e the exception catched
     */
    @Override
    public void exceptionThrown(Exception e) {
        context.logger.getLogger().fatal("Addon: " + getID() + " crashed", e);
    }

    public String setUnusualDefaultPropertiesPath() {
        return null;
    }

    /**
     * An ID must always be unique.
     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
     * If you have to implement this interface multiple times, just concatenate unique Strings to
     * .class.getCanonicalName()
     *
     * @return A String containing an ID
     */
    @Override
    public String getID() {
        return addOnID;
    }
}
