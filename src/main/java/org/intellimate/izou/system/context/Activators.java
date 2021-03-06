package org.intellimate.izou.system.context;

import org.intellimate.izou.activator.ActivatorModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IllegalIDException;
import ro.fortsoft.pf4j.AddonAccessible;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface Activators {
    /**
     * adds an activator and automatically submits it to the Thread-Pool
     * @param activatorModel the activator to add
     * @throws IllegalIDException not yet implemented
     */
    void addActivator(ActivatorModel activatorModel) throws IllegalIDException;

    /**
     * removes the activator and stops the Thread
     * @param activatorModel the activator to remove
     */
    void removeActivator(ActivatorModel activatorModel);

    /**
     * returns the ID of the Manager
     * @return an instance of Identification
     */
    Identification getManagerIdentification();
}
