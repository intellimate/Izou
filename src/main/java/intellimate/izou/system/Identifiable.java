package intellimate.izou.system;

/**
 * Makes a class identifiable by forcing implementations to set an ID.
 */
public interface Identifiable
{
    /**
     * An ID must always be unique.
     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
     * If you have to implement this interface multiple times, just concatenate unique Strings to
     * .class.getCanonicalName()
     * @return A String containing an ID
     */
    abstract public String getID();

    /**
     * checks whether this instance is the owner of this Identification
     * @param identification the identification to check
     * @return true if the same, false if not
     */
    default boolean isOwner(Identification identification) {
        return getID().equals(identification.getID());
    }
}
