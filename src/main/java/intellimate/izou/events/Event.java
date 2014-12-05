package intellimate.izou.events;

import intellimate.izou.system.Identifiable;
import intellimate.izou.system.Identification;
import intellimate.izou.resource.ListResourceProvider;
import intellimate.izou.resource.Resource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * This class represents an Event.
 */
public class Event implements Identifiable{
    /**
     * Use this type when other AddOns should react to this Event.
     */
    public static final String RESPONSE = Event.class.getCanonicalName() + "Response";
    /**
     * Use this type when other AddOns should just notice (they needn't).
     */
    public static final String NOTIFICATION = Event.class.getCanonicalName() + "Notification";
    private final String type;
    private final Identification source;
    private List<String> descriptors = new ArrayList<>();
    private ListResourceProvider listResourceContainer = new ListResourceProvider();
    private final EventBehaviourController eventBehaviourController = new EventBehaviourController(this);
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * Creates a new Event Object
     * @param type the Type of the Event, try to use the predefined Event types
     * @param source the source of the Event, most likely a this reference.
     */
    private Event(String type, Identification source) {
        this.type = type;
        this.source = source;
    }

    /**
     * Creates a new Event Object
     * @param type the Type of the Event, try to use the predefined Event types
     * @param source the source of the Event, most likely a this reference.
     * @return an Optional, that may be empty if type is null or empty or source is null
     */
    public static Optional<Event> createEvent(String type, Identification source) {
        if(type == null || type.isEmpty()) return Optional.empty();
        if(source == null) return Optional.empty();
        return Optional.of(new Event(type, source));
    }

    /**
     * The ID of the Event.
     * It describes the Type of the Event.
     * @return A String containing an ID
     */
    @Override
    public String getID() {
    return type;
    }

    /**
     * The type of the Event.
     * It describes the Type of the Event.
     * @return A String containing an ID
     */
    public String getType() {
        return type;
    }

    /**
     * returns the Source of the Event, e.g. the object who fired it.
     * @return an identifiable
     */
    public Identification getSource() {
        return source;
    }

    /**
     * returns all the Resources the Event currently has
     * @return an instance of ListResourceContainer
     */
    public ListResourceProvider getListResourceContainer() {
        return listResourceContainer;
    }

    /**
     * adds a Resource to the Container
     * @param resource an instance of the resource to add
     */
    void addResource(Resource resource) {
        listResourceContainer.addResource(resource);
    }

    /**
     * adds a List of Resources to the Container
     * @param resources a list containing all the resources
     */
    void addResources(List<Resource> resources) {
        listResourceContainer.addResource(resources);
    }

    /**
     * returns a List containing all the Descriptors.
     * @return a List containing the Descriptors
     */
    public List<String> getDescriptors() {
        return descriptors;
    }

    /**
     * returns a List containing all the Descriptors and the type.
     * @return a List containing the Descriptors
     */
    public List<String> getAllIformations() {
        List<String> information = new LinkedList<>(descriptors);
        information.add(type);
        return information;
    }

    /**
     * sets the Descriptors (but not the Event-Type).
     * @param descriptors a List containing all the Descriptors
     */
    public void setDescriptors(LinkedList<String> descriptors) {
        this.descriptors = descriptors;
    }

    /**
     * sets the Descriptors (but not the Event-Type).
     * @param descriptor a String describing the Event
     */
    public void addDescriptor(String descriptor) {
        descriptors.add(descriptor);
    }

    /**
     * returns whether the event contains the specific descriptor.
     * this method also checks whether it matches the type.
     * @param descriptor a String with the ID of the Descriptor
     * @return boolean when the Event contains the descriptor, false when not.
     */
    public boolean containsDescriptor(String descriptor) {
        return descriptors.contains(descriptor) || type.equals(descriptor);
    }

    /**
     * returns the associated EventBehaviourController
     * @return an instance of EventBehaviourController
     */
    public EventBehaviourController getEventBehaviourController() {
        return eventBehaviourController;
    }
}
