package karlskrone.jarvis.activator;

import karlskrone.jarvis.events.EventManager;

import java.util.HashMap;

/**
 * The Task of an Activator is to listen for whatever you choose to implement and fires events to notify a change.
 * <p>
 * The Activator always runs in the Background, just overwrite activatorStarts(). To use Activator simply extend from it
 * and hand an instance over to the ActivatorManager.
 */
public abstract class Activator implements Runnable {

    private final HashMap<String, EventManager.ActivatorEventCaller> callers = new HashMap<>();
    private final EventManager manager;


    public Activator(EventManager manager) {
        this.manager = manager;
    }

    /**
     * This method implements runnable and should only be called by a Thread.
     */
    @Override
    public void run() {
        try {
            activatorStarts();
        } catch (InterruptedException e) {
        } catch (Exception e) {
            this.terminated(e);
        }
    }

    /**
     * Starting an Activator causes this method to be called.
     *
     * @throws InterruptedException will be caught by the Activator implementation, used to stop the Activator Thread
     */
    public abstract void activatorStarts() throws InterruptedException;

    /**
     * This method gets called when the Activator Thread got terminated.
     * <p>
     * This is an unusual way of ending a thread. The main reason for this should be, that the activator was interrupted
     * by an uncaught exception.
     *
     * @param e if not null, the exception, which caused the termination
     */
    public abstract void terminated(Exception e);

    /**
     * registers an Event.
     * <p>
     * To fire an event you first have to register it. After that you can call the fireEvent() method.
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException thrown if the ID is null or empty
     */
    public void registerEvent(String id) throws IllegalArgumentException {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorEvent(id);
        callers.put(id, caller);
    }

    /**
     * unregisters an Event at EventManager.
     * <p>
     * If you don't need an event anymore, you can unregister it .
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException thrown if the ID is null or empty
     */
    public void unregisterEvent(String id) throws IllegalArgumentException {
        EventManager.ActivatorEventCaller caller = callers.get(id);
        if (caller == null) {
            throw new IllegalArgumentException();
        }
        manager.unregisterActivatorEvent(id, caller);
        callers.remove(id);
    }

    /**
     * returns all the registered Events
     *
     * @return an array containing all the registered Events
     */
    public String[] getAllRegisteredEvents() {
        String[] temp = new String[0];
        return callers.keySet().toArray(temp);
    }

    /**
     * fires an Event.
     * <p>
     * This triggers all the ContentGenerator instances, that have subscribed to the event.
     * Note that if multiple events get fired simultaneously, a MultipleEventsException gets thrown.
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException             thrown if the ID is null or empty
     * @throws EventManager.MultipleEventsException thrown if there are other events fired
     */
    public void fireEvent(String id) throws IllegalArgumentException, EventManager.MultipleEventsException {
        EventManager.ActivatorEventCaller caller = callers.get(id);
        if (caller == null) {
            throw new IllegalArgumentException();
        }
        caller.fire();
    }
}
