package org.intellimate.izou.system.file;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * FileSubscribers are usually paired with {@link ReloadableFile} objects in the {@link FilePublisher}, where their
 * update method is triggered with a file change pertaining to the reloadable file.
 */
@AddonAccessible
public interface FileSubscriber {
    /**
     * Method that is called when a file pertaining to a reloadableFile is changed.
     */
    void update();
}
