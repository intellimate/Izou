package intellimate.izou.events;

import intellimate.izou.system.Identifiable;
import intellimate.izou.system.ListResourceContainer;

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
    private final Identifiable source;
    private LinkedList<String> descriptors = new LinkedList<>();
    private ListResourceContainer listResourceContainer = new ListResourceContainer();

    /**
     * Creates a new Event Object
     * @param type the Type of the Event, try to use the predefined Event types
     * @param source the source of the Event, most likely a this reference.
     *               Beware! Others can Access the Object!
     */
    public Event(String type, Identifiable source) {
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
    public Identifiable getSource() {
        return source;
    }

    public ListResourceContainer getListResourceContainer() {
        return listResourceContainer;
    }
}
