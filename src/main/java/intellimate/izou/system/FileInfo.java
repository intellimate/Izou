package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.WatchKey;

/**
 *
 */
public class FileInfo {
    private Path path;
    private String fileType;
    private AddOn addOn;
    private ReloadableFiles reloadableFiles;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());


    /**
     *
     * @param path
     * @param fileType
     * @param addOn
     */
    public FileInfo(Path path, String fileType, AddOn addOn) {
        this(path, fileType, addOn, null);
    }

    /**
     *
     * @param path
     * @param fileType
     * @param reloadableFiles
     */
    public FileInfo(Path path, String fileType, ReloadableFiles reloadableFiles) {
        this(path, fileType, null, reloadableFiles);
    }

    /**
     *
     * @param path
     * @param fileType
     * @param addOn
     * @param reloadableFiles
     */
    public FileInfo(Path path, String fileType, AddOn addOn, ReloadableFiles reloadableFiles) {
        this.path = path;
        this.fileType = fileType;
        this.addOn = addOn;
        this.reloadableFiles = reloadableFiles;
    }

    /**
     *
     * @return
     */
    public Path getPath() {
        return path;
    }

    /**
     *
     * @return
     */
    public String getFileType() {
        return fileType;
    }

    /**
     *
     * @return
     */
    public AddOn getAddOn() {
        return addOn;
    }

    /**
     *
     * @return
     */
    public ReloadableFiles getReloadableFiles() {
        return reloadableFiles;
    }
}
