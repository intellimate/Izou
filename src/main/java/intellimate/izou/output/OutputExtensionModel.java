package intellimate.izou.output;

import com.google.common.reflect.TypeToken;
import intellimate.izou.events.EventModel;
import intellimate.izou.identification.Identifiable;

/**
 * OutputExtension's purpose is to take resourceData and convert it into another data format so that it can be rendered correctly
 * by the output-plugin. These objects are represented in the form of future objects that are stored in tDoneList
 */
public interface OutputExtensionModel<X, T> extends Identifiable {
    /**
     * Checks if the outputExtension can execute with the current event
     *
     * @return the state of whether the outputExtension can execute with the current event
     */
    boolean canRun(EventModel event);

    /**
     * Gets the id of the output-plugin the outputExtension belongs to
     *
     * @return id of the output-plugin the outputExtension belongs to
     */
    String getPluginId();

    /**
     * generates the data for the given Event
     * @param event the event to generate for
     * @param t the optional argument
     * @return the result
     */
    X generate(EventModel event, T t);

    /**
     * returns the ReturnType of the generic
     * @return the type of the generic
     */
    //i don't think there is another way
    TypeToken<X> getReturnType();

    /**
     * returns the Type of the Argument or null if none
     * @return the type of the argument
     */
    //i don't think there is another way
    TypeToken<T> getArgumentType();
}
