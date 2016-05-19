package org.intellimate.izou.identification;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * <p>
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
@AddonAccessible
public interface Identification {
    /**
     * returns the ID of the owner of the Identification
     * @return a String containing the ID
     */
    String getID();

    /**
     * this method returns whether this Identification Object was created by the owner
     * @return true if created by the owner, false if not
     */
    boolean isCreatedFromInstance();
}
