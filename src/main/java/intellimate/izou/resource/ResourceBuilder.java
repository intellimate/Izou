package intellimate.izou.resource;

import intellimate.izou.events.Event;
import intellimate.izou.identification.Identifiable;

import java.util.List;
import java.util.Optional;

/**
 * This interface is used to provide resources to other parts of the application.
 */
@SuppressWarnings("UnusedDeclaration")
public interface ResourceBuilder extends Identifiable {
    /**
     * this method is called to register what resources the object provides.
     * just pass a List of Resources without Data in it.
     * @return a List containing the resources the object provides
     */
    abstract List<Resource> announceResources();
    /**
     * this method is called to register for what Events it wants to provide Resources.
     * @return a List containing ID's for the Events
     */
    abstract List<String> announceEvents();
    /**
     * this method is called when an object wants to get a Resource.
     * it has as an argument resource instances without data, which just need to get populated.
     * @param resources a list of resources without data
     * @param event if an event caused the action, it gets passed. It can also be null.
     * @return a list of resources with data
     */
    abstract List<Resource> provideResource(List<Resource> resources, Optional<Event> event);
}