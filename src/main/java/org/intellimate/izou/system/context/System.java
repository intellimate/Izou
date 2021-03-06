package org.intellimate.izou.system.context;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * this interface provides various methods to interact with Izou-parts not fitting in the other context-categories.
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
public interface System {
    /**
     * this method registers an listener which will be fired when all the addons finished registering.
     * @param runnable the runnable to register.
     */
    void registerInitializedListener(Runnable runnable);
}
