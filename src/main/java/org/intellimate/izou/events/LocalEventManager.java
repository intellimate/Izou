package org.intellimate.izou.events;

import org.intellimate.izou.IzouModule;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.identification.IdentificationManagerM;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.main.Main;

import java.util.Optional;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class is used to manage local events.
 */
public class LocalEventManager extends IzouModule implements Runnable {
    //here are all the Instances which fire events stored
    private final ConcurrentHashMap<Identification, EventCaller> callers = new ConcurrentHashMap<>();
    //the queue where all the Events are stored
    final BlockingQueue<EventModel> events = new LinkedBlockingQueue<>(1);
    //if false, run() will stop
    private boolean stop = false;
    private final EventCallable eventCallable;

    public LocalEventManager(Main main) {
        super(main);
        IdentificationManagerM identificationManager = IdentificationManager.getInstance();
        identificationManager.registerIdentification(this);
        Optional<EventCallable> eventCallable = identificationManager.getIdentification(this)
                .flatMap(id -> {
                    try {
                        return getMain().getEventDistributor().registerEventPublisher(id);
                    } catch (IllegalIDException e) {
                        log.fatal("Illegal ID for LocalEventManager", e);
                        return Optional.empty();
                    }
                });
        if (!eventCallable.isPresent()) {
            log.fatal("Unable to obtain EventCallable for " + getID());
            System.exit(1);
            this.eventCallable = null;
        } else {
            this.eventCallable = eventCallable.get();
        }
    }

    /**
     * Registers with the EventManager to fire an event.
     *
     * Note: the same Event can be fired from multiple sources.
     * Method is thread-safe.
     *
     * @param identification the Identification of the the instance
     * @return an Optional, empty if already registered
     * @throws IllegalIDException not yet implemented
     */
    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    public Optional<EventCallable> registerCaller(Identification identification) throws IllegalIDException {
        if(identification == null ||
            callers.containsKey(identification)) return Optional.empty();
        EventCaller eventCaller = new EventCaller(events);
        callers.put(identification, eventCaller);
        return Optional.of(eventCaller);
    }

    /**
     * Unregister with the EventManager.
     *
     * Method is thread-safe.
     *
     * @param identification the Identification of the the instance
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void unregisterCaller(Identification identification) {
        if(!callers.containsKey(identification)) return;
        callers.get(identification).localEvents = null;
        callers.remove(identification);
    }

    /**
     * This method fires an Event
     *
     * @param event the fired Event
     * @throws IllegalIDException not yet implemented
     * @throws org.intellimate.izou.events.MultipleEventsException if there is currently another event getting processed
     */
    public void fireEvent(EventModel event) throws IllegalIDException, org.intellimate.izou.events.MultipleEventsException {
        if(events == null) return;
        if(events.isEmpty()) {
            events.add(event);
        } else {
            throw new org.intellimate.izou.events.MultipleEventsException();
        }
    }

    @Override
    public void run() {
        stop = false;
        while (!stop) {
            EventModel event;
            try {
                event = events.take();
                if (!event.getSource().isCreatedFromInstance()) {
                    error("event: " + event + "has invalid source");
                    continue;
                }
                try {
                    eventCallable.fire(event);
                } catch (org.intellimate.izou.events.MultipleEventsException e) {
                    log.error("unable to fire Event", e);
                }
            } catch (InterruptedException e) {
                log.warn(e);
            }
        }
    }

    /**
     * Should stop the EventManager.
     *
     * The method run() is a while-loop that repeats itself as long as a variable isn't true. This sets the variable true
     * but does NOT interrupt the execution! If its not processing, it is waiting for an event, so this Thread still may
     * not stop without interruption.
     */
    @SuppressWarnings("UnusedDeclaration")
    public void stop() {
        stop = true;
    }

    /**
     * Class used to fire events.
     *
     * To fire events a class must register with registerCaller, then this class will be returned.
     * Use fire() to fire the event;
     */
    @SuppressWarnings("SameParameterValue")
    public final class EventCaller implements EventCallable {
        private BlockingQueue<EventModel> localEvents;
        //private, so that this class can only constructed by EventManager
        private EventCaller(BlockingQueue<EventModel> events) {
            this.localEvents = events;
        }

        /**
         * This method is used to fire the event.
         *
         * @throws org.intellimate.izou.events.MultipleEventsException an Exception will be thrown if there are currently other events fired
         */
        public void fire(EventModel event) throws org.intellimate.izou.events.MultipleEventsException {
            if(events.isEmpty()) {
                localEvents.add(event);
            } else {
                throw new org.intellimate.izou.events.MultipleEventsException();
            }
        }
    }

    /**
     * Exception thrown if there are multiple Events fired at the same time.
     */
    @SuppressWarnings("WeakerAccess")
    @Deprecated
    public static class MultipleEventsException extends Exception {
        public MultipleEventsException() {
            super("Multiple Events fired at the same time");
        }
    }
}
