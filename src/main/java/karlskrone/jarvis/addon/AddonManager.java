package karlskrone.jarvis.addon;

import karlskrone.jarvis.activator.Activator;
import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.contentgenerator.ContentGenerator;
import karlskrone.jarvis.contentgenerator.ContentGeneratorManager;
import karlskrone.jarvis.events.EventController;
import karlskrone.jarvis.events.EventManager;
import karlskrone.jarvis.output.OutputExtension;
import karlskrone.jarvis.output.OutputManager;
import karlskrone.jarvis.output.OutputPlugin;
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
