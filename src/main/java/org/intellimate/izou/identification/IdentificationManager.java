package org.intellimate.izou.identification;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * this interface exists only for backward-capability and will be removed.
 * @author LeanderK
 * @version 1.0
 */
@Deprecated
@AddonAccessible
public interface IdentificationManager extends IdentificationManagerM {
    /**
     * returns the IndentificationManager-Instance
     * @return the instance
     */
    @Deprecated
    static IdentificationManagerM getInstance() {
        return IdentificationManagerImpl.singletonInstance;
    }
}
