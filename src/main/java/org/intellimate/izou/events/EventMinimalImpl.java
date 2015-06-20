package org.intellimate.izou.events;

import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.resource.ListResourceMinimalImpl;
import org.intellimate.izou.resource.ListResourceProvider;
import org.intellimate.izou.resource.ResourceModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

/**
 * this is a minimal implementation of an Event. Do not use this outside Izou! It will change between Versions!
 * @author LeanderK
 * @version 1.0
 */
public class EventMinimalImpl implements EventModel<EventMinimalImpl> {
    private final String type;
    private final Identification source;
    private final List<String> descriptors;
    private final ListResourceProvider listResourceContainer;
    private final Consumer<EventLifeCycle> callback;
    private final EventBehaviourControllerImpl eventBehaviourController;

    public EventMinimalImpl(String type, Identification source, List<String> descriptors) {
        this.type = type;
        this.source = source;
        this.descriptors = descriptors;
        this.listResourceContainer = new ListResourceMinimalImpl();
        callback = eventLifeCycle ->{};
        eventBehaviourController = new EventBehaviourControllerImpl();
    }

    public EventMinimalImpl(String type, Identification source, List<String> descriptors, Consumer<EventLifeCycle> callback) {
        this.type = type;
        this.source = source;
        this.descriptors = descriptors;
        this.listResourceContainer = new ListResourceMinimalImpl();
        this.callback = callback;
        eventBehaviourController = new EventBehaviourControllerImpl();
    }

    /**
     * The type of the Event.
     * It describes the Type of the Event.
     *
     * @return A String containing an ID
     */
    @Override
    public String getType() {
        return type;
    }

    /**
     * returns the Source of the Event, e.g. the object who fired it.
     * @return an identifiable
     */
    @Override
    public Identification getSource() {
        return source;
    }

    /**
     * returns all the Resources the Event currently has
     * @return an instance of ListResourceContainer
     */
    @Override
    public ListResourceProvider getListResourceContainer() {
        return listResourceContainer;
    }

    /**
     * adds a Resource to the Container
     * @param resource an instance of the resource to add
     * @return the resulting Event (which is the same instance)
     */
    @Override
    public EventMinimalImpl addResource(ResourceModel resource) {
        listResourceContainer.addResource(resource);
        return this;
    }

    /**
     * adds a List of Resources to the Container
     * @param resources a list containing all the resources
     */
    @Override
    public EventMinimalImpl addResources(List<ResourceModel> resources) {
        listResourceContainer.addResource(resources);
        return this;
    }

    /**
     * returns a List containing all the Descriptors.
     * @return a List containing the Descriptors
     */
    @Override
    public List<String> getDescriptors() {
        return descriptors;
    }

    /**
     * returns a List containing all the Descriptors and the type.
     * @return a List containing the Descriptors
     */
    @Override
    public List<String> getAllInformations() {
        ArrayList<String> strings = new ArrayList<>(descriptors);
        strings.add(type);
        return strings;
    }

    /**
     * returns whether the event contains the specific descriptor.
     * this method also checks whether it matches the type.
     *
     * @param descriptor a String with the ID of the Descriptor
     * @return boolean when the Event contains the descriptor, false when not.
     */
    @Override
    public boolean containsDescriptor(String descriptor) {
        return descriptors.contains(descriptor) || type.equals(descriptor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventMinimalImpl)) return false;

        EventMinimalImpl that = (EventMinimalImpl) o;

        if (type != null ? !type.equals(that.type) : that.type != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        return !(descriptors != null ? !descriptors.equals(that.descriptors) : that.descriptors != null);

    }

    @Override
    public int hashCode() {
        int result = type != null ? type.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (descriptors != null ? descriptors.hashCode() : 0);
        return result;
    }

    /**
     * returns the associated EventBehaviourController
     *
     * @return an instance of EventBehaviourController
     */
    @Override
    public EventBehaviourControllerModel getEventBehaviourController() {
        return eventBehaviourController;

    }

    /**
     * this method gets called when the different lifecycle-stages got reached.
     * It is not blocking!
     *
     * @param eventLifeCycle the lifecycle reached.
     */
    @Override
    public void lifecycleCallback(EventLifeCycle eventLifeCycle) {
        callback.accept(eventLifeCycle);
    }

    /**
     * An ID must always be unique.
     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
     * If you have to implement this interface multiple times, just concatenate unique Strings to
     * .class.getCanonicalName()
     *
     * @return A String containing an ID
     */
    @Override
    public String getID() {
        return type;
    }

    private class EventBehaviourControllerImpl implements EventBehaviourControllerModel {

        /**
         * generates the data to control the Event
         * <p>
         * The Identifications with the highest Integer get the priority.
         * </p>
         *
         * @param identifications the Identifications of the OutputPlugins
         * @return a HashMap, where the keys represent the associated Behaviour and the values the Identification;
         */
        @Override
        public HashMap<Integer, List<Identification>> getOutputPluginBehaviour(List<Identification> identifications) {
            HashMap<Integer, List<Identification>> hashMap = new HashMap<>();
            hashMap.put(0, identifications);
            return hashMap;
        }
    }
}
