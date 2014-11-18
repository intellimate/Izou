package intellimate.izou.system;

/**
 * Used to provide identification.
 * You can obtain an Instance through IdentificationManager
 */
public class Identification {
    private Identifiable identifiable;
    private Identification(Identifiable identifiable) {
        this.identifiable = identifiable;
    }

    static Identification createIdentification(Identifiable identifiable) {
        if(identifiable == null) return null;
        return new Identification(identifiable);
    }

    /**
     * returns the ID of the owner of the Identification
     * @return a String containing the ID
     */
    public String getID() {
        return identifiable.getID();
    }

    /**
     * returns the Identifiable object of the Owner
     * @return a instance of Identifiable
     */
    Identifiable getIdentifiable() {
        return identifiable;
    }
}
