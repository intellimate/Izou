package org.intellimate.izou.events;

import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.resource.ListResourceMinimalImpl;
import org.intellimate.izou.resource.ListResourceProvider;
import org.intellimate.izou.resource.ResourceModel;

import java.util.ArrayList;
import java.util.List;

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

    public EventMinimalImpl(String type, Identification source, List<String> descriptors) {
        this.type = type;
        this.source = source;
        this.descriptors = descriptors;
        this.listResourceContainer = new ListResourceMinimalImpl();
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

    /**
     * returns the associated EventBehaviourController
     *
     * @return an instance of EventBehaviourController
     */
    @Override
    public EventBehaviourControllerModel getEventBehaviourController() {
        return null;
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
}
