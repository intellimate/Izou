package intellimate.izou.events;

import java.util.concurrent.BlockingQueue;

/**
 * This class is used to pass Events to the EventDistributor
 */
public class EventPublisher {
    //the queue where all the Events are stored
    private final BlockingQueue<Event> events;
    protected EventPublisher(BlockingQueue<Event> events) {
        this.events = events;
    }

    public void fireEvent(Event event) {
        events.add(event);
    }
}
