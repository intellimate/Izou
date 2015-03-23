package intellimate.izou.resource;

import intellimate.izou.events.EventModel;
import intellimate.izou.identification.Identifiable;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to provide resources to other parts of the application.
 */
@SuppressWarnings("UnusedDeclaration")
public interface ResourceBuilderModel extends Identifiable {
    /**
     * This method is called to register what resources the object provides.
     * just pass a List of Resources without Data in it.
     *
     * @return a List containing the resources the object provides
     */
    abstract List<? extends ResourceModel> announceResources();
    /**
     * this method is called to register for what Events it wants to provide Resources.
     * <p>
     * The Event has to be in the following format: It should contain only one Descriptor and and one Resource with the
     * ID "description", which contains an description of the Event.
     * </p> 
     * @return a List containing ID's for the Events
     */
    abstract List<? extends EventModel<?>> announceEvents();
    /**
     * This method is called when an object wants to get a Resource.
     *
     * <p>
     * Don't use the Resources provided as arguments, they are just the requests.
     * There is a timeout after 1 second.
     * </p> 
     * @param resources a list of resources without data
     * @param event if an event caused the action, it gets passed. It can also be null.
     * @return a list of resources with data
     */
    abstract List<ResourceModel> provideResource(List<? extends ResourceModel> resources, Optional<EventModel> event);
}