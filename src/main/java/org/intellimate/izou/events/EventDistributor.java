package org.intellimate.izou.events;

import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.resource.ResourceModel;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * This class gets all the Events from all registered EventPublisher, generates Resources and passes them to the
 * OutputManager. Can also be used to fire Events Concurrently.
 */
public class EventDistributor extends IzouModule implements Runnable, AddonThreadPoolUser {
    private BlockingQueue<EventModel<?>> events = new LinkedBlockingQueue<>();
    private ConcurrentHashMap<Identification, EventPublisher> registered = new ConcurrentHashMap<>();
    //here are all the Instances to to control the Event-dispatching stored
    private final ConcurrentLinkedQueue<EventsControllerModel> eventsControllers = new ConcurrentLinkedQueue<>();
    //here are all the Listeners stored
    private final ConcurrentHashMap<String, ArrayList<EventListenerModel>> listeners = new ConcurrentHashMap<>();
    private boolean stop = false;

    public EventDistributor(Main main) {
        super(main);
        main.getThreadPoolManager().getIzouThreadPool().submit(this);
    }

    /**
     * fires the event concurrently, this is generally discouraged.
     * <p>
     * This method should not be used for normal Events, for for events which obey the following laws:<br>
     * 1. they are time critical.<br>
     * 2. addons are not expected to react in any way beside a small update<br>
     * 3. they are few.<br>
     * if your event matches the above laws, you may consider firing it concurrently.
     * </p>
     * @param eventModel the EventModel
     */
    public void fireEventConcurrently(EventModel<?> eventModel) {
        if(eventModel == null) return;
        submit(() -> processEvent(eventModel));
    }

    /**
     * with this method you can register EventPublisher add a Source of Events to the System.
     * <p>
     * This method represents a higher level of abstraction! Use the EventManager to fire Events!
     * This method is intended for use cases where you have an entire new source of events (e.g. network)
     * @param identification the Identification of the Source
     * @return An Optional Object which may or may not contains an EventPublisher
     * @throws IllegalIDException not yet implemented
     */
    public Optional<EventCallable> registerEventPublisher(Identification identification) throws IllegalIDException {
        if(registered.containsKey(identification)) return Optional.empty();
        EventPublisher eventPublisher = new EventPublisher(events);
        registered.put(identification, eventPublisher);
        return Optional.of(eventPublisher);
    }

    /**
     * with this method you can unregister EventPublisher add a Source of Events to the System.
     * <p>
     * This method represents a higher level of abstraction! Use the EventManager to fire Events!
     * This method is intended for use cases where you have an entire new source of events (e.g. network)
     * @param identification the Identification of the Source
     */
    public void unregisterEventPublisher(Identification identification) {
        if(!registered.containsKey(identification)) return;
        registered.remove(identification);
    }

    /**
     * Registers an EventController to control EventDispatching-Behaviour
     * <p>
     * Method is thread-safe.
     * It is expected that this method executes quickly.
     *
     * @param controller the EventController Interface to control event-dispatching
     * @throws IllegalIDException not yet implemented
     */
    public void registerEventsController(EventsControllerModel controller) throws IllegalIDException  {
        eventsControllers.add(controller);
    }

    /**
     * Unregisters an EventController
     * <p>
     * Method is thread-safe.
     *
     * @param controller the EventController Interface to remove
     */
    public void unregisterEventsController(EventsControllerModel controller) {
        eventsControllers.remove(controller);
    }


    /**
     * Adds an listener for events.
     * <p>
     * Be careful with this method, it will register the listener for ALL the informations found in the Event. If your
     * event-type is a common event type, it will fire EACH time!.
     * It will also register for all Descriptors individually!
     * It will also ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     * </p>
     * @param event the Event to listen to (it will listen to all descriptors individually!)
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     * @throws IllegalIDException not yet implemented
     */
    @SuppressWarnings({"SynchronizationOnLocalVariableOrMethodParameter"})
    public void registerEventListener(EventModel event, EventListenerModel eventListener) throws IllegalIDException {
        registerEventListener(event.getAllInformations(), eventListener);
    }

    /**
     * Adds an listener for events.
     * <p>
     * It will register for all ids individually!
     * This method will ignore if this listener is already listening to an Event.
     * Method is thread-safe.
     * </p>
     * @param ids this can be type, or descriptors etc.
     * @param eventListener the ActivatorEventListener-interface for receiving activator events
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void registerEventListener(List<String> ids, EventListenerModel eventListener) {
        for(String id : ids) {
            ArrayList<EventListenerModel> listenersList = listeners.get(id);
            if (listenersList == null) {
                listeners.put(id, new ArrayList<>());
                listenersList = listeners.get(id);
            }
            if (!listenersList.contains(eventListener)) {
                synchronized (listenersList) {
                    listenersList.add(eventListener);
                }
            }
        }
    }

    /**
     * unregister an EventListener
     *
     * It will unregister for all Descriptors individually!
     * It will also ignore if this listener is not listening to an Event.
     * Method is thread-safe.
     *
     * @param event the Event to stop listen to
     * @param eventListener the ActivatorEventListener used to listen for events
     * @throws IllegalArgumentException if Listener is already listening to the Event or the id is not allowed
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public void unregisterEventListener(EventModel<EventModel> event, EventListenerModel eventListener) throws IllegalArgumentException {
        for (String id : event.getAllInformations()) {
            ArrayList<EventListenerModel> listenersList = listeners.get(id);
            if (listenersList == null) {
                return;
            }
            synchronized (listenersList) {
                listenersList.remove(eventListener);
            }
        }
    }

    /**
     * Checks whether to dispatch an event
     *
     * @param event the fired Event
     * @return true if the event should be fired
     */
    private boolean checkEventsControllers(EventModel event) {
        List<CompletableFuture<Boolean>> collect = eventsControllers.stream()
                .map(controller -> submit(() -> controller.controlEventDispatcher(event)))
                .collect(Collectors.toList());
        try {
            collect = timeOut(collect, 1000);
        } catch (InterruptedException e) {
            debug("interrupted");
        }
        return collect.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .noneMatch(bool -> !bool);
    }

    public BlockingQueue<EventModel<?>> getEvents() {
        return events;
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        while(!stop) {
            try {
                EventModel<?> event = events.take();
                processEvent(event);
            } catch (InterruptedException e) {
                log.warn("interrupted", e);
            }
        }
    }

    /**
     * process the Event
     * @param event the event to process
     */
    private void processEvent(EventModel<?> event) {
        if (!event.getSource().isCreatedFromInstance()) {
            error("event: " + event + "has invalid source");
            return;
        }
        debug("EventFired: " + event.toString() + " from " + event.getSource().getID());
        if (checkEventsControllers(event)) {
            List<ResourceModel> resourceList = getMain().getResourceManager().generateResources(event);
            event.addResources(resourceList);
            List<EventListenerModel> listenersTemp = event.getAllInformations().parallelStream()
                    .map(listeners::get)
                    .filter(Objects::nonNull)
                    .flatMap(Collection::stream)
                    .distinct()
                    .collect(Collectors.toList());

            List<CompletableFuture> futures = listenersTemp.stream()
                    .map(eventListener -> submit(() -> eventListener.eventFired(event)))
                    .collect(Collectors.toList());
            try {
                timeOut(futures, 1000);
            } catch (InterruptedException e) {
                error("interrupted", e);
            }
            getMain().getOutputManager().passDataToOutputPlugins(event);
        }
    }

    /**
     * stops the EventDistributor
     */
    public void stop() {
        stop = true;
    }

    /**
     * This class is used to pass Events to the EventDistributor
     */
    private class EventPublisher implements EventCallable {
        //the queue where all the Events are stored
        private final BlockingQueue<EventModel<?>> events;
        protected EventPublisher(BlockingQueue<EventModel<?>> events) {
            this.events = events;
        }

        /**
         * use this method to fire Events.
         * @param event the Event to fire
         */
        public void fire(EventModel event) {
            if(event == null) return;
            events.add(event);
        }
    }
}
