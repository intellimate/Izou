package intellimate.izou.contentgenerator;

import intellimate.izou.events.EventManager;
import intellimate.izou.system.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * The Task of an ContentGenerator is to generate a ContentData-Object when a Event it subscribed to was fired.
 * <p>
 * When an Event this ContentGenerator subscribed to was fired, the ContentGeneratorManager will run the instance of it
 * in a ThreadPool and generate(String eventID) will be called.
 */
@SuppressWarnings("SameParameterValue")
public abstract class ContentGenerator<T> implements Callable<ContentData<T>>, EventManager.ActivatorEventListener{
    private ContentGeneratorManager contentGeneratorManager;
    //stores the ID of the ContentGenerator
    private final String contentGeneratorID;
    private EventManager eventManager = null;
    //stores the eventID, will be retrieved when Thread started
    private String eventID = null;
    //stores all registered events
    private final List<String> registeredEvents = new LinkedList<>();
    private final Context context;

    /**
     * Creates an Instance of ContentGenerator
     * @param contentGeneratorID the ID of the ContentGenerator in the form: package.class
     */
    public ContentGenerator(String contentGeneratorID, Context context) {
        this.contentGeneratorID = contentGeneratorID;
        this.context = context;
    }

    /**
     * registers all needed Dependencies to function
     * @param contentGeneratorManager an instance of ContentGeneratorManager
     * @param eventManager an instance of EventManager
     */
    void registerAllNeededDependencies(ContentGeneratorManager contentGeneratorManager, EventManager eventManager) {
        setContentGeneratorManager(contentGeneratorManager);
        setEventManager(eventManager);
    }
        /**
         * Sets the ContentGenerator.
         *
         * The ContentGeneratorManager will usually be set when you add the ContentGenerator to the ContentGeneratorManager.
         * via ContentGeneratorManager.addContentGenerator();
         *
         * @param contentGeneratorManager the contentGeneratorManager
         */
    private void setContentGeneratorManager(ContentGeneratorManager contentGeneratorManager) {
        this.contentGeneratorManager = contentGeneratorManager;
    }

    /**
     * Sets the EventManager and registers all Events
     *
     * The EventManager will usually be set when you add the ContentGenerator to the ContentGeneratorManager.
     *
     * @param eventManager an instance of EventManager
     */
    private void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;
        for (String eventID : registeredEvents) {
            try {
                eventManager.addActivatorEventListener(eventID, this);
            } catch (IllegalArgumentException e) {
                handleError(e);
            }
        }
    }

    /**
     * Gets called when an Event got Fired
     *
     * @param id the ID of the Event, format: package.class.name
     * @return a Future representing pending completion of the task.
     */
    @Override
    public Future<ContentData> activatorEventFired(String id) {
        if(contentGeneratorManager == null)
        {
            throw new NullPointerException("You have not set the ContentGeneratorManager");
        }
        this.eventID = id;
        return contentGeneratorManager.runContentGenerator(this);
    }

    /**
     * This method implements Callable and should only be called by a Thread.
     *
     * @return The Data computed as a ContentDataObject
     */
    @Override
    public  ContentData<T> call() throws Exception{
        try {
            ContentData<T> contentData = generate(eventID);
            eventID = null;
            return contentData;
        } catch (InterruptedException inter) {
            return null;
        } catch (Exception e) {
            handleError(e);
        }
        return null;
    }

    /**
     * Registers an Event.
     *
     * The ContentGenerator listens to events, and when fired will be started in a Thread in ContentGeneratorManager.
     * The method generate() will then be called and can compute a result.
     * But the events will only be registered when added to the System (registered at the ContentGeneratorManagers).
     * If there is a problem with the events, the exception will be thrown later (handleError()).
     *
     * @param eventID the ID of the Event, format: package.class.name
     */
    public void registerEvent(String eventID){
        if(eventManager != null) {
            try {
                eventManager.addActivatorEventListener(eventID, this);
            } catch (IllegalArgumentException e) {
                handleError(e);
            }
        }
        registeredEvents.add(eventID);
    }

    /**
     * Unregister an Event.
     *
     * @param eventID the ID of the Event, format: package.class.name
     * But the events will only be registered when added to the System (registered at the ContentGeneratorManagers).
     * If there is a problem with the events, the exception will be thrown later (handleError()).
     */
    public void unregisterEvent(String eventID){
        if(eventManager != null) {
            try {
               eventManager.deleteActivatorEventListener(eventID, this);
            }
            catch (IllegalStateException e) {
                handleError(e);
            }
        }
        registeredEvents.remove(eventID);
    }

    /**
     * Unregister all events.
     *
     * But the events will only be registered when added to the System (registered at the ContentGeneratorManagers).
     * If there is a problem with the events, the exception will be thrown later (handleError()).
     */
    public void unregisterAllEvents() throws IllegalArgumentException{
        registeredEvents.stream().filter(id -> eventManager != null).forEach(id -> {
            try {
                eventManager.deleteActivatorEventListener(id, this);
            } catch (IllegalArgumentException e) {
                handleError(e);
            }
        });
        registeredEvents.clear();
    }

    /**
     * Returns all Event-Ids this Instance is listening to.
     *
     * @return a list containing all the Event-IDs
     */
    public List<String> getRegisteredEvents() {
        return registeredEvents;
    }

    /**
     * Generates the ContentData.
     *
     * This method will finally be called when the ContentGenerator was started in a Thread to compute ContentData.
     *
     * @param eventID the Event-ID which was fired.
     * @return ContentData the Data computed
     * @throws Exception any exceptions thrown in the process, including interruptions.
     */
    public abstract ContentData<T> generate(String eventID) throws Exception;

    /**
     * Handles the Errors during Computation and registering Events.
     *
     * This method handles the errors thrown during Computation by generate(). Note that this can also include
     * Interruption Exceptions if the Thread takes to long.
     * Another possibility is that an EventID is malformed or null. Note that this exception can only be thrown as soon
     * as this instance is registered.
     *
     * @param e the exception which was thrown
     */
    public abstract void handleError(Exception e);

    /**
     * gets the ContentGeneratorID
     * @return a String
     */
    public String getContentGeneratorID() {
        return contentGeneratorID;
    }
}
