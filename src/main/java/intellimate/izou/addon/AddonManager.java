package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.contentgenerator.ContentGeneratorManager;
import intellimate.izou.events.EventController;
import intellimate.izou.events.EventManager;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputManager;
import intellimate.izou.output.OutputPlugin;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.PluginManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

/**
 * Manages all the AddOns.
 */
public class AddOnManager {
    private List<AddOn> addOnList;
    private final OutputManager outputManager;
    private final EventManager eventManager;
    private final ContentGeneratorManager contentGeneratorManager;
    private final ActivatorManager activatorManager;
    private final PropertiesManager propertiesManager;

    public AddOnManager(OutputManager outputManager, EventManager eventManager,
                        ContentGeneratorManager contentGeneratorManager, ActivatorManager activatorManager) {
        addOnList = new LinkedList<>();
        this.outputManager = outputManager;
        this.eventManager = eventManager;
        this.contentGeneratorManager = contentGeneratorManager;
        this.activatorManager = activatorManager;

        PropertiesManager propertiesManagerTemp;
        try {
            propertiesManagerTemp = new PropertiesManager();
        } catch(IOException e) {
            propertiesManagerTemp = null;
            e.printStackTrace();
            //TODO: implement error handling
        }
        propertiesManager = propertiesManagerTemp;
        Thread thread = new Thread(propertiesManager);
        thread.start();
    }

    /**
     * loops through all AddOns and lets them register all their Activators
     */
    private void registerActivators() {
        for (AddOn addOn : addOnList) {
            Activator[] activators = addOn.registerActivator();
            if(activators == null || activators.length == 0) continue;
            for(Activator activator : activators) {
                if(activator == null) continue;
                activatorManager.addActivator(activator);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their ContentGenerators
     */
    private void registerContentGenerators(){
        for (AddOn addOn : addOnList) {
            ContentGenerator[] contentGenerators = addOn.registerContentGenerator();
            if(contentGenerators == null || contentGenerators.length == 0) continue;
            for(ContentGenerator contentGenerator : contentGenerators) {
                if(contentGenerator == null) continue;
                contentGeneratorManager.addContentGenerator(contentGenerator);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their EventController
     */
    private void registerEventControllers(){
        for (AddOn addOn : addOnList) {
            EventController[] eventControllers = addOn.registerEventController();
            if(eventControllers == null || eventControllers.length == 0) continue;
            for (EventController eventController : addOn.registerEventController()) {
                if(eventController == null) continue;
                eventManager.addEventController(eventController);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their OutputsPlugins
     */
    private void registerOutputPlugins() {
        for (AddOn addOn : addOnList) {
            OutputPlugin[] outputPlugins = addOn.registerOutputPlugin();
            if(outputPlugins == null || outputPlugins.length == 0) continue;
            for(OutputPlugin outputPlugin : addOn.registerOutputPlugin()) {
                if(outputPlugin == null) continue;
                outputManager.addOutputPlugin(outputPlugin);
            }
        }
    }

    /**
     * loops through all AddOns and lets them register all their OutputsExtensions
     */
    private void registerOutputExtensions() {
        for (AddOn addOn : addOnList) {
            OutputExtension[] outputExtensions = addOn.registerOutputExtension();
            if(outputExtensions == null || outputExtensions.length == 0) continue;
            for(OutputExtension outputExtension : addOn.registerOutputExtension()) {
                if(outputExtension == null) continue;
                outputManager.addOutputExtension(outputExtension, outputExtension.getPluginId());
            }
        }
    }

    /**
     * registers all property files with the PropertyManager
     * @throws IOException
     */
    private void registerPropertyFiles() throws IOException {
        for (AddOn addOn : addOnList) {
            propertiesManager.registerProperty(addOn.registerPropertiesFile());
        }
    }

    /**
     * retrieves and registers all AddOns.
     */
    public void retrieveAndRegisterAddOns() {
        addAllAddOns();
        prepareAllAddOns();
        registerAllAddOns();
    }

    /**
     * registers all AddOns.
     * @param addOns a List containing all the AddOns
     */
    public void addAndRegisterAddOns(List<AddOn> addOns) {
        addOnList.addAll(addOns);
        prepareAllAddOns();
        registerAllAddOns();
    }

    /**
     * This method searches all the "/lib"-directory for AddOns and adds them to the addOnList
     */
    private void addAllAddOns() {
        File libFile;
        try {
            String libPath = new File(".").getCanonicalPath()+ File.separator + "lib";
            libFile = new File(libPath);
        } catch (IOException e) {
            //TODO: implement Exception handling
            e.printStackTrace();
            return;
        }
        if(!Files.exists(libFile.toPath())) try {
            Files.createDirectories(libFile.toPath());
        } catch (IOException e) {
            //TODO: implement Exception handling
            e.printStackTrace();
        }
        PluginManager pluginManager = new DefaultPluginManager(libFile);
        List<AddOn> addOns = pluginManager.getExtensions(AddOn.class);
        addOnList.addAll(addOns);
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
     * registers all AddOns
     */
    private void registerAllAddOns() {
        registerOutputPlugins();
        registerOutputExtensions();
        registerContentGenerators();
        registerEventControllers();
        registerActivators();
        try {
            registerPropertyFiles();
        } catch(IOException e) {
            e.printStackTrace();
            //TODO: implement exception handling
        }
    }
}
