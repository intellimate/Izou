package intellimate.izouSDK.contentgenerator;

import intellimate.izou.events.Event;
import intellimate.izou.resource.Resource;
import intellimate.izou.system.Context;
import intellimate.izou.identification.IdentificationManager;

import java.util.List;
import java.util.Optional;

/**
 * The Task of an ContentGenerator is to generate a Resources-Object when a Event it subscribed to was fired.
 * <p>
 *     When an Event this ContentGenerator subscribed to was fired, the ContentGeneratorManager will run the instance
 *     of it in a ThreadPool and generate(String eventID) will be called.
 * </p>
 */
public abstract class ContentGeneratorImpl implements ContentGenerator {
    //stores the ID of the ContentGenerator
    private final String contentGeneratorID;
    private final Context context;
    private IdentificationManager identificationManager = IdentificationManager.getInstance();

    /**
     * Creates a new content generator and ensures that each content generator has its own event id so that it can be
     * triggered by activators.
     *
     * <p>
     *     It essentially forces each content generator to register an event ID with the local_event.properties file.
     * </p>
     *
     * @param id the id of the content generator
     * @param context the context of the addOn
     * @param eventDescription the description of the event id
     * @param eventIDName the event id name
     * @param eventID the actual event id
     */
    public ContentGeneratorImpl(String id, Context context, String eventDescription, String eventIDName,
                                String eventID) {
        this.contentGeneratorID = id;
        this.context = context;
        identificationManager.registerIdentification(this);

        if (eventDescription == null || eventIDName == null || eventID == null) {
            context.logger.getLogger().error("Unable to register event for content generator: " + id);
        } else {
            context.events.addEventIDToPropertiesFile(eventDescription, eventIDName, eventID);
        }
    }

    /**
     * this method is called to register what resources the object provides.
     * just pass a List of Resources without Data in it.
     *
     * @return a List containing the resources the object provides
     */
    @Override
    public abstract List<Resource> announceResources();

    /**
     * this method is called to register for what Events it wants to provide Resources.
     *
     * @return a List containing ID's for the Events
     */
    @Override
    public abstract List<String> announceEvents();

    /**
     * this method is called when an object wants to get a Resource.
     * it has as an argument resource instances without data, which just need to get populated.
     *
     * @param resources a list of resources without data
     * @param event     if an event caused the action, it gets passed. It can also be null.
     * @return a list of resources with data
     */
    @Override
    public abstract List<Resource> provideResource(List<Resource> resources, Optional<Event> event);

    /**
     * this method gets called when the task submitted to the ThreadPool crashes
     *
     * @param e the exception caught
     */
    public void exceptionThrown(Exception e) {
        context.logger.getLogger().fatal("ContentGenerator: " + getID() + " crashed", e);
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
        return contentGeneratorID;
    }

    /**
     * returns the Context of the AddOn.
     *
     * Context provides some general Communications.
     *
     * @return an instance of Context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * gets the singleton instance of IdentificationManager
     *
     * @return the singleton instance of IdentificationManager
     */
    public IdentificationManager getIdentificationManager() {
        return identificationManager;
    }
}
