package org.intellimate.izou.addon;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.system.Context;
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
public interface AddOnModel extends ExtensionPoint, Identifiable {
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
    PluginWrapper getPlugin();

    /**
     * sets the Plugin IF it is not already set.
     * @param plugin the plugin
     */
    void setPlugin(PluginWrapper plugin);
}
