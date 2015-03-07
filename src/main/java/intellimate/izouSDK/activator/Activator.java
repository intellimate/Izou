package intellimate.izouSDK.activator;

import intellimate.izou.events.Event;
import intellimate.izou.events.EventCallable;
import intellimate.izou.events.MultipleEventsException;
import intellimate.izou.identification.IdentificationManager;
import intellimate.izou.system.Context;
import intellimate.izou.threadpool.ExceptionCallback;

/**
 * The Task of an Activator is to listen for whatever you choose to implement and fires events to notify a change.
 * <p>
 * The Activator always runs in the Background, just overwrite activatorStarts(). To use Activator simply extend from it
 * and hand an instance over to the ActivatorManager.
 */
public abstract class Activator implements intellimate.izou.activator.Activator, ExceptionCallback {
    private EventCallable caller;
    private IdentificationManager identificationManager = IdentificationManager.getInstance();
    //counts the exception
    private int exceptionCount = 0;
    //limit of the exceptionCount
    @SuppressWarnings("FieldCanBeLocal")
    private final int exceptionLimit = 100;
    private Context context;

    public Activator(Context context) {
        if(!identificationManager.registerIdentification(this)) {
            context.logger.getLogger().fatal("Failed to register with identification manager" + getID());
        }
        this.context = context;
    }

    /**
     * it this method returns false (and only if it returns false) it will not get restarted once stopped
     * <p>
     * Internally there is a limit of 100 times the activator is allowed to finish exceptionally (everything but
     * returning false)
     * </p>
     *
     * @return true if the activator should get restarted, false if not
     * @throws Exception if unable to compute a result
     */
    @Override
    public Boolean call() throws Exception {
        try {
            activatorStarts();
        } catch (InterruptedException e) {
            return !terminated(e);
        } catch (Exception e) {
            return !terminated(e);
        }
        return false;
    }

    /**
     * Starting an Activator causes this method to be called.
     *
     * @throws InterruptedException will be caught by the Activator implementation, doesn't restart the activator
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
            if(terminated(e))
            {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    context.logger.getLogger().fatal(e);
                }
                //activatorManager.restartActivator(this);
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
     * fires an Event.
     * <p>
     * This triggers all the ContentGenerator instances, that have subscribed to the event.
     * Note that if multiple events get fired simultaneously, a MultipleEventsException gets thrown.
     *
     * @param event the event to fire
     * @throws IllegalArgumentException             thrown if the event is null or empty
     * @throws intellimate.izou.events.MultipleEventsException if there are other addons firing events
     */
    public void fireEvent(Event event) throws IllegalArgumentException, MultipleEventsException {
        if (event == null) {
            throw new IllegalArgumentException();
        }
        caller.fire(event);
    }

    /**
     * returns the associated EventCaller
     * @return the EventCaller
     */
    public EventCallable getCaller() {
        return caller;
    }

    /**
     * returns the Context of the AddOn.
     *
     * Context provides some general Communications.
     *
     * @return an instance of Context.
     */
    public intellimate.izou.system.context.Context getContext() {
        return context;
    }

    /**
     * get identification manager
     * @return get identification manager
     */
    public IdentificationManager getIdentificationManager() {
        return identificationManager;
    }
}
