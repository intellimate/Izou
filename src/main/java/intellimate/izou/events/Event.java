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
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class represents an Event.
 */
public class Event implements Identifiable {
    /**
     * Use this type when other AddOns should react to this Event.
     */
    public static final String RESPONSE = Event.class.getCanonicalName() + "Response";
    /**
     * Use this type when other AddOns should just notice (they needn't).
     */
    public static final String NOTIFICATION = Event.class.getCanonicalName() + "Notification";
    //common Events-Descriptors:
    /**
     * Event for a Welcome with maximum response.
     *
     * Every component that can contribute should contribute to this Event.
     */
    public static final String FULL_WELCOME_EVENT = LocalEventManager.class.getCanonicalName() + ".FullWelcomeEvent";
    /**
     * Event for a Welcome with major response.
     *
     * Every component that is import should contribute to this Event.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final String MAJOR_WELCOME_EVENT = LocalEventManager.class.getCanonicalName() + ".MajorWelcomeEvent";
    /**
     * Event for a Welcome with major response.
     *
     * Only components that have information of great importance should contribute to this event.
     */
    @SuppressWarnings("UnusedDeclaration")
    public static final String MINOR_WELCOME_EVENT = LocalEventManager.class.getCanonicalName() + ".MinorWelcomeEvent";
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
     * @return the resulting Event (which is the same instance)
     */
    Event addResource(Resource resource) {
        listResourceContainer.addResource(resource);
        return this;
    }

    /**
     * adds a List of Resources to the Container
     * @param resources a list containing all the resources
     */
    Event addResources(List<Resource> resources) {
        listResourceContainer.addResource(resources);
        return this;
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
     * @return the resulting Event (which is the same instance)
     */
    public Event setDescriptors(List<String> descriptors) {
        this.descriptors = descriptors;
        return this;
    }

    /**
     * sets the Descriptors (but not the Event-Type).
     * @param descriptor a String describing the Event.
     * @return the resulting Event (which is the same instance)
     */
    public Event addDescriptor(String descriptor) {
        descriptors.add(descriptor);
        return this;
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

    /**
     * applies the consumer to the Event
     * @param consumer the consumer
     * @return this Event
     */
    public Event peek (Consumer<Event> consumer) {
        consumer.accept(this);
        return this;
    }

    /**
     * maps this event to T
     * @param function the function to map
     * @param <T> the return type
     * @return T
     */
    public <T> T map (Function<Event, T> function) {
        return function.apply(this);
    }

    /**
     * tries to fire the Event.
     * <p>
     * if calling failed, it will call onError. If onError returns true, it will wait 20 milli-seconds an retries
     * firing. OnError will be called with the parameters: this Event and a counter which increments for every try.
     * If onError returns false, the MultipleEventsException will be thrown.
     * </p>
     * @param eventCaller the EventCaller used to fire
     * @param onError this method will be called when an error occurred
     * @return this Event
     * @throws intellimate.izou.events.MultipleEventsException when the method fails to fire the event and onError
     *                              returns false
     */
    public Event tryFire (EventCaller eventCaller, BiFunction<Event, Integer, Boolean> onError)
            throws MultipleEventsException {
        return tryFire(eventCaller, onError, null);
    }

    /**
     * tries to fire the Event.
     * <p>
     * if calling failed, it will call onError. If onError returns true, it will wait 20 milli-seconds an retries
     * firing. OnError will be called with the parameters: this Event and a counter which increments for every try.
     * If onError returns false, the MultipleEventsException will be thrown.
     * if calling succeeded, it will call onSuccess.
     * </p>
     * @param eventCaller the EventCaller used to fire
     * @param onError this method will be called when an error occurred
     * @param onSuccess this method will be called when firing succeeded
     * @return this Event
     * @throws intellimate.izou.events.MultipleEventsException when the method fails to fire the event and onError
     *                              returns false
     */
    public Event tryFire (EventCaller eventCaller, BiFunction<Event, Integer, Boolean> onError,
                                                        Consumer<Event> onSuccess) throws MultipleEventsException {
        boolean success = false;
        int count = 0;
        while (!success) {
            try {
                eventCaller.fire(this);
                success = true;
            } catch (MultipleEventsException e) {
                count++;
                boolean retry = onError.apply(this, count);
                if (!retry)
                    throw e;
            }
        }
        onSuccess.accept(this);
        return this;
    }
}
