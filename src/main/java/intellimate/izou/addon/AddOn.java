package intellimate.izou.addon;

import intellimate.izou.identification.Identifiable;
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
     * use this method to register your Modules.
     * <p>
     * It will time out after 3 seconds!
     * </p> 
     */
    void register();

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
}
