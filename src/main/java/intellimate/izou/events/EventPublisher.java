package intellimate.izou.events;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * use this method to fire Events.
     * @param event the Event to fire
     */
    public void fireEvent(Event event) {
        if(event == null) return;
        events.add(event);
    }
}
