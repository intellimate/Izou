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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

import java.io.File;
import java.io.IOException;
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
    private final Main main;

    public AddOnManager(OutputManager outputManager,
                        ResourceManager resourceManager, ActivatorManager activatorManager, Main main) {
        addOnList = new LinkedList<>();
        this.outputManager = outputManager;
        this.resourceManager = resourceManager;
        this.activatorManager = activatorManager;
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
                    try {
                        activatorManager.addActivator(activator);
                    } catch (Exception e) {
                        fileLogger.error(e.getMessage());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                    try {
                        resourceManager.registerResourceBuilder(contentGenerator);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                    try {
                        main.getEventDistributor().registerEventsController(eventsController);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                    try {
                        outputManager.addOutputPlugin(outputPlugin);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                    try {
                        outputManager.addOutputExtension(outputExtension, outputExtension.getPluginId());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
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
                //fileManager.registerFileDir(Paths.get(dir), "properties", addOn);
                addOn.setDefaultPropertiesPath(getFolder(addOn));
            } else {
                //TODO implement log that says no property file was found
            }
        }
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
        prepareAllAddOns();
        try {
            registerFiles();
        } catch(IOException e) {
            e.printStackTrace();
            //TODO: implement exception handling
        }
        //has to be last because of properties that require file paths from prepare and register
        initAllAddOns();
        registerAllAddOns();
    }

    /**
     * registers all AddOns.
     *
     * @param addOns a List containing all the AddOns
     */
    public void addAndRegisterAddOns(List<AddOn> addOns) {
        addOnList.addAll(addOns);
        prepareAllAddOns();
        try {
            registerFiles();
        } catch(IOException e) {
            e.printStackTrace();
            //TODO: implement exception handling
        }
        //has to be last because of properties that require file paths from prepare and register
        initAllAddOns();
        registerAllAddOns();
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
            //TODO: implement Exception handling
            e.printStackTrace();
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
            e.printStackTrace();
        }
        try {
            List<AddOn> addOns = pluginManager.getExtensions(AddOn.class);
            addOnList.addAll(addOns);
        } catch (Exception e) {
            e.printStackTrace();
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
        registerOutputPlugins();
        registerOutputExtensions();
        registerContentGenerators();
        registerEventControllers();
        registerActivators();
    }
}
