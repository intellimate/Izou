package karlskrone.jarvis.activator;

import karlskrone.jarvis.events.EventManager;

import java.util.HashMap;

/**
 * The Task of an Activator is to listen for whatever you choose to implement and fires events to notify a change.
 *
 * The Activator always runs in the Background, just overwrite activatorStarts(). To use Activator simply extend from it
 * and hand an instance over to the ActivatorManager.
 */
public abstract class Activator implements Runnable{

    private HashMap<String, EventManager.ActivatorEventCaller> callers = new HashMap<>();
    private EventManager manager;

    public Activator (EventManager manager) {
        this.manager = manager;
    }

    @Override
    public void run() {
        try {
            activatorStarts();
        }
        catch (InterruptedException e) {
            return;
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
     *
     * This is an unusual way of ending a thread. The main reason for this should be, that the activator was hanging
     *
     * @deprecated because the logic behind is probably not working yet
     */
    public void activatorTerminated(){};

    /**
     * registers an Event.
     *
     * To fire an event you first have to register it. After that you can call the fireEvent() method.
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException thrown if the id is null or empty
     */
    public void registerEvent(String id) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        EventManager.ActivatorEventCaller caller = manager.registerActivatorEvent(id);
        callers.put(id, caller);
    }

    /**
     * unregisters an Event at EventManager.
     *
     * If you don't need an event anymore, you can unregister it .
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException thrown if the id is null or empty
     */
    public void unregisterEvent(String id) throws IllegalArgumentException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        EventManager.ActivatorEventCaller caller = callers.get(id);
        if (caller == null) {
            throw new IllegalArgumentException();
        }
        manager.unregisterActivatorEvent(id, caller);
        callers.remove(id);
    }

    public String[] getAllRegisteredEvents() {
        String[] temp = new String[0];
        return callers.keySet().toArray(temp);
    }

    /**
     * fires an Event.
     *
     * This triggers all the ContentGenerator instances, that have subscribed to the event.
     * Note that if multiple events get fired simultaneously, a MultipleEventsException gets thrown.
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException thrown if the id is null or empty
     * @throws EventManager.MultipleEventsException thrown if there are other events fired
     */
    public void fireEvent (String id) throws IllegalArgumentException, EventManager.MultipleEventsException {
        if (id == null || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        EventManager.ActivatorEventCaller caller = callers.get(id);
        if (caller == null) {
            throw new IllegalArgumentException();
        }
        caller.fire();
    }
}
