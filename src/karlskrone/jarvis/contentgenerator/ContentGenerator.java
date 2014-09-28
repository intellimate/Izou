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
    private EventManager eventManager;
    //stores the eventID, will be retrieved when Thread started
    private String eventID = null;
    //stores all registered events
    List<String> registeredEvents = new LinkedList<>();

    public ContentGenerator(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     *
     * @param contentGeneratorManager
     */
    public void setContentGeneratorManager(ContentGeneratorManager contentGeneratorManager) {
        this.contentGeneratorManager = contentGeneratorManager;
    }

    @Override
    public Future<ContentData> activatorEventFired(String id) {
        this.eventID = id;
        return contentGeneratorManager.runContentGenerator(this);
    }

    @Override
    public  ContentData<T> call() throws Exception {
        try {
            ContentData<T> contentData = generate(eventID);
            eventID = null;
            return contentData;
        } catch (Exception e) {
            handleError(e);
        }
        return null;
    }

    public void registerEvent(String eventID) throws IllegalArgumentException{
        eventManager.addActivatorEventListener(eventID, this);
        registeredEvents.add(eventID);
    }

    public void unregisterEvent(String eventID) throws IllegalArgumentException{
        eventManager.deleteActivatorEventListener(eventID, this);
        registeredEvents.remove(eventID);
    }

    public void unregisterAllEvents() throws IllegalArgumentException{
        for (String id : registeredEvents)
        {
            eventManager.deleteActivatorEventListener(id, this);
        }
        registeredEvents.clear();
    }

    public List<String> getRegisteredEvents() {
        return registeredEvents;
    }

    public abstract ContentData<T> generate(String eventID) throws Exception;

    public abstract void handleError(Exception e) throws Exception;
}
