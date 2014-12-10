package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventsController;
import intellimate.izou.main.Main;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputManager;
import intellimate.izou.output.OutputPlugin;
import intellimate.izou.resource.ResourceManager;
import intellimate.izou.system.Context;
import intellimate.izou.system.FileManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages all the AddOns.
 */
public class AddOnManager {
    private List<AddOn> addOnList;
    private final OutputManager outputManager;
    private final ResourceManager resourceManager;
    private final ActivatorManager activatorManager;
    public static final String ADDON_DATA_PATH = "." + File.separator + "resources" + File.separator;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());
    private final FileManager fileManager;
    private final Main main;

    public AddOnManager(OutputManager outputManager,
                        ResourceManager resourceManager, ActivatorManager activatorManager, FileManager fileManager, Main main) {
        addOnList = new LinkedList<>();
        this.outputManager = outputManager;
        this.resourceManager = resourceManager;
        this.activatorManager = activatorManager;
        this.fileManager = fileManager;
        this.main = main;
    }

    /**
     * loops through all AddOns and lets them register all their Activators
     */
    private void registerActivators() {
        for (AddOn addOn : addOnList) {
            try {
                Activator[] activators = addOn.registerActivator();
                if (activators == null || activators.length == 0) continue;
                for (Activator activator : activators) {
                    if (activator == null) continue;
                    fileLogger.debug("registering Activator: " + activator.getID()
                            + " from AddOn: " + addOn.getID());
                    try {
                        activatorManager.addActivator(activator);
                    } catch (Exception e) {
                        fileLogger.error("Error while trying to add the activator: " + activator.getID(), e);
                    }
                }
            } catch(Exception | NoClassDefFoundError e) {
                fileLogger.error("Error while trying to add the activators", e);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their ContentGenerators
     */
    private void registerContentGenerators() {
        for (AddOn addOn : addOnList) {
            try {
                ContentGenerator[] contentGenerators = addOn.registerContentGenerator();
                if (contentGenerators == null || contentGenerators.length == 0) continue;
                for (ContentGenerator contentGenerator : contentGenerators) {
                    if (contentGenerator == null) continue;
                    fileLogger.debug("registering ContentGenerator: " + contentGenerator.getID()
                            + " from AddOn: " + addOn.getID());
                    try {
                        resourceManager.registerResourceBuilder(contentGenerator);
                    } catch (Exception e) {
                        fileLogger.error("Error while registering ContentGenerator: " + contentGenerator.getID(), e);
                    }
                }
            } catch (Exception | NoClassDefFoundError e) {
                fileLogger.error("Error while trying to register the ContentGenerators", e);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their EventController
     */
    private void registerEventControllers() {
        for (AddOn addOn : addOnList) {
            try {
                EventsController[] eventsControllers = addOn.registerEventController();
                if (eventsControllers == null || eventsControllers.length == 0) continue;
                for (EventsController eventsController : addOn.registerEventController()) {
                    if (eventsController == null) continue;
                    fileLogger.debug("registering EventsController: " + eventsController.getID()
                            + " from AddOn: " + addOn.getID());
                    try {
                        main.getEventDistributor().registerEventsController(eventsController);
                    } catch (IllegalArgumentException e) {
                        fileLogger.error("Error while registering EventsController:" + eventsController.getID(), e);
                    }
                }
            } catch (Exception | NoClassDefFoundError e) {
                fileLogger.error("Error while trying to register the EventsControllers", e);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their OutputsPlugins
     */
    private void registerOutputPlugins() {
        for (AddOn addOn : addOnList) {
            try {
                OutputPlugin[] outputPlugins = addOn.registerOutputPlugin();
                if (outputPlugins == null || outputPlugins.length == 0) continue;
                for (OutputPlugin outputPlugin : addOn.registerOutputPlugin()) {
                    if (outputPlugin == null) continue;
                    fileLogger.debug("registering OutputPlugin: " + outputPlugin.getID()
                            + " from AddOn: " + addOn.getID());
                    try {
                        outputManager.addOutputPlugin(outputPlugin);
                    } catch (Exception e) {
                        fileLogger.error("Error while registering OutputPlugin: " + outputPlugin.getID(), e);
                    }
                }
            } catch (Exception | NoClassDefFoundError e) {
                fileLogger.error("Error while trying to register the OutputPlugins", e);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their OutputsExtensions
     */
    private void registerOutputExtensions() {
        for (AddOn addOn : addOnList) {
            try {
                OutputExtension[] outputExtensions = addOn.registerOutputExtension();
                if (outputExtensions == null || outputExtensions.length == 0) continue;
                for (OutputExtension outputExtension : addOn.registerOutputExtension()) {
                    if (outputExtension == null) continue;
                    fileLogger.debug("registering OutputExtension: " + outputExtension.getID()
                                            + " from AddOn: " + addOn.getID());
                    try {
                        outputManager.addOutputExtension(outputExtension, outputExtension.getPluginId());
                    } catch (Exception e) {
                        fileLogger.error("Error while registering the OutputExtension: " + outputExtension.getID(), e);
                    }
                }
            } catch (Exception | NoClassDefFoundError e) {
                fileLogger.error("Error while trying to register the OutputExtensions", e);
            }
        }
    }

    /**
     * registers all property files with the PropertyManager
     *
     * @throws IOException
     */
    private void registerFiles() throws IOException {
        String dir = "." + File.separator + "properties";
        for (AddOn addOn : addOnList) {
            if(!(getFolder(addOn) == null)) {
                fileManager.registerFileDir(Paths.get(dir), "properties", addOn);
                addOn.setDefaultPropertiesPath(getFolder(addOn));
            } else {
                fileLogger.debug("no property file was found for AddOn: " + addOn.getID());
            }
        }
    }

    /**
     * registers all the properties-Files for the AddOns
     */
    private void registerProterties() {
        addOnList.stream()
                .forEach(AddOn::initProperties);
    }

    /**
     * gets folder of addOn
     *
     * @param addOn the addOn for which to get the folder
     * @return the folder as a String or null if it was not found
     */
    private String getFolder(AddOn addOn) {
        String addOnName = addOn.getClass().getPackage().getName();
        String[] nameParts = addOnName.split("\\.");

        File file = new File("." + File.separator + "lib");
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        for(String fileName : directories) {
            if(nameParts.length - 1 >= 0 && fileName.contains(nameParts[nameParts.length - 1]))
                return fileName;
        }
        return null;
    }

    /**
     * retrieves and registers all AddOns.
     */
    public void retrieveAndRegisterAddOns() {
        addAllAddOns();
        initAllAddOns();
        try {
            registerFiles();
        } catch(IOException e) {
            fileLogger.error("Error while trying to register the Files for the AddOns",e);
        }
        registerAllAddOns();
        prepareAllAddOns();
    }

    /**
     * Adds AddOns without registering them.
     */
    public void addAddOnsWithoutRegistering(List<AddOn> addOns) {
        addOnList.addAll(addOns);
    }

    /**
     * registers all AddOns.
     *
     * @param addOns a List containing all the AddOns
     */
    public void addAndRegisterAddOns(List<AddOn> addOns) {
        addOnList.addAll(addOns);
        initAllAddOns();
        try {
            registerFiles();
        } catch(IOException e) {
            fileLogger.error("Error while trying to register the files for the AddOns",e);
        }
        registerAllAddOns();
        prepareAllAddOns();
    }

    /**
     * This method searches all the "/lib"-directory for AddOns and adds them to the addOnList
     */
    private void addAllAddOns() {
        File libFile;
        try {
            String libPath = new File(".").getCanonicalPath() + File.separator + "lib";
            libFile = new File(libPath);
        } catch (IOException e) {
            fileLogger.error("Error while trying to get the lib-directory"+e);
            return;
        }

        PluginManager pluginManager = new DefaultPluginManager(libFile);
        // load the plugins
        pluginManager.loadPlugins();

        // enable a disabled plugin
        //pluginManager.enablePlugin("welcome-plugin");

        // start (active/resolved) the plugins
        try {
            pluginManager.startPlugins();
        } catch (Exception e) {
            fileLogger.fatal("Error while trying to start the PF4J-Plugins", e);
        }
        try {
            List<AddOn> addOns = pluginManager.getExtensions(AddOn.class);
            addOnList.addAll(addOns);
        } catch (Exception e) {
            fileLogger.fatal("Error while trying to start the AddOns", e);
        }
    }

    /**
     * prepares all AddOns
     */
    private void prepareAllAddOns() {
        for (AddOn addOn : addOnList) {
            addOn.prepare();
        }
    }

    /**
     * internal initiation of all addOns
     */
    private void initAllAddOns() {
        for (AddOn addOn : addOnList) {
            addOn.initAddOn(new Context(addOn, main, "warn"));
        }
    }

    /**
     * registers all AddOns
     */
    private void registerAllAddOns() {
        registerProterties();
        registerOutputPlugins();
        registerOutputExtensions();
        registerContentGenerators();
        registerEventControllers();
        registerActivators();
    }
}
