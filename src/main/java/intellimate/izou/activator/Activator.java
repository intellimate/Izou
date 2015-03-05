package intellimate.izou.activator;

import intellimate.izou.system.Identifiable;

/**
 * The Task of an Activator is to listen for whatever you choose to implement and fires events to notify a change.
 * <p>
 * The Activator always runs in the Background, just overwrite activatorStarts(). To use Activator simply extend from it
 * and hand an instance over to the ActivatorManager.
 */
public interface Activator extends Identifiable, Runnable {

    /**
     * Starts the activator
     */
    @Override
    void run();
}
