package intellimate.izou.system.file;

import intellimate.izou.IdentificationSet;
import intellimate.izou.IzouModule;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IllegalIDException;
import intellimate.izou.main.Main;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

/**
 * FilePublisher that notifies {@link FileSubscriberModel} objects. It is triggered when a file is reloaded. Particularly
 * file subscribers are mapped to {@link ReloadableFileModel} objects. When a reloadable file is reloaded, all file
 * subscribers belonging to it are notified. All file subscribers in general can also be notified.
 */
public class FilePublisher extends IzouModule {
    private HashMap<ReloadableFileModel, IdentificationSet<FileSubscriberModel>> fileSubscribers;
    private IdentificationSet<FileSubscriberModel> defaultFileSubscribers;

    /**
     * Creates a new FilePublisher object. There should only be one in Izou
     * @param main an Instance of main, used to get all the other classes
     */
    public FilePublisher(Main main) {
        super(main);
        this.fileSubscribers = new HashMap<>();
        this.defaultFileSubscribers = new IdentificationSet<>(false);
    }

    /**
     * Registers a {@link FileSubscriberModel} with a {@link ReloadableFileModel}. So when the {@code reloadableFile} is reloaded,
     * the fileSubscriber will be notified. Multiple file subscribers can be registered with the same reloadable file.
     *
     * @param reloadableFile the reloadable file that should be observed
     * @param fileSubscriber the fileSubscriber that should be notified when the reloadable file is reloaded
     * @param identification the Identification of the FileSubscriber
     * @throws IllegalIDException not yet implemented
     */
    public void register(ReloadableFileModel reloadableFile, FileSubscriberModel fileSubscriber, Identification identification)
                                                                                            throws IllegalIDException {
        IdentificationSet<FileSubscriberModel> subscribers = fileSubscribers.get(reloadableFile);

        if (subscribers == null) {
            subscribers = new IdentificationSet<>(false);
            fileSubscribers.put(reloadableFile, subscribers);
        }
        subscribers.add(fileSubscriber, identification);
    }

    /**
     * Registers a {@link FileSubscriberModel} so that whenever any file is reloaded, the fileSubscriber is notified.
     *
     * @param fileSubscriber the fileSubscriber that should be notified when the reloadable file is reloaded
     * @param identification the Identification of the FileSubscriber
     * @throws IllegalIDException not yet implemented
     */
    public void register(FileSubscriberModel fileSubscriber, Identification identification) throws IllegalIDException {
        defaultFileSubscribers.add(fileSubscriber, identification);
    }

    /**
     * Unregisters all instances of fileSubscriber found.
     *
     * @param fileSubscriber the fileSubscriber to unregister
     */
    public void unregister(FileSubscriberModel fileSubscriber) {
        for (IdentificationSet<FileSubscriberModel> subList : fileSubscribers.values()) {
            subList.remove(fileSubscriber);
        }
        defaultFileSubscribers.remove(fileSubscriber);
    }

    /**
     * Notifies all file subscribers registered to {@code reloadableFile}
     *
     * @param reloadableFile the ReloadableFile object for which to notify all pertaining file subscribers
     */
    public synchronized void notifyFileSubscribers(ReloadableFileModel reloadableFile) {
        notifyDefaultFileSubscribers();

        IdentificationSet<FileSubscriberModel> subList = fileSubscribers.get(reloadableFile);
        if (subList == null) {
            return;
        }

        for (FileSubscriberModel sub : subList) {
            CompletableFuture.runAsync(sub::update, main.getThreadPoolManager().getAddOnsThreadPool());
        }
    }

    /**
     * Notifies all file subscribers.
     */
    public synchronized void notifyAllFileSubcribers() {
        for (IdentificationSet<FileSubscriberModel> subList : fileSubscribers.values()) {
            for (FileSubscriberModel sub : subList) {
                CompletableFuture.runAsync(sub::update, main.getThreadPoolManager().getAddOnsThreadPool());
            }
        }

        notifyDefaultFileSubscribers();
    }

    /**
     * Notifies all default file subscribers, that is those that will all be notified no matter what.
     */
    public synchronized void notifyDefaultFileSubscribers() {
        for (FileSubscriberModel sub : defaultFileSubscribers) {
            CompletableFuture.runAsync(sub::update, main.getThreadPoolManager().getAddOnsThreadPool());
        }
    }

    /**
     * Get all subscribers for a {@code reloadableFile}
     *
     * @param reloadableFile the {@code reloadableFile} for which to get all subscribers
     * @return all subscribers for a {@code reloadableFile}
     */
    public IdentificationSet<FileSubscriberModel> getFileSubscribersForReloadableFile(ReloadableFileModel reloadableFile) {
        return fileSubscribers.get(reloadableFile);
    }
}
