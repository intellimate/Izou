package intellimate.izou.activator;

import intellimate.izou.events.EventManager;
import intellimate.izou.system.Context;

import java.util.HashMap;

/**
 * The Task of an Activator is to listen for whatever you choose to implement and fires events to notify a change.
 * <p>
 * The Activator always runs in the Background, just overwrite activatorStarts(). To use Activator simply extend from it
 * and hand an instance over to the ActivatorManager.
 */
public abstract class Activator implements Runnable {

    private HashMap<String, EventManager.ActivatorEventCaller> callers = null;
    private EventManager eventManager;
    private ActivatorManager activatorManager;
    //counts the exception
    private int exceptionCount = 0;
    //limit of the exceptionCount
    @SuppressWarnings("FieldCanBeLocal")
    private final int exceptionLimit = 100;
    private Context context;

    /**
     * creates a new activator with a context
     *
     * @param context the context of the addOn (where all "global" or necessary, addOn-wide functionalities are stored)
     *                (Ex: logging)
     */
    public Activator(Context context) {
        this.context = context;
    }

    /**
     * This method implements runnable and should only be called by a Thread.
     */
    @Override
    public void run() {
        try {
            activatorStarts();
        } catch (InterruptedException e) {
            //noinspection UnnecessaryReturnStatement
            return;
        } catch (Exception e) {
            this.exceptionThrown(e);
        }
    }

    /**
     * Starting an Activator causes this method to be called.
     *
     * @throws InterruptedException will be caught by the Activator implementation, used to stop the Activator Thread
     */
    public abstract void activatorStarts() throws InterruptedException;

    /**
     * wrapper for terminated.
     *
     * This method counts the exceptions, if they are above the limit, doesn't call terminated
     * @param e if not null, the exception, which caused the termination
     */
    public final void exceptionThrown(Exception e) {
        exceptionCount++;
        if(exceptionCount < exceptionLimit) {
            if(terminated(e) && activatorManager != null)
            {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    context.getLogger().warn(e.getMessage());
                }
                activatorManager.addActivator(this);
            }
        }
    }

    /**
     * This method gets called when the Activator Thread got exceptionThrown.
     * <p>
     * This is an unusual way of ending a thread. The main reason for this should be, that the activator was interrupted
     * by an uncaught exception.
     *
     * @param e if not null, the exception, which caused the termination
     * @return true if the Thread should be restarted
     */
    public abstract boolean terminated(Exception e);

    /**
     * registers an Event.
     * <p>
     * To fire an event you first have to register it. After that you can call the fireEvent() method.
     * Only use this method inside activatorStarts();
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException thrown if the ID is null or empty
     * @throws java.lang.IllegalStateException thrown if not called inside activatorStarts();
     */
    public void registerEvent(String id) throws IllegalArgumentException, IllegalStateException {
        if (callers == null) {
            throw new IllegalStateException("This method got called outside activatorStarts()");
        }
        else {
            EventManager.ActivatorEventCaller caller = eventManager.registerActivatorCaller(id);
            callers.put(id, caller);
        }
    }

    /**
     * unregister an Event at EventManager.
     * <p>
     * If you don't need an event anymore, you can unregister it .
     *
     * @param id the ID for the Event, format: package.class.name
     * @throws IllegalArgumentException thrown if the ID is null or empty
     * @throws java.lang.IllegalStateException thrown if not called inside activatorStarts();
     */
    public void unregisterEvent(String id) throws IllegalArgumentException, IllegalStateException {
        if (callers == null) {
            throw new IllegalStateException("This method got called outside activatorStarts()");
        }
        EventManager.ActivatorEventCaller caller;
        caller = callers.get(id);
        if (caller == null) {
            throw new IllegalArgumentException();
        }
        eventManager.unregisterActivatorCaller(id, caller);
        callers.remove(id);
    }

    /**
     * unregister all Event at EventManager.
     * <p>
     * If you don't need this class anymore, you should unregister all events to avoid memory leaks.
     * @throws IllegalArgumentException thrown if the ID is null or empty
     * java.lang.IllegalStateException thrown if not called inside activatorStarts();
     */
    public void unregisterAllEvents() throws IllegalArgumentException, IllegalStateException {
        if (callers == null) {
            throw new IllegalStateException("This method got called outside activatorStarts()");
        }
        for (String key : callers.keySet()) {
            EventManager.ActivatorEventCaller caller = callers.get(key);
            if (caller == null) {
                throw new IllegalArgumentException();
            }
            eventManager.unregisterActivatorCaller(key, caller);
        }
        callers.clear();
    }

    /**
     * returns all the registered Events
     *
     * @return an array containing all the registered Events
     */
    public String[] getAllRegisteredEvents() {
        String[] temp = new String[0];
        if (callers == null) {
            return null;
        }
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

    /**
     * registers all needed Dependencies to function
     * @param eventManager an instance of EventManager
     * @param activatorManager an instance of activatorManager
     */
    void registerAllNeededDependencies(EventManager eventManager, ActivatorManager activatorManager) {
        callers = new HashMap<>();
        setEventManager(eventManager);
        setActivatorManager(activatorManager);
    }

    private void setEventManager(EventManager eventManager) {
        this.eventManager = eventManager;

    }

    private void setActivatorManager(ActivatorManager activatorManager) {
        this.activatorManager = activatorManager;
    }

}
