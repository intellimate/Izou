package org.intellimate.izou.events;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.resource.ListResourceProvider;
import org.intellimate.izou.resource.ResourceModel;

import java.util.List;

/**
 * This class represents an Event, the main communication form for the AddOns.
 * After the call to finalizeEvent() the properties (except adding resources) should be immutable.
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface EventModel<X extends EventModel> extends Identifiable {
    /**
     * The type of the Event.
     * It describes the Type of the Event.
     * @return A String containing an ID
     */
    String getType();

    /**
     * Returns the source of the event, e.g. the object who fired it.
     *
     * @return an identifiable
     */
    Identification getSource();

    /**
     * Returns all the resources the event currently has
     *
     * @return an instance of ListResourceContainer
     */
    ListResourceProvider getListResourceContainer();

    /**
     * adds a resource to the container
     * @param resource an instance of the resource to add
     * @return the resulting Event (which is the same instance)
     */
    X addResource(ResourceModel resource);

    /**
     * adds a list of resources to the container
     * @param resources a list containing all the resources
     * @return the resulting Event (which is the same instance)
     */
    X addResources(List<ResourceModel> resources);

    /**
     * returns a list containing all the descriptors.
     * @return a list containing the descriptors
     */
    List<String> getDescriptors();

    /**
     * adds an descriptor.
     * @param descriptor the descriptor to add
     * @return true if added, false if not
     */
    boolean addDescriptor(String descriptor);

    /**
     * removes an descriptor.
     * @param descriptor to remove
     * @return true if removed, false if not
     */
    boolean removeDescriptor(String descriptor);

    /**
     * returns the immutable (except the resources) event
     * @return the EventModel
     */
    EventModel<X> finalizeEvent();

    /**
     * returns a list containing all the descriptors and the type.
     * @return a list containing the descriptors
     */
    List<String> getAllInformations();

    /**
     * returns whether the event contains the specific descriptor.
     * this method also checks whether it matches the type.
     * @param descriptor a string with the ID of the descriptor
     * @return boolean when the event contains the descriptor, false when not.
     */
    boolean containsDescriptor(String descriptor);

    /**
     * returns the associated EventBehaviourController
     * @return an instance of EventBehaviourController
     */
    EventBehaviourControllerModel getEventBehaviourController();
}
