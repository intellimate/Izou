package org.intellimate.izou.system.file;

import org.intellimate.izou.identification.Identifiable;
import ro.fortsoft.pf4j.AddonAccessible;

/**
 * Interface for system-manager. System-manager calls on reloadFile when it detects a change in the file. Any class
 * implementing this method can be used by the system-manager.
 */
@AddonAccessible
public interface ReloadableFile extends Identifiable{

    /**
     * Method that system-manager uses to update file when it has detected a change in it.
     * @param eventType the Type which caused the reloading
     */
    void reloadFile(String eventType);
}
