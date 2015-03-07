package intellimate.izou.system.context;

import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface Output {
    /**
     * adds output extension to desired outputPlugin
     *
     * adds output extension to desired outputPlugin, so that the output-plugin can start and stop the outputExtension
     * task as needed. The outputExtension is specific to the output-plugin
     *
     * @param outputExtension the outputExtension to be added
     */
    void addOutputExtension(OutputExtension outputExtension);

    /**
     * removes the output-extension of id: extensionId from outputPluginList
     *
     * @param outputExtension the OutputExtension to remove
     */
    void removeOutputExtension(OutputExtension outputExtension);

    /**
     * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
     * @param outputPlugin OutputPlugin to add
     */
    void addOutputPlugin(OutputPlugin outputPlugin);

    /**
     * removes the OutputPlugin and stops the thread
     * @param outputPlugin the outputPlugin to remove
     */
    void removeOutputPlugin(OutputPlugin outputPlugin);
}
