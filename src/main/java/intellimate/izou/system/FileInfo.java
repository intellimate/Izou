package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.WatchKey;

/**
 * Information for files registered in the FileManager, it is what is processed when a file-event has been raised in the
 * file manager
 */
public class FileInfo {
    private Path path;
    private String fileType;
    private AddOn addOn;
    private ReloadableFiles reloadableFiles;
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
     * @param reloadableFiles interface that includes update method
     */
    public FileInfo(Path path, String fileType, ReloadableFiles reloadableFiles) {
        this(path, fileType, null, reloadableFiles);
    }

    /**
     * creates a new FileInfo object
     *
     * @param path path of DIRECTORY (not file itself!!!!) where file to be watched is located
     * @param fileType the type of file the file is, can be name or extension or both
     *                 (Ex: "test", "txt", "test.txt" all work)
     * @param addOn the addOn that requires the file
     * @param reloadableFiles interface that includes update method
     */
    public FileInfo(Path path, String fileType, AddOn addOn, ReloadableFiles reloadableFiles) {
        this.path = path;
        this.fileType = fileType;
        this.addOn = addOn;
        this.reloadableFiles = reloadableFiles;
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
    public ReloadableFiles getReloadableFiles() {
        return reloadableFiles;
    }
}
