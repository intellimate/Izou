package intellimate.izou.system;

/**
 * FileSubscribers are usually paired with {@link ReloadableFile} objects in the {@link FilePublisher}, where their
 * update method is triggered with a file change pertaining to the reloadable file.
 *
 * @author Julian Brendl
 * @version 1.0
 */
public interface FileSubscriber {
    /**
     * Method that is called when a file pertaining to a reloadableFile is changed.
     */
    public void update();
}
