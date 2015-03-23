package intellimate.izou.output;

import com.google.common.reflect.TypeToken;
import intellimate.izou.events.EventModel;
import intellimate.izou.identification.Identifiable;
import intellimate.izou.identification.Identification;

/**
 * The OutputPlugin class gets Event and then starts threads filled with output-extension tasks to create the final
 * output and then render it on its own medium
 */
public interface OutputPluginModel<X, T> extends Runnable, Identifiable {
    /**
     * Adds an event to blockingQueue
     *
     * @param event the event to add
     * @throws IllegalStateException raised if problems adding an event to blockingQueue
     */
    void addToEventList(EventModel event);

    /**
     * callback method to notify that an OutputExtension was added
     * @param identification the Identification of the OutputExtension added
     */
    void outputExtensionAdded(Identification identification);

    /**
     * callback method to notify that an OutputExtension was added
     * @param identification the Identification of the OutputExtension added
     */
    void outputExtensionRemoved(Identification identification);

    /**
     * returns the Type of the one wants to receive from the OutputExtensions
     * @return the type of the generic
     */
    //i don't think there is another way
    TypeToken<T> getRecievingType();

    /**
     * returns the Type of the argument for the OutputExtensions, or null if none
     * @return the type of the Argument
     */
    //i don't think there is another way
    TypeToken<X> getArgumentType();
}
