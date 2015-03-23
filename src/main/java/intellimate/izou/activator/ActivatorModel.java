package intellimate.izou.activator;

import intellimate.izou.identification.Identifiable;

import java.util.concurrent.Callable;

/**
 * The Task of an Activator is to listen for whatever you choose to implement and fires events to notify a change.
 * <p>
 * The Activator always runs in the Background, just overwrite activatorStarts(). To use Activator simply extend from it
 * and hand an instance over to the ActivatorManager.
 */
public interface ActivatorModel extends Identifiable, Callable<Boolean> {

    /**
     * it this method returns false (and only if it returns false) it will not get restarted once stopped
     * <p>
     * Internally there is a limit of 100 times the activator is allowed to finish exceptionally (everything but
     * returning false)
     * </p>
     * @return true if the activator should get restarted, false if not
     * @throws Exception if unable to compute a result
     */
    @Override
    Boolean call() throws Exception;
}
