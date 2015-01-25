package intellimate.izou.system;

import intellimate.izou.main.Main;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * FilePublisher that notifies {@link FileSubscriber} objects. It is triggered when a file is reloaded. Particularly
 * file subscribers are mapped to {@link ReloadableFile} objects. When a reloadable file is reloaded, all file
 * subscribers belonging to it are notified. All file subscribers in general can also be notified.
 */
public class FilePublisher {
    private HashMap<ReloadableFile, List<FileSubscriber>> fileSubscribers;
    private List<FileSubscriber> defaultFileSubscribers;
    private Main main;

    /**
     * Creates a new FilePublisher object. There should only be one in Izou
     */
    public FilePublisher(Main main) {
        this.fileSubscribers = new HashMap<>();
        this.defaultFileSubscribers = new ArrayList<>();
        this.main = main;
    }

    /**
     * Registers a {@link FileSubscriber} with a {@link ReloadableFile}. So when the {@code reloadableFile} is reloaded,
     * the fileSubscriber will be notified. Multiple file subscribers can be registered with the same reloadable file.
     *
     * @param reloadableFile the reloadable file that should be observed
     * @param fileSubscriber the fileSubscriber that should be notified when the reloadable file is reloaded
     */
    public void register(ReloadableFile reloadableFile, FileSubscriber fileSubscriber) {
        List<FileSubscriber> subscribers = fileSubscribers.get(reloadableFile);

        if (subscribers == null) {
            List<FileSubscriber> subList = new ArrayList<>();
            subList.add(fileSubscriber);
            fileSubscribers.put(reloadableFile, subList);
        } else {
            subscribers.add(fileSubscriber);
        }
    }

    /**
     * Registers a {@link FileSubscriber} so that whenever any file is reloaded, the fileSubscriber is notified.
     *
     * @param fileSubscriber the fileSubscriber that should be notified when the reloadable file is reloaded
     */
    public void register(FileSubscriber fileSubscriber) {
        defaultFileSubscribers.add(fileSubscriber);
    }

    /**
     * Unregisters all instances of fileSubscriber found.
     *
     * @param fileSubscriber the fileSubscriber to unregister
     */
    public void unregister(FileSubscriber fileSubscriber) {
        for (List<FileSubscriber> subList : fileSubscribers.values()) {
            subList.remove(fileSubscriber);
        }
        defaultFileSubscribers.remove(fileSubscriber);
    }

    /**
     * Notifies all file subscribers registered to {@code reloadableFile}
     *
     * @param reloadableFile the ReloadableFile object for which to notify all pertaining file subscribers
     */
    public synchronized void notifyFileSubcribers(ReloadableFile reloadableFile) {
        notifyDefaultFileSubscribers();

        List<FileSubscriber> subList = fileSubscribers.get(reloadableFile);
        if (subList == null) {
            return;
        }

        for (FileSubscriber sub : subList) {
            CompletableFuture.runAsync(sub::update, main.getThreadPoolManager().getAddOnsThreadPool());
        }
    }

    /**
     * Notifies all file subscribers.
     */
    public synchronized void notifyAllFileSubcribers() {
        for (List<FileSubscriber> subList : fileSubscribers.values()) {
            for (FileSubscriber sub : subList) {
                CompletableFuture.runAsync(sub::update, main.getThreadPoolManager().getAddOnsThreadPool());
            }
        }

        notifyDefaultFileSubscribers();
    }

    /**
     * Notifies all default file subscribers, that is those that will all be notified no matter what.
     */
    public synchronized void notifyDefaultFileSubscribers() {
        for (FileSubscriber sub : defaultFileSubscribers) {
            CompletableFuture.runAsync(sub::update, main.getThreadPoolManager().getAddOnsThreadPool());
        }
    }

    /**
     * Get all subscribers
     *
     * @return hashmap with all subscribers
     */
    public HashMap<ReloadableFile, List<FileSubscriber>> getFileSubscribers() {
        return fileSubscribers;
    }

    /**
     * Get all subscribers for a {@code reloadableFile}
     *
     * @param reloadableFile the {@code reloadableFile} for which to get all subscribers
     * @return all subscribers for a {@code reloadableFile}
     */
    public List<FileSubscriber> getFileSubscribersForReloadableFile(ReloadableFile reloadableFile) {
        return fileSubscribers.get(reloadableFile);
    }
}
