package org.intellimate.izou.identification;

import ro.fortsoft.pf4j.AddonAccessible;

import java.util.Optional;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface IdentificationManagerM {

    /**
     * Returns the IdentificationManager instance
     * @return the instance
     */
    static IdentificationManagerM getInstance() {
        return IdentificationManagerImpl.singletonInstance;
    }

    /**
     * If you have registered with an Identifiable interface, you can receive Identification Instances with this method.
     * @param identifiable the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    Optional<Identification> getIdentification(Identifiable identifiable);

    /**
     * If a class has registered with an Identifiable interface you can receive an Identification Instance describing
     * the class by providing his ID.
     * @param id the ID of the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    Optional<Identification> getIdentification(String id);

    /**
     * Registers an Identifiable, ID has to be unique.
     * @param identifiable the Identifiable to register
     * @return true if registered/already registered or false if the ID is already existing
     */
    boolean registerIdentification(Identifiable identifiable);
}
