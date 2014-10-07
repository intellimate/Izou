package karlskrone.jarvis.addon;

import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.contentgenerator.ContentGeneratorManager;
import karlskrone.jarvis.events.EventManager;

import java.util.List;

/**
 * Manages all the Plugins.
 *
 * Currently Quick & Dirty Implementation.
 */
public class PluginManager {
    List<Plugin> pluginList;
    /*
    ro.fortsoft.pf4j.PluginManager pluginManager;
    public PluginManager() {
        pluginManager = new DefaultPluginManager(new File(System.getProperty(this.getClass().getPackage().getName(), "plugins")));
        // load the plugins
        pluginManager.loadPlugins();
        // start (active/resolved) the plugins
        pluginManager.startPlugins();
        // retrieves the extensions for Greeting extension point
        pluginList = pluginManager.getExtensions(Plugin.class);
    }
    */
    public PluginManager() {
        //quick & dirty implementation
        //initialize Plugins here:
    }

    /**
     * loops all the Plugins an lets them register all their Activators
     *
     * @param activatorManager the ActivatorManager to register to
     */
    public void registerActivator(ActivatorManager activatorManager) {
        for (Plugin plugin : pluginList) {
            plugin.registerActivator(activatorManager);
        }
    }

    /**
     * loops all the Plugins an lets them register all their ContentGenerators
     * @param contentGeneratorManager the contentGeneratorManager to register to
     */
    public void registerContentGenerator(ContentGeneratorManager contentGeneratorManager){
        for (Plugin plugin : pluginList) {
            plugin.registerContentGenerator(contentGeneratorManager);
        }
    }

    /**
     * loops all the Plugins an lets them register all their EventController
     * @param eventManager the EventManager to register to
     */
    public void registerEventController(EventManager eventManager){
        for (Plugin plugin : pluginList) {
            plugin.registerEventController(eventManager);
        }
    }

    /**
     * loops all the Plugins an lets them register all their Outputs
     */
    public void registerOutput(){
        for (Plugin plugin : pluginList) {
            plugin.registerOutput();
        }
    }
}
