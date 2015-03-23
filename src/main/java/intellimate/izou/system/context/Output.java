package intellimate.izou.system.context;

import intellimate.izou.events.EventModel;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IllegalIDException;
import intellimate.izou.output.OutputExtensionModel;
import intellimate.izou.output.OutputPluginModel;

import java.util.List;
import java.util.concurrent.CompletableFuture;

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
     * @throws IllegalIDException not yet implemented
     */
    void addOutputExtension(OutputExtensionModel outputExtension) throws IllegalIDException;

    /**
     * removes the output-extension of id: extensionId from outputPluginList
     *
     * @param outputExtension the OutputExtension to remove
     */
    void removeOutputExtension(OutputExtensionModel outputExtension);

    /**
     * adds outputPlugin to outputPluginList, starts a new thread for the outputPlugin, and stores the future object in a HashMap
     * @param outputPlugin OutputPlugin to add
     * @throws IllegalIDException not yet implemented
     */
    void addOutputPlugin(OutputPluginModel outputPlugin) throws IllegalIDException;

    /**
     * removes the OutputPlugin and stops the thread
     * @param outputPlugin the outputPlugin to remove
     */
    void removeOutputPlugin(OutputPluginModel outputPlugin);

    /**
     * returns all the associated OutputExtensions
     * @param outputPlugin the OutputPlugin to search for
     * @return a List of Identifications
     */
    List<Identification> getAssociatedOutputExtension(OutputPluginModel<?, ?> outputPlugin);

    /**
     * starts every associated OutputExtension
     * @param outputPlugin the OutputPlugin to generate the Data for
     * @param x the argument or null
     * @param event the Event to generate for
     * @return a List of Future-Objects
     */
    public <T, X> List<CompletableFuture<T>> generateAllOutputExtensions(OutputPluginModel<T, X> outputPlugin,
                                                                                   X x, EventModel event);

    /**
     * returns the ID of the Manager
     */
    Identification getManagerIdentification();
}
