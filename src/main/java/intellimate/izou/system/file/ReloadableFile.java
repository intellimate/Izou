package intellimate.izou.system.file;

import intellimate.izou.identification.Identifiable;

/**
 * Interface for system-manager. System-manager calls on reloadFile when it detects a change in the file. Any class
 * implementing this method can be used by the system-manager.
 */
public interface ReloadableFile extends Identifiable{

    /**
     * Method that system-manager uses to update file when it has detected a change in it.
     * @param eventType the Type which caused the reloading
     */
    public void reloadFile(String eventType);
}
