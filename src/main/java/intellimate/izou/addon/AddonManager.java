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

    public AddOnManager(OutputManager outputManager, EventManager eventManager,
                        ContentGeneratorManager contentGeneratorManager, ActivatorManager activatorManager) {
        addOnList = new LinkedList<>();
        this.outputManager = outputManager;
        this.eventManager = eventManager;
        this.contentGeneratorManager = contentGeneratorManager;
        this.activatorManager = activatorManager;
    }

    /**
     * loops all the AddOns an lets them register all their Activators
     */
    private void registerActivators() {
        for (AddOn addOn : addOnList) {
            for(Activator activator : addOn.registerActivator()) {
                activatorManager.addActivator(activator);
            }
        }
    }

    /**
     * loops all the AddOns an lets them register all their ContentGenerators
     */
    private void registerContentGenerators(){
        for (AddOn addOn : addOnList) {
            for(ContentGenerator contentGenerator : addOn.registerContentGenerator()) {
                contentGeneratorManager.addContentGenerator(contentGenerator);
            }
        }
    }

    /**
     * loops all the AddOns an lets them register all their EventController
     */
    private void registerEventControllers(){
        for (AddOn addOn : addOnList) {
            for (EventController eventController : addOn.registerEventController()) {
                eventManager.addEventController(eventController);
            }
        }
    }

    /**
     * loops all the AddOns an lets them register all their OutputsPlugins
     */
    private void registerOutputPlugins(){
        for (AddOn addOn : addOnList) {
            for(OutputPlugin outputPlugin : addOn.registerOutputPlugin()) {
                outputManager.addOutputPlugin(outputPlugin);
            }
        }
    }

    /**
     * loops all the AddOns an lets them register all their OutputsExtensions
     */
    private void registerOutputExtensions(){
        for (AddOn addOn : addOnList) {
            for(OutputExtension outputExtension : addOn.registerOutputExtension()) {
                outputManager.addOutputExtension(outputExtension, outputExtension.getPluginId());
            }
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
        Path libPath = Paths.get("/lib");
        if(!Files.exists(libPath)) try {
            Files.createDirectories(libPath);
        } catch (IOException e) {
            //TODO: implement Exception handling
            e.printStackTrace();

        }
        PluginManager pluginManager = new DefaultPluginManager(libPath.toFile());
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
    }
}
