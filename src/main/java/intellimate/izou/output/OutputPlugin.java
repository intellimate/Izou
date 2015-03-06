package intellimate.izou.output;

import intellimate.izou.events.Event;
import intellimate.izou.identification.Identifiable;
import intellimate.izou.threadpool.ExceptionCallback;

import java.util.concurrent.ExecutorService;

/**
 * The OutputPlugin class gets Event and then starts threads filled with output-extension tasks to create the final
 * output and then render it on its own medium
 */
public interface OutputPlugin<T> extends Runnable, Identifiable, ExceptionCallback {
    /**
     * Adds an event to blockingQueue
     *
     * @param event the event to add
     * @throws IllegalStateException raised if problems adding an event to blockingQueue
     */
    void addToEventList(Event event) throws IllegalStateException;

    /**
     * Sets the executor of the OutputManager for efficiency reasons
     *
     * @param executor the executor to be set
     */
    void setExecutor(ExecutorService executor);

    /**
     * add outputExtension to outputExtensionList
     *
     * @param outputExtension the output-extension to be added to outputExtensionList
     */
    void addOutputExtension(OutputExtension<T> outputExtension);

    /**
     * Removes output-extensions from outputExtensionList
     *
     * Removes output-extensions from outputExtensionList, outputExtensionList is an ArrayList that stores all
     * outputExtensions of all OutputPlugins
     *
     * @param id the id of the output extension to be removed
     */
    void removeOutputExtension(String id);
}
