package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventsController;
import intellimate.izou.identification.Identifiable;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import intellimate.izou.system.Context;
import intellimate.izou.threadpool.ExceptionCallback;
import ro.fortsoft.pf4j.ExtensionPoint;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * All AddOns must implement this Class.
 *
 * It will be instantiated and its registering-methods will be called by the PluginManager.
 * This class has method for a properties-file named addOnID.properties (AddOnsID in the form: package.class)
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface AddOn extends ExtensionPoint, Identifiable, ExceptionCallback {
    /**
     * Internal initiation of addOn - fake constructor, comes before prepare
     * @param context the context to initialize with
     */
    void initAddOn(Context context);

    /**
     * use this method to build your instances etc.
     */
    void prepare();

    /**
     * use this method to register (if needed) your Activators.
     * @return Array containing Instances of Activators
     */
    Activator[] registerActivator();

    /**
     * use this method to register (if needed) your ContentGenerators.
     * @return Array containing Instances of ContentGenerators
     */
    ContentGenerator[] registerContentGenerator();

    /**
     * use this method to register (if needed) your EventControllers.
     * @return Array containing Instances of EventControllers
     */
    EventsController[] registerEventController();

    /**
     * use this method to register (if needed) your OutputPlugins.
     * @return Array containing Instances of OutputPlugins
     */
    OutputPlugin[] registerOutputPlugin();

    /**
     * use this method to register (if needed) your Output.
     * @return Array containing Instances of OutputExtensions
     */
    OutputExtension[] registerOutputExtension();

    /**
     * gets the associated Plugin.
     * @return the Plugin.
     */
    public PluginWrapper getPlugin();

    /**
     * sets the Plugin IF it is not already set.
     * @param plugin the plugin
     */
    public void setPlugin(PluginWrapper plugin);

    /**
     * returns the Context of the AddOn.
     *
     * Context provides some general Communications.
     *
     * @return an instance of Context.
     */
    public Context getContext();
}
