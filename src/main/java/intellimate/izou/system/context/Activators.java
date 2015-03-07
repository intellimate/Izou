package intellimate.izou.system.context;

import intellimate.izou.identification.IllegalIDException;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface Activators {
    /**
     * adds an activator and automatically submits it to the Thread-Pool
     * @param activator the activator to add
     * @throws IllegalIDException not yet implemented
     */
    void addActivator(intellimate.izou.activator.Activator activator) throws IllegalIDException;

    /**
     * removes the activator and stops the Thread
     * @param activator the activator to remove
     */
    void removeActivator(intellimate.izou.activator.Activator activator);
}
