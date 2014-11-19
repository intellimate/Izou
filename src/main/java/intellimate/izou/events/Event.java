package intellimate.izou.events;

import intellimate.izou.system.Identifiable;
import intellimate.izou.system.Identification;
import intellimate.izou.system.ListResourceProvider;
import intellimate.izou.system.Resource;

import java.util.LinkedList;

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
    private LinkedList<String> descriptors = new LinkedList<>();
    private ListResourceProvider listResourceContainer = new ListResourceProvider();

    /**
     * Creates a new Event Object
     * @param type the Type of the Event, try to use the predefined Event types
     * @param source the source of the Event, most likely a this reference.
     */
    Event(String type, Identification source) {
        this.type = type;
        this.source = source;
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
    public void addResource(Resource resource) {
        listResourceContainer.addResource(resource);
    }

    /**
     * returns a List containing all the Descriptors.
     * The event-type is also included in the Descriptors.
     * @return a List containing the Descriptors
     */
    public LinkedList<String> getDescriptors() {
        return descriptors;
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
}
