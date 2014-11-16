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
    abstract String getID();
}
