package intellimate.izou.events;

import java.util.concurrent.BlockingQueue;

/**
 * This class is used to pass Events to the EventDistributor
 */
public final class EventPublisher implements EventCallable {
    //the queue where all the Events are stored
    private final BlockingQueue<Event> events;
    protected EventPublisher(BlockingQueue<Event> events) {
        this.events = events;
    }

    /**
     * use this method to fire Events.
     * @param event the Event to fire
     */
    public void fire(Event event) {
        if(event == null) return;
        events.add(event);
    }
}
