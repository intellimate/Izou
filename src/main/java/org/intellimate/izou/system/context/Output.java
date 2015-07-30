package org.intellimate.izou.system.context;

import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.output.OutputExtensionModel;
import org.intellimate.izou.output.OutputPluginModel;

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
     * @param t the argument or null
     * @param event the Event to generate for
     * @param <T> the type of the argument
     * @param <X> the return type
     * @return a List of Future-Objects
     */
    <T, X> List<CompletableFuture<X>> generateAllOutputExtensions(OutputPluginModel<T, X> outputPlugin,
                                                                                   T t, EventModel event);

    /**
     * returns the ID of the Manager
     * @return an instance of Identification
     */
    Identification getManagerIdentification();
}
