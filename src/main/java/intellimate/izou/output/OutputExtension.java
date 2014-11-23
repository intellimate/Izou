package intellimate.izou.output;

import intellimate.izou.events.Event;
import intellimate.izou.system.Identifiable;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * OutputExtension's purpose is to take resourceData and convert it into another data format so that it can be rendered correctly
 * by the output-plugin. These objects are represented in the form of future objects that are stored in tDoneList
 */
public abstract class OutputExtension<T> implements Callable<T>, Identifiable{

    /**
     * the id of the outputExtension
     */
    private final String id;

    /**
     * the id of the outputPlugin the outputExtension belongs to
     */
    private String pluginId;

    /**
     * the current Event
     */
    private List<Event> events = new LinkedList<>();

    /**
     * a list of all the resource's which the outputExtension would like to receive theoretically to work with
     */
    private List<String> resourceIdWishList;

    /**
     * creates a new outputExtension with a new id
     * @param id the id to be set to the id of outputExtension
     */
    public OutputExtension(String id) {
        this.id = id;
        resourceIdWishList = new ArrayList<>();
    }

    /**
     * returns the current event
     * @return the event
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * returns its resourceIdWishList
     * @return a List containing the resourceIDs
     */
    public List<String> getResourceIdWishList() {
        return resourceIdWishList;
    }

    /**
     * sets the event
     *
     * @param event the event to be added
     */
    public void setEvent(Event event) {
        events.add(event);
    }

    /**
     * checks if the outputExtension can execute with the current event
     *
     * @return the state of whether the outputExtension can execute with the current event
     */
    public boolean canRun() {
        return !events.isEmpty();
    }

    /**
     * removes content-data with id: id from the event
     *
     * @param id the id of the content-data to be removed
     */
    @Deprecated
    public void removeContentData(String id) {
    }

    /**
     * adds id of resource to the wish list
     *
     * @param id the id of the resource to be added to the wish list
     */
    public void addResourceIdToWishList(String id) {
        resourceIdWishList.add(id);
    }

    /**
     * removes resourceId from the resourceIdWishList
     *
     * @param id the id of the resource to be removed
     */
    public void removeResourceIdFromWishList(String id) {
        resourceIdWishList.remove(id);
    }

    /**
     * returns the id of the outputExtension
     *
     * @return id of outputExtension to be returned
     */
    @Override
    public String getID() {
        return id;
    }

    /**
     * gets the id of the output-plugin the outputExtension belongs to
     * @return id of the output-plugin the outputExtension belongs to
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * sets the id of the output-plugin the outputExtension belongs to
     * @param pluginId the id of the output-plugin the outputExtension belongs to that is to be set
     */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    /**
     * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
     * to the outputPlugin
     */
    @Override
    public abstract T call() throws Exception;
}

