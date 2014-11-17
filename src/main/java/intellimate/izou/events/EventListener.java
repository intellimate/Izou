package intellimate.izou.events;

import intellimate.izou.contentgenerator.ContentData;

import java.util.concurrent.Future;

/**
 * Interface for listening to events.
 *
 * To receive events a class must implements this interface and register with the addActivatorEventListener-method.
 * When the activator event occurs, that object's eventFired method is invoked.
 */
public interface EventListener {

    /**
     * Invoked when an activator-event occurs.
     *
     * @param id the ID of the Event, format: package.class.name
     * @return a Future representing pending completion of the task
     */
    public Future<ContentData> eventFired(String id);
}
