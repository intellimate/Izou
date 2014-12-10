package intellimate.izou.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Used to provide identification.
 * You can obtain an Instance through IdentificationManager
 */
public final class Identification {
    private Identifiable identifiable;
    private boolean createdFromInstance;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    private Identification(Identifiable identifiable, boolean createdFromInstance) {
        this.identifiable = identifiable;
        this.createdFromInstance = createdFromInstance;
    }

    protected static Identification createIdentification(Identifiable identifiable) {
        if(identifiable == null) return null;
        return new Identification(identifiable, false);
    }

    protected static Identification createIdentification(Identifiable identifiable, boolean createdFromInstance) {
        if(identifiable == null) return null;
        return new Identification(identifiable, createdFromInstance);
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

    /**
     * this method returns whether this Identification Object was created by the owner
     * @return true if created by the owner, false if not
     */
    public boolean isCreatedFromInstance() {
        return createdFromInstance;
    }

    /**
     * returns whether this and the other identification belong to the same Identifiable
     * @param identification an instance of identification
     * @return true if they equal, false if not
     */
    public boolean equals(Identification identification) {
        return getID().equals(identification.getID());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if(o instanceof Identifiable) {
            Identifiable that = (Identifiable) o;
            return identifiable.getID().equals(that.getID());
        } else if (o instanceof Identification) {
            Identification that = (Identification) o;
            return equals(that);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return identifiable != null ? identifiable.hashCode() : 0;
    }
}