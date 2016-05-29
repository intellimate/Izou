package org.intellimate.izou.addon;

import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.ClientHandlerException;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.identification.AddOnInformation;
import org.intellimate.izou.identification.AddOnInformationManager;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.server.CommunicationManager;
import org.intellimate.izou.server.ServerRequests;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.nio.file.StandardCopyOption.COPY_ATTRIBUTES;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * this class is used to synchronize with the server and manage the while delete/copy process
 * @author LeanderK
 * @version 1.0
 */
//TODO what about entries where there is no id given and that are not installed?
//TODO handle mutiple addons with different id's
@SuppressWarnings("Duplicates")
class SynchronizationManager extends IzouModule {
    private final Lock lock = new ReentrantLock();
    private boolean isLocked = false;

    public SynchronizationManager(Main main) {
        super(main);
    }

    /**
     * this methods replaces all the apps in the "/lib" directory with new ones from the "/new" directory.
     * it also deletes all the marked plugins
     */
    void copyOrDeleteDownloadedApps() throws IOException {
        File newLibLocation = getMain().getFileSystemManager().getNewLibLocation();
        if (!newLibLocation.exists()) {
            return;
        }

        Map<Boolean, List<File>> newOrReplace;
        try (Stream<Path> stream = Files.list(newLibLocation.toPath())) {
            newOrReplace = stream.map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".zip"))
                    .collect(Collectors.partitioningBy(file -> file.getName().contains("delete")));
        }


        Set<String> toDelete = newOrReplace.get(true).stream()
                .map(AddOn::new)
                .map(addOn -> addOn.name)
                .collect(Collectors.toSet());

        Map<String, AddOn> newAddons = newOrReplace.get(false).stream()
                .map(AddOn::new)
                .collect(Collectors.toMap(addOn -> addOn.name, Function.identity(), (addOn, addOn2) -> {
                    if (addOn.getVersion().compareTo(addOn2.getVersion()) >= 0) {
                        return addOn;
                    } else {
                        return addOn2;
                    }
                }));

        try (Stream<Path> stream = Files.list(getMain().getFileSystemManager().getLibLocation().toPath())) {
                    stream.map(Path::toFile)
                    .filter(file -> file.getName().endsWith(".zip") || file.isDirectory())
                    .filter(file -> {
                        AddOn addOn = new AddOn(file);
                        return toDelete.contains(addOn.name) || newAddons.containsKey(addOn.name);
                    })
                    .forEach(file -> {
                        if (file.isDirectory()) {
                            try {
                                Files.walkFileTree(file.toPath(), new SimpleFileVisitor<Path>() {
                                    @Override
                                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                                        Files.delete(file);
                                        return FileVisitResult.CONTINUE;
                                    }

                                    @Override
                                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                                        Files.delete(dir);
                                        return FileVisitResult.CONTINUE;
                                    }

                                });
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            file.delete();
                        }
                    });
        }

        newOrReplace.get(false).forEach(file -> {
            try {
                Files.copy(file.toPath(), new File(getMain().getFileSystemManager().getLibLocation(), file.getName()).toPath(), COPY_ATTRIBUTES, REPLACE_EXISTING);
                file.delete();
            } catch (IOException e) {
                error("Unable to copy file "+file.toString());
            }
        });

        newOrReplace.get(true).forEach(File::delete);
    }

    /**
     * synchronizes the apps
     * @return if something changed or false if not (may be unable to communicate or an error)
     */
    boolean synchronizeApps() throws IOException {
        lock.lock();
        isLocked = true;
        try {
            Optional<ServerRequests> requestsOptional = getMain().getCommunicationManager()
                    .map(CommunicationManager::getServerRequests);
            if (requestsOptional.isPresent()) {
                Map<Integer, App> newestAddOnAndDependency = getNewestAppsAndDependency(getMain().getAddOnInformationManager().getSelectedAddOns(), requestsOptional.get());

                List<AddOnInformation> toDelete = createDeleteFiles(newestAddOnAndDependency);

                List<App> newApps = downloadNewApps(newestAddOnAndDependency, requestsOptional.get());

                List<AddOn> downloadedApps = newApps.stream()
                        .map(app -> getNewestVersion(app).map(version -> new AddOn(app.getName(), version.toString(), app.getId())))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());

                return downloadedApps.isEmpty();
            } else {
                return false;
            }
        } finally {
            lock.unlock();
            isLocked = false;
        }
    }

    private List<App> downloadNewApps(Map<Integer, App> newestAddOnAndDependency, ServerRequests serverRequests) throws IOException {
        List<AddOnInformation> allInstalled = getMain().getAddOnManager().getInstalledWithDependencies();
        List<App> newApps = newestAddOnAndDependency.values().stream()
                .filter(app -> allInstalled.stream()
                        .filter(addOn -> addOn.getName().equals(app.getName()))
                        .findAny()
                        .filter(addOn -> {
                            return getNewestVersion(app)
                                    .map(appVersion -> appVersion.compareTo(addOn.getVersion()) < 0)
                                    .orElse(true);
                        })
                        .map(ignore -> false)
                        .orElse(true)
                )
                .collect(Collectors.toList());

        Map<String, App> nameToNewApp = newApps.stream().collect(Collectors.toMap(App::getName, Function.identity()));

        ArrayList<AddOn> installed = new ArrayList<>(getMain().getAddOnManager().getScheduledToInstall());

        installed.stream()
                .filter(addOn -> {
                    if (!nameToNewApp.containsKey(addOn.name)) {
                        return true;
                    } else {
                        return getNewestVersion(nameToNewApp.get(addOn.name))
                                .map(version -> version.compareTo(addOn.getVersion()) > 0)
                                .orElse(false);
                    }
                } )
                .forEach(addOn -> {
                    try {
                        Files.list(getMain().getFileSystemManager().getNewLibLocation().toPath())
                                .map(Path::toFile)
                                .filter(file -> file.getName().startsWith(addOn.name))
                                .filter(file -> file.getName().matches(AddOnFileManager.REGEX_INSTALL_APP))
                                .forEach(file -> {
                                    boolean deleted = file.delete();
                                    if (!deleted) {
                                        error("unable to delete file: "+file);
                                    }
                                });
                    } catch (IOException e) {
                        error("unable to walk newLib-Directory", e);
                    }
                });

        newApps.forEach(app -> {
            String url = app.getVersions(0).getDownloadLink();
            try {
                InputStream inputStream = serverRequests.download(url);
                File file = new File(getMain().getFileSystemManager().getNewLibLocation(), app.getName() + "-" + app.getVersions(0).getVersion() + ".zip");
                if (!file.exists()) {
                    file.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(file);
                ByteStreams.copy(inputStream, fileOutputStream);
                inputStream.close();
                fileOutputStream.close();
            } catch (ClientHandlerException e) {
                error("unable to download from "+url, e);
            } catch (IOException e) {
                error("unable to create file ", e);
            }
        });

        return newApps;
    }

    private Optional<Version> getNewestVersion(App app) {
        class Tupel {
            App.AppVersion data;
            Version version;
        }
        return app.getVersionsList().stream()
                .map(version -> {
                    Tupel tupel = new Tupel();
                    tupel.data = version;
                    tupel.version = new Version(version.getVersion());
                    return tupel;
                })
                .reduce((tupel, tupel2) -> {
                    if (tupel.version.compareTo(tupel2.version) > 0) {
                        return tupel;
                    } else {
                        return tupel2;
                    }
                })
                .map(tupel -> tupel.version);
    }

    private List<AddOnInformation> createDeleteFiles(Map<Integer, App> newestAddOnAndDependency) throws IOException {
        List<AddOnInformation> allInstalled = getMain().getAddOnManager().getInstalledWithDependencies();
        Set<String> selectedNames = allInstalled.stream()
                .map(AddOnInformation::getName)
                .collect(Collectors.toSet());

        List<AddOnInformation> finalToDeleteList = allInstalled.stream()
                .filter(addOn -> !selectedNames.contains(addOn.getName()))
                .filter(addOn -> addOn.getServerID()
                        .map(id -> !newestAddOnAndDependency.containsKey(id))
                        .orElse(true)
                ).collect(Collectors.toList());

        finalToDeleteList.forEach(addOn -> {
                    File file = new File(getMain().getFileSystemManager().getNewLibLocation(), addOn.getName() + "-" + addOn.getVersion() + "-delete.zip");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            error("unable to create file "+file, e);
                        }
                    }
                });

        getMain().getAddOnManager().getScheduledToDelete().stream()
                .filter(addOn -> addOn.getId().isPresent())
                .filter(addOn -> newestAddOnAndDependency.containsKey(addOn.getId().get()))
                .forEach(addOn -> {
                    try {
                        Files.list(getMain().getFileSystemManager().getNewLibLocation().toPath())
                                .map(Path::toFile)
                                .filter(File::isFile)
                                .filter(file -> file.getName().startsWith(addOn.name))
                                .filter(file -> file.getName().matches(AddOnFileManager.REGEX_DELETE_APP))
                                .forEach(file -> {
                                    boolean deleted = file.delete();
                                    if (!deleted) {
                                        error("unable to delete file: "+file);
                                    }
                                });
                    } catch (IOException e) {
                        error("unable to walk newLib-Directory", e);
                    }

                });

        return finalToDeleteList;
    }

    private Map<Integer, App> getNewestAppsAndDependency(List<AddOn> selected, ServerRequests serverRequests) throws ClientHandlerException {
        List<App> initialSelected = selected.stream()
                .filter(app -> app.getId().isPresent())
                .map(app -> {
                    try {
                        return serverRequests.getAddonAndDependencies(app.getId().get());
                    } catch (ClientHandlerException e) {
                        debug("unable to connect to server", e);
                        return Optional.<App>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Map<Integer, App> resolved = initialSelected.stream()
                .collect(Collectors.toMap(App::getId, Function.identity(), (app, app2) -> app));

        Map<Integer, App> toResolve = initialSelected.stream()
                .flatMap(app -> app.getVersionsList().stream())
                .flatMap(appVersion -> appVersion.getDependenciesList().stream())
                .filter(app -> !resolved.containsKey(app.getId()))
                .collect(Collectors.toMap(App::getId, Function.identity(), (app, app2) -> app));

        while (!toResolve.isEmpty()) {
            Integer next = toResolve.keySet().iterator().next();
            App remove = toResolve.remove(next);
            serverRequests.getAddonAndDependencies(remove.getId())
                .ifPresent(app -> {
                    resolved.put(app.getId(), app);
                    app.getVersionsList().stream()
                            .flatMap(appVersion ->  appVersion.getDependenciesList().stream())
                            .filter(appD -> !resolved.containsKey(appD.getId()))
                            .forEach(appD -> toResolve.put(appD.getId(), appD));
                });
        }
        return resolved;
    }

    /**
     * deletes all files in the newLib-folder and resets the AddonInformationManager
     */
    void resetDownloaded() {
        lock.lock();
        isLocked = true;
        try {
            try {
                Path newLib = getMain().getFileSystemManager().getNewLibLocation().toPath();
                Files.walk(newLib, FileVisitOption.FOLLOW_LINKS)
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .filter(file -> file.getName().matches(AddOnFileManager.REGEX_DELETE_AND_INSTALL_APP))
                        .forEach(File::delete);
            } catch (IOException e) {
                error("unable to list files in the NewLib-Folder");
            }
        } finally {
            lock.unlock();
            isLocked = false;
        }
    }

    /**
     * returns whether the class is busy synchronizing
     * @return true if synchronizing
     */
    boolean isBusy() {
        return isLocked;
    }


}
