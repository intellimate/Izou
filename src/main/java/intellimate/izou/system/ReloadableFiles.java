package intellimate.izou.system;

/**
 * Interface for system-manager. System-manager calls on reloadFile when it detects a change in the file. Any class
 * implementing this method can be used by the system-manager.
 */
public interface ReloadableFiles {

    /**
     * Method that system-manager uses to update file when it has detected a change in it.
     */
    public void reloadFile(String eventType);
}
