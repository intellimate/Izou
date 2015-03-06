package intellimate.izou.output;

import intellimate.izou.events.Event;
import intellimate.izou.identification.Identifiable;

import java.util.concurrent.Callable;

/**
 * OutputExtension's purpose is to take resourceData and convert it into another data format so that it can be rendered correctly
 * by the output-plugin. These objects are represented in the form of future objects that are stored in tDoneList
 */
public interface OutputExtension<T> extends Callable<T>, Identifiable {
    /**
     * Adds an event which the output extension can expect
     *
     * @param event the event to be added
     */
    void addEvent(Event event);

    /**
     * Checks if the outputExtension can execute with the current event
     *
     * @return the state of whether the outputExtension can execute with the current event
     */
    boolean canRun();

    /**
     * Gets the id of the output-plugin the outputExtension belongs to
     *
     * @return id of the output-plugin the outputExtension belongs to
     */
    String getPluginId();
}
