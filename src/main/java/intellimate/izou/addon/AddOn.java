package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventsController;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import intellimate.izou.system.Context;
import intellimate.izou.system.Identifiable;
import intellimate.izou.threadpool.ExceptionCallback;
import ro.fortsoft.pf4j.ExtensionPoint;

import java.io.*;
import java.util.Enumeration;
import java.util.Properties;

/**
 * All AddOns must extend this Class.
 *
 * It will be instantiated and its registering-methods will be called by the PluginManager.
 * This class has method for a properties-file named addOnID.properties (AddOnsID in the form: package.class)
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class AddOn implements ExtensionPoint, Identifiable, ExceptionCallback {
    private PropertiesContainer propertiesContainer;
    @SuppressWarnings("FieldCanBeLocal")
    private final String addOnID;
    private String propertiesPath;
    private String defaultPropertiesPath;
    private Context context;

    /**
     * the default constructor for AddOns
     * @param addOnID the ID of the Plugin in the form: package.class
     */
    public AddOn(String addOnID) {
        this.addOnID = addOnID;
        this.propertiesContainer = new PropertiesContainer();
    }

    /**
     * internal initiation of addOn
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
     * Initializes properties in the addOn. Creates new properties file with default properties.
     */
    public void initProperties() {
        Properties properties = propertiesContainer.getProperties();
        String propertiesPathTemp;
        try {
            propertiesPathTemp = new File(".").getCanonicalPath() + File.separator + "properties" + File.separator + addOnID + ".properties";
        } catch (IOException e) {
            propertiesPathTemp = null;
            context.logger.getLogger().error("Error while trying to build the propertiesPathTemp", e);
        }

        propertiesPath = propertiesPathTemp;
        File propertiesFile = new File(propertiesPath);
        if (!propertiesFile.exists()) try {
            propertiesFile.createNewFile();
        } catch (IOException e) {
            context.logger.getLogger().error("Error while trying to create the new Properties file", e);
        }

        InputStream inputStream;
        try {
            inputStream = new FileInputStream(propertiesFile);
            try {
                properties.load(inputStream);
            } catch (IOException e) {
                context.logger.getLogger().error("unable to load the InputStream for the PropertiesFile",e);
            }
        } catch (FileNotFoundException e) {
            context.logger.getLogger().error("Properties-File not found", e);
        }

        if(defaultPropertiesPath != null) {
            @SuppressWarnings("unchecked")
            Enumeration<String> keys = (Enumeration<String>)this.propertiesContainer.getProperties().propertyNames();

            if (!keys.hasMoreElements()) {
                try {
                    createDefaultPropertyFile(defaultPropertiesPath);
                } catch (IOException e) {
                    context.logger.getLogger().error("Error while trying to copy the Default-Properties File", e);
                }
                if (new File(defaultPropertiesPath).exists() && !writeToPropertiesFile (defaultPropertiesPath)) return;
                try {
                    reloadFiles();
                } catch (IOException e) {
                    context.logger.getLogger().error("Error while trying to reload the Properties-Files", e);
                }
            }
        }
        //defaultPropertiesPath = null;
    }

    /**
     * Writes defaultPropertiesFile.txt to real properties file
     * This is done so that the final user never has to worry about property file initialization
     *
     * @param defaultPropsPath path to defaultPropertyFile.txt (or where it should be created)
     * @return true if operation has succeeded, else false
     */
    private boolean writeToPropertiesFile(String defaultPropsPath) {
        return context.fileManager.getFileManager().writeToFile(defaultPropsPath, propertiesPath);
    }

    /**
     * Creates a defaultPropertyFile.txt in case it does not exist yet. In case it is used by an addOn,
     * it copies its content into the real properties file every time the addOn is launched.
     *
     * It is impossible to get the properties file on default, that way the user should not have to worry about
     * the property file's initial content.
     *
     * @param defaultPropsPath path to defaultPropertyFile.txt (or where it should be created)
     * @throws IOException is thrown by bufferedWriter
     */
    private void createDefaultPropertyFile(String defaultPropsPath) throws IOException {
        context.fileManager.getFileManager().createDefaultFile(defaultPropsPath, "# Properties should always be in the " +
                "form of: \"key = value\"");
    }

    /**
     * reloads the propertiesFile into the propertiesContainer
     *
     * @throws IOException thrown by inputStream
     */
    public void reloadFiles() throws IOException {
        Properties temp = new Properties();
        InputStream inputStream = null;
        try {
            File properties = new File(propertiesPath);

            //Reader reader = new FileReader(properties);
            BufferedReader in = new BufferedReader(new InputStreamReader(new FileInputStream(properties),
                    "UTF8"));
            temp.load(in);
            //inputStream = new FileInputStream(properties);
            //temp.load(inputStream);
        } catch(IOException e) {
            context.logger.getLogger().error("Error while trying to load the Properties-File: " + propertiesPath, e);
            return;
        } finally {
            if (inputStream != null)
                inputStream.close();
        }
        this.propertiesContainer.setProperties(temp);
    }

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
     * You should probably use getPropertiesContainer() unless you have a very good reason not to.
     *
     * Searches for the property with the specified key in this property list.
     *
     * If the key is not found in this property list, the default property list, and its defaults, recursively, are
     * then checked. The method returns null if the property is not found.
     *
     * @param key the property key.
     * @return the value in this property list with the specified key value.
     */
    public String getProperties(String key) {
        return propertiesContainer.getProperties().getProperty(key);
    }

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
     * Returns an Instance of Properties, if found
     *
     * @return an Instance of Properties or null;
     */
    public PropertiesContainer getPropertiesContainer() {
        return propertiesContainer;
    }

    /**
     * Calls the HashTable method put.
     *
     * Provided for parallelism with the getProperty method. Enforces use of strings for
     *     * property keys and values. The value returned is the result of the HashTable call to put.

     * @param key the key to be placed into this property list.
     * @param value the value corresponding to key.
     */
    public void setProperties(String key, String value) {
        propertiesContainer.getProperties().setProperty(key, value);
    }

    /**
     * Sets properties
     *
     * @param properties instance of properties, not null
     */
    public void setProperties(Properties properties) {
        if(properties == null) return;
        this.propertiesContainer.setProperties(properties);
    }

    /**
     * Sets properties-container
     *
     * @param propertiesContainer the properties-container
     */
    public void setPropertiesContainer(PropertiesContainer propertiesContainer) {
        if(propertiesContainer == null) return;
        this.propertiesContainer = propertiesContainer;
    }

    /**
     * gets the path to default properties file (the file which is copied into the real properties on start)
     *
     * @return path to default properties file
     */
    public String getDefaultPropertiesPath() {
        return defaultPropertiesPath;
    }

    /**
     * *** YOU SHOULD NOT HAVE TO USE THIS METHOD ***
     *
     * sets the path to default properties file (the file which is copied into the real properties on start),
     * all you need to do is provide the artifact name and version concatenated together. We strongly recommend
     * you look at the addOnName parameter for more info. It is also VERY IMPORTANT that your "defaultProperties.txt"
     * file is created in the resource folder of your addOn.
     *
     * @param addOnName The artifact name and version concatenated together.
     *                  (Ex: "artifactName-versionNumber", "testaddon-0.1", etc.)
     *
     * @throws java.lang.NullPointerException the given addOnName is not the correct artifact name and version number
     */
    public void setDefaultPropertiesPath(String addOnName) throws NullPointerException {
        String tempPath = "." + File.separator + "lib" + File.separator + addOnName + File.separator +
                "classes" + File.separator;
        if(new File(tempPath).exists())
            this.defaultPropertiesPath = tempPath + "defaultProperties.txt";
        else
            throw new NullPointerException("File path does not exist");
    }

    /**
     * this method gets called when the task submitted to the ThreadPool crashes
     *
     * @param e the exception catched
     */
    @Override
    public void exceptionThrown(Exception e) {
        context.logger.getLogger().fatal("Addon: " + getID() + " crashed", e);
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
