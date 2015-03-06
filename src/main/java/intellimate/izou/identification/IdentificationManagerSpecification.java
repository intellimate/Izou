package intellimate.izou.identification;

import java.util.Optional;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface IdentificationManagerSpecification {
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
