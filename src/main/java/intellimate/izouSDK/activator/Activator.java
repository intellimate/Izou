package intellimate.izouSDK.activator;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.events.Event;
import intellimate.izou.events.LocalEventManager;
import intellimate.izou.events.MultipleEventsException;
import intellimate.izou.system.Context;
import intellimate.izou.identification.Identifiable;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IdentificationManager;
import intellimate.izou.threadpool.ExceptionCallback;

import java.util.Optional;

/**
 * The Task of an Activator is to listen for whatever you choose to implement and fires events to notify a change.
 * <p>
 * The Activator always runs in the Background, just overwrite activatorStarts(). To use Activator simply extend from it
 * and hand an instance over to the ActivatorManager.
 */
public abstract class Activator implements intellimate.izou.activator.Activator, Runnable, Identifiable,
        ExceptionCallback {
    private LocalEventManager.EventCaller caller;
    private LocalEventManager localEventManager;
    private ActivatorManager activatorManager;
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
     * This method implements runnable and should only be called by a Thread.
     */
    @Override
    public void run() {
        try {
            activatorStarts();
        } catch (InterruptedException e) {
            context.logger.getLogger().warn("Unable to start activator", e);
            //noinspection UnnecessaryReturnStatement
            return;
        } catch (Exception e) {
            context.logger.getLogger().warn("Unable to start activator - trying again", e);
            this.exceptionThrown(e);
        }
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
            if(terminated(e) && activatorManager != null)
            {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e1) {
                    context.logger.getLogger().fatal(e);
                }
                activatorManager.restartActivator(this);
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
     * unregister the Caller at the EventManager.
     * <p>
     * If you don't need this class anymore, you should unregister the caller to avoid memory leaks.
     */
    public void unregisterCaller() {
        if (caller == null) return;
        localEventManager.unregisterCaller(identificationManager.getIdentification(this).get());
    }

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
    public LocalEventManager.EventCaller getCaller() {
        return caller;
    }

    private void setLocalEventManager(LocalEventManager localEventManager) {
        this.localEventManager = localEventManager;
        Optional<Identification> identification = identificationManager.getIdentification(this);
        if(!identification.isPresent()) {
            context.logger.getLogger().fatal("Identification not found while setting local event manager");
            return;
        }
        Optional<LocalEventManager.EventCaller> result = localEventManager.registerCaller(identification.get());
        //noinspection StatementWithEmptyBody
        if(!result.isPresent()) {
            context.logger.getLogger().fatal("Unable to register with local event manager");
            return;
        }
        caller = result.get();
    }

    /**
     * returns the Context of the AddOn.
     *
     * Context provides some general Communications.
     *
     * @return an instance of Context.
     */
    public Context getContext() {
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
