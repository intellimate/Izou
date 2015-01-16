package intellimate.izou.system;

/**
 * Interface for system-manager. System-manager calls on reloadFile when it detects a change in the file. Any class
 * implementing this method can be used by the system-manager.
 */
public interface ReloadableFile {

    /**
     * Method that system-manager uses to update file when it has detected a change in it.
     * @param eventType the Type which caused the reloading
     */
    public void reloadFile(String eventType);

    /**
     * Get ID of reloadableFile object
     *
     * @return the ID of reloadableFile object
     */
    public String getID();
}
