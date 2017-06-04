package org.intellimate.izou.addon;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.server.Request;
import org.intellimate.izou.server.Response;
import org.intellimate.izou.system.Context;
import org.intellimate.server.proto.HttpRequest;
import org.intellimate.server.proto.HttpResponse;
import ro.fortsoft.pf4j.AddonAccessible;
import ro.fortsoft.pf4j.ExtensionPoint;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * All AddOns must implement this Class.
 *
 * It will be instantiated and its registering-methods will be called by the PluginManager.
 * This class has method for a properties-file named addOnID.properties (AddOnsID in the form: package.class)
 */
@AddonAccessible
public interface AddOnModel extends ExtensionPoint, Identifiable {
    /**
     * Internal initiation of addOn - fake constructor, comes before prepare
     * @param context the context to initialize with
     */
    void initAddOn(Context context);

    /**
     * Use this method to register your Modules.
     * It will time out after 3 seconds!
     */
    void register();

    /**
     * Gets the associated Plugin.
     *
     * @return the Plugin.
     */
    PluginWrapper getPlugin();

    /**
     * Sets the Plugin IF it is not already set.
     *
     * @param plugin the plugin
     */
    void setPlugin(PluginWrapper plugin);

    /**
     * this method handles the HTTP-Requests
     * @param request the request to process
     * @return the response
     */
    Response handleRequest(Request request);
}
