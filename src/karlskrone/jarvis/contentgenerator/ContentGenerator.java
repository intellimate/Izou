package karlskrone.jarvis.contentgenerator;

import karlskrone.jarvis.events.EventManager;

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
public abstract class ContentGenerator<T> implements Callable<ContentData<T>>, EventManager.ActivatorEventListener{
    private ContentGeneratorManager contentGeneratorManager;
    private final EventManager eventManager;
    //stores the eventID, will be retrieved when Thread started
    private String eventID = null;
    //stores all registered events
    private final List<String> registeredEvents = new LinkedList<>();

    public ContentGenerator(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Sets the ContentGenerator.
     *
     * The ContentGeneratorManager will usually be set when you add the ContentGenerator to the ContentGeneratorManager
     * via ContentGeneratorManager.addContentGenerator();
     *
     * @param contentGeneratorManager the contentGeneratorManager
     */
    public void setContentGeneratorManager(ContentGeneratorManager contentGeneratorManager) {
        this.contentGeneratorManager = contentGeneratorManager;
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
     *
     * @param eventID the ID of the Event, format: package.class.name
     * @throws IllegalArgumentException if the id is null or empty
     */
    public void registerEvent(String eventID) throws IllegalArgumentException{
        eventManager.addActivatorEventListener(eventID, this);
        registeredEvents.add(eventID);
    }

    /**
     * Unregisters an Event.
     *
     * @param eventID the ID of the Event, format: package.class.name
     * @throws IllegalArgumentException if the id is null or empty
     */
    public void unregisterEvent(String eventID) throws IllegalArgumentException{
        eventManager.deleteActivatorEventListener(eventID, this);
        registeredEvents.remove(eventID);
    }

    /**
     * Unregisters all events.
     *
     * @throws IllegalArgumentException if this exception will be thrown, there is a problem in the Event-System
     */
    public void unregisterAllEvents() throws IllegalArgumentException{
        for (String id : registeredEvents)
        {
            eventManager.deleteActivatorEventListener(id, this);
        }
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
     * Handles the Errors during Computation.
     *
     * This method handles the errors thrown during Computation by generate(). Note that this can also include
     * Interruption Exceptions if the Thread takes to long.
     *
     * @param e the exception which was thrown
     */
    public abstract void handleError(Exception e);
}
