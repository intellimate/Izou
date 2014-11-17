package intellimate.izou.system;

import intellimate.izou.addon.AddOn;

import java.nio.file.Path;
import java.nio.file.WatchKey;

public class FileInfo {
    private Path path;
    private String fileType;
    private AddOn addOn;
    private ReloadableFiles reloadableFiles;

    public FileInfo(Path path, String fileType, AddOn addOn) {
        this(path, fileType, addOn, null);
    }

    public FileInfo(Path path, String fileType, ReloadableFiles reloadableFiles) {
        this(path, fileType, null, reloadableFiles);
    }

    public FileInfo(Path path, String fileType, AddOn addOn, ReloadableFiles reloadableFiles) {
        this.path = path;
        this.fileType = fileType;
        this.addOn = addOn;
        this.reloadableFiles = reloadableFiles;
    }

    public Path getPath() {
        return path;
    }


    public String getFileType() {
        return fileType;
    }

    public AddOn getAddOn() {
        return addOn;
    }

    public ReloadableFiles getReloadableFiles() {
        return reloadableFiles;
    }
}
