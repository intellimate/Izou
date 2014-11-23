package intellimate.izou.system;

import java.util.LinkedList;
import java.util.Optional;

/**
 * You can register an Object with the IdentificationManager and receive an Identification Objects.
 */
public class IdentificationManager {
    private LinkedList<Identifiable> identifiables = new LinkedList<>();
    private static IdentificationManager ourInstance = new IdentificationManager();

    public static IdentificationManager getInstance() {
        return ourInstance;
    }

    private IdentificationManager() {

    }

    /**
     * If you have registered with an Identifiable interface, you can receive Identification Instances with this method.
     * @param identifiable the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    public Optional<Identification> getIdentification(Identifiable identifiable) {
        if(identifiables.stream()
                .anyMatch(listIdentifiable -> listIdentifiable == identifiable)) {
            return Optional.of(Identification.createIdentification(identifiable, true));
        }
        return Optional.empty();
    }

    /**
     * If a class has registered with an Identifiable interface you can receive an Identification Instance describing
     * the class by providing his ID.
     * @param id the ID of the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    public Optional<Identification> getIdentification(String id) {
        Optional<Identifiable> result = identifiables.stream()
                .filter(identifiable1 -> identifiable1.getID().equals(id))
                .findFirst();

        if(!result.isPresent()) {
            return Optional.empty();
        } else {
            return Optional.of(Identification.createIdentification(result.get()));
        }
    }

    /**
     * Registers an Identifiable, ID has to be unique.
     * @return true if registered/already registered of false if the ID is already existing
     */
    public boolean registerIdentification(Identifiable identifiable) {
        if(identifiable == null) return false;
        if(identifiables.contains(identifiable)  || identifiables.stream()
                .anyMatch(identifiableS -> identifiableS.getID().equals(identifiable.getID())))
            return true;
        identifiables.add(identifiable);
        return true;
    }
}
