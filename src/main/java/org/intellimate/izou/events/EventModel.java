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
     * returns the Source of the Event, e.g. the object who fired it.
     * @return an identifiable
     */
    Identification getSource();

    /**
     * returns all the Resources the Event currently has
     * @return an instance of ListResourceContainer
     */
    ListResourceProvider getListResourceContainer();

    /**
     * adds a Resource to the Container
     * @param resource an instance of the resource to add
     * @return the resulting Event (which is the same instance)
     */
    X addResource(ResourceModel resource);

    /**
     * adds a List of Resources to the Container
     * @param resources a list containing all the resources
     * @return the resulting Event (which is the same instance)
     */
    X addResources(List<ResourceModel> resources);

    /**
     * returns a List containing all the Descriptors.
     * @return a List containing the Descriptors
     */
    List<String> getDescriptors();

    /**
     * returns a List containing all the Descriptors and the type.
     * @return a List containing the Descriptors
     */
    List<String> getAllInformations();

    /**
     * returns whether the event contains the specific descriptor.
     * this method also checks whether it matches the type.
     * @param descriptor a String with the ID of the Descriptor
     * @return boolean when the Event contains the descriptor, false when not.
     */
    boolean containsDescriptor(String descriptor);

    /**
     * returns the associated EventBehaviourController
     * @return an instance of EventBehaviourController
     */
    EventBehaviourControllerModel getEventBehaviourController();
}
