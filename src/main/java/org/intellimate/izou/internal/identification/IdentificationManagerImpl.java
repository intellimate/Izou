package org.intellimate.izou.internal.identification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * You can register an Object with the IdentificationManager and receive an Identification Objects.
 */
//FIXME: ConcurrentModification Exception with streams
public final class IdentificationManagerImpl implements IdentificationManager {
    private List<Identifiable> identifiables = Collections.synchronizedList(new ArrayList<>());
    private static IdentificationManagerImpl ourInstance = new IdentificationManagerImpl();
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    public static IdentificationManager getInstance() {
        return ourInstance;
    }

    private IdentificationManagerImpl() {

    }

    /**
     * If you have registered with an Identifiable interface, you can receive Identification Instances with this method.
     * @param identifiable the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    @Override
    public Optional<Identification> getIdentification(Identifiable identifiable) {
        if(identifiables.stream()
                .anyMatch(listIdentifiable -> listIdentifiable.getID().equals(identifiable.getID()))) {
            return Optional.of(IdentificationImpl.createIdentification(identifiable, true));
        }
        return Optional.empty();
    }

    /**
     * If a class has registered with an Identifiable interface you can receive an Identification Instance describing
     * the class by providing his ID.
     * @param id the ID of the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    @Override
    public Optional<Identification> getIdentification(String id) {
        Optional<Identifiable> result = identifiables.stream()
                .filter(identifiable1 -> identifiable1.getID().equals(id))
                .findFirst();

        if(!result.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(IdentificationImpl.createIdentification(result.get()));
        }
    }

    /**
     * Registers an Identifiable, ID has to be unique.
     * @param identifiable the Identifiable to register
     * @return true if registered/already registered or false if the ID is already existing
     */
    @Override
    public synchronized boolean registerIdentification(Identifiable identifiable) {
        if (identifiable == null || identifiable.getID() == null || identifiable.getID().isEmpty()) return false;
        if (identifiables.contains(identifiable)  || identifiables.stream()
                .anyMatch(identifiableS -> identifiableS.getID().equals(identifiable.getID())))
            return false;
        identifiables.add(identifiable);
        return true;
    }

    /**
     * returns true if the Identification is valid, false if not
     *
     * @param identification the Identification to test
     * @return true if valid, false if nit
     */
    @Override
    public boolean verify(Identification identification) {
        return identification instanceof IdentificationImpl;
    }
}
