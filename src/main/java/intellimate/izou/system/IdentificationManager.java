package intellimate.izou.system;

import java.util.LinkedList;

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
    public Identification getIdentification(Identifiable identifiable) {
        if(identifiables.contains(identifiable)) {
            return new Identification(identifiable);
        }
        return null;
    }

    /**
     * If a class has registered with an Identifiable interface you can receive an Identification Instance describing
     * the class by providing his ID.
     * @param id the ID of the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    public Identification getIdentification(String id) {
        for(Identifiable identifiable : identifiables) {
            if(identifiable.getID().equals(id)) {
                return new Identification(identifiable);
            }
        }
        return null;
    }

    /**
     * Registers an Identifiable, ID has to be unique.
     * @return true if registered/already registered of false if the ID is already existing
     */
    public boolean registerIdentification(Identifiable identifiable) {
        if(identifiable == null) return false;
        if(identifiables.contains(identifiable)) return true;
        for(Identifiable identifiable1 : identifiables) {
            if(identifiable1.getID().equals(identifiable.getID())) {
                return false;
            }
        }
        identifiables.add(identifiable);
        return true;
    }
}
