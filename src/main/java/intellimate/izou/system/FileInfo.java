package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;

/**
 * Information for files registered in the FileManager, it is what is processed when a file-event has been raised in the
 * file manager
 */
public class FileInfo {
    private Path path;
    private String fileType;
    private AddOn addOn;
    private ReloadableFile reloadableFile;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());


    /**
     * creates a new FileInfo object
     *
     * @param path path of DIRECTORY (not file itself!!!!) where file to be watched is located
     * @param fileType the type of file the file is, can be name or extension or both
     *                 (Ex: "test", "txt", "test.txt" all work)
     * @param addOn the addOn that requires the file
     */
    public FileInfo(Path path, String fileType, AddOn addOn) {
        this(path, fileType, addOn, null);
    }

    /**
     * creates a new FileInfo object
     *
     * @param path path of DIRECTORY (not file itself!!!!) where file to be watched is located
     * @param fileType the type of file the file is, can be name or extension or both
     *                 (Ex: "test", "txt", "test.txt" all work)
     * @param reloadableFile interface that includes update method
     */
    public FileInfo(Path path, String fileType, ReloadableFile reloadableFile) {
        this(path, fileType, null, reloadableFile);
    }

    /**
     * creates a new FileInfo object
     *
     * @param path path of DIRECTORY (not file itself!!!!) where file to be watched is located
     * @param fileType the type of file the file is, can be name or extension or both
     *                 (Ex: "test", "txt", "test.txt" all work)
     * @param addOn the addOn that requires the file
     * @param reloadableFile interface that includes update method
     */
    public FileInfo(Path path, String fileType, AddOn addOn, ReloadableFile reloadableFile) {
        this.path = path;
        this.fileType = fileType;
        this.addOn = addOn;
        this.reloadableFile = reloadableFile;
    }

    /**
     * gets directory path of fileInfo
     *
     * @return the directory path of fileInfo
     */
    public Path getPath() {
        return path;
    }

    /**
     * gets file type or name of file
     *
     * @return the file type or name of file
     */
    public String getFileType() {
        return fileType;
    }

    /**
     * gets the addOn to which the registered file belongs
     *
     * @return the addOn to which the registered file belongs
     */
    public AddOn getAddOn() {
        return addOn;
    }

    /**
     * gets the reloadable files to which the registered file belongs
     *
     * @return the reloadable files to which the registered file belongs
     */
    public ReloadableFile getReloadableFile() {
        return reloadableFile;
    }

    /**
     * Gets id of file-info
     *
     * @return the id of the file-info
     */
    public String getID() {
        if (addOn != null) {
            return addOn.getID();
        } else if (reloadableFile != null) {
            return reloadableFile.getID();
        }
        return null;
    }
}
