package org.intellimate.izou.server;

import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.ClientHandlerException;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.identification.AddOnInformationManager;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.App;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * this class is used to synchronize with the server
 * @author LeanderK
 * @version 1.0
 */
//TODO what about entries where there is no id given and that are not installed?
//TODO handle mutiple addons with different id's
@SuppressWarnings("Duplicates")
class Synchronization extends IzouModule {
    private final AddOnInformationManager infoManager;
    private final ServerRequests serverRequests;
    private final String deleteAppRegex = "\\w+-[\\.\\d]+-delete\\.zip";
    private final String deleteAndInstallRegex = "\\w+-[\\.\\d]+(-.+)?\\.zip";
    private final String appRegex = "\\w+-[\\.\\d]+\\.zip";
    private final Lock lock = new ReentrantLock();
    private boolean isLocked = false;
    private final Version currentVersion;

    public Synchronization(Main main, List<AddOn> selected, boolean disabledLib, ServerRequests serverRequests,
                           Version currentVersion) {
        super(main);
        infoManager = main.getAddOnInformationManager();
        this.serverRequests = serverRequests;
        this.currentVersion = currentVersion;
        List<AddOn> allInstalled;
        if (disabledLib) {
            allInstalled = new ArrayList<>();
        } else {
            allInstalled = loadInstalledApps(selected);
        }
        Set<String> installedNames = allInstalled.stream().map(addOn -> addOn.name).collect(Collectors.toSet());
        List<AddOn> installed = new ArrayList<>();
        List<AddOn> toInstall = new ArrayList<>();
        List<AddOn> toDelete = new ArrayList<>();
        for (AddOn addOn : selected) {
            if (addOn.name.isEmpty()) {
                continue;
            }
            if (installedNames.contains(addOn.name)) {
                installed.add(addOn);
            } else {
                toInstall.add(addOn);
            }
        }
        List<AddOn> toInstallScheduled = loadScheduledApps(selected).get(false);
        if (toInstallScheduled == null) {
            toInstallScheduled = new ArrayList<>();
        }
        Set<String> scheduled = toInstallScheduled.stream()
                .map(addOn -> addOn.name)
                .collect(Collectors.toSet());
        toInstall.removeIf(addOn -> scheduled.contains(addOn.name));
        infoManager.initAddOns(installed, allInstalled, toInstall, toInstallScheduled, toDelete, selected);
    }

    /**
     * loads all the installed Apps
     * @param selected the selected Apps
     * @return the List of installed AddOns
     */
    private List<AddOn> loadInstalledApps(List<AddOn> selected) {
        Map<String, AddOn> installed;
        Path libPath = main.getFileSystemManager().getLibLocation().toPath();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(libPath)) {
            installed = StreamSupport.stream(dirStream.spliterator(), false)
                    .filter(path -> path.toFile().isFile())
                    .filter(path -> path.toFile().getName().matches(appRegex))
                    .map(this::pathToApp)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(app -> app.name, Function.identity()));
        } catch (IOException e) {
            error("unable to query existing apps", e);
            System.exit(-1);
            throw new RuntimeException();
        }
        selected.forEach(addON -> {
            if (installed.containsKey(addON.name)) {
                AddOn installedAddon = installed.get(addON.name);
                installedAddon.id = addON.id;
            }
        });
        return new ArrayList<>(installed.values());
    }

    /**
     * loads all the scheduled apps
     * @param selected the list of selected apps
     * @return a List of scheduled Apps
     */
    private Map<Boolean, List<AddOn>> loadScheduledApps(List<AddOn> selected) {
        Predicate<Path> isScheduledForDeletion = path -> {
            String name = path.toFile().getName();
            return name.matches(deleteAppRegex);
        };

        class Tuple {
            private Path path;
            private AddOn addon;
        }

        Map<Boolean, List<AddOn>> scheduled;
        Path newLib = main.getFileSystemManager().getNewLibLocation().toPath();
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(newLib)) {
            scheduled = StreamSupport.stream(dirStream.spliterator(), false)
                    .filter(path -> path.toFile().isFile())
                    .filter(path -> path.toFile().getName().matches(deleteAndInstallRegex))
                    .map(path -> {
                        Tuple tuple = new Tuple();
                        tuple.path = path;
                        tuple.addon = pathToApp(path);
                        return tuple;
                    })
                    .filter(tuple -> tuple.addon != null)
                    .collect(Collectors.groupingBy(
                            (Function<Tuple, Boolean>) tuple -> isScheduledForDeletion.test(tuple.path),
                            Collectors.mapping(tuple -> tuple.addon, Collectors.toList())
                    ));
        } catch (IOException e) {
            error("unable to query existing apps", e);
            System.exit(-1);
            throw new RuntimeException();
        }
        selected.forEach(selectedAddON -> {
            scheduled.entrySet().stream()
                    .flatMap(entry -> entry.getValue().stream())
                    .forEach(addOn -> {
                        if (addOn.name.equals(selectedAddON.name)) {
                            addOn.id = selectedAddON.id;
                        }
                    });
        });
        return scheduled;
    }

    /**
     * maps a Path to an App
     * @param path the Path to map the Addon to
     * @return an AddOn or null
     */
    private AddOn pathToApp(Path path) {
        String fileName = path.toFile().getName();
        String[] split = fileName.split("-");
        String name = split[0];
        Version version;
        try {
            version = new Version(split[1]);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return new AddOn(name, version.toString(), -1);
    }

    /**
     * synchronizes the apps
     * @return if something changed
     */
    boolean synchronizeApps() {
        lock.lock();
        isLocked = true;
        try {
            Map<Integer, App> newestAddOnAndDependency = getNewestAppsAndDependency(infoManager.getSelectedAddOns());

            List<AddOn> toDelete = createDeleteFiles(newestAddOnAndDependency);

            List<App> newApps = downloadNewApps(newestAddOnAndDependency);

            List<AddOn> downloadedApps = newApps.stream()
                    .map(app -> getNewestVersion(app).map(version -> new AddOn(app.getName(), version.toString(), app.getId())))
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());

            getMain().getAddOnInformationManager().setAddonsToDelete(toDelete);

            getMain().getAddOnInformationManager().setAddOsToInstallDownloaded(downloadedApps);

            return downloadedApps.isEmpty();
        } finally {
            lock.unlock();
            isLocked = false;
        }
    }

    boolean updateIzou() throws IOException, ClientHandlerException {
        lock.lock();
        isLocked = true;
        try {
            String currentServerVersion = serverRequests.getNewestVersion();
            File izouFile = main.getFileSystemManager().getIzouJarLocation();
            if (new Version(currentServerVersion).compareTo(currentVersion) != 0) {
                InputStream input = serverRequests.download("url");
                File tempFile = new File(izouFile.getAbsolutePath() + ".new");
                if (!tempFile.exists()) {
                    tempFile.createNewFile();
                }
                FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
                ByteStreams.copy(input, fileOutputStream);
                input.close();
                fileOutputStream.close();
                return true;
            }
            return false;
        } finally {
            lock.unlock();
            isLocked = false;
        }
    }

    private List<App> downloadNewApps(Map<Integer, App> newestAddOnAndDependency) {
        List<App> newApps = newestAddOnAndDependency.values().stream()
                .filter(app -> infoManager.getInstalledWithDependencies().stream()
                        .filter(addOn -> addOn.name.equals(app.getName()))
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

        ArrayList<AddOn> installed = new ArrayList<>(infoManager.getAddOnsToInstallDownloaded());

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
                                .filter(file -> file.getName().matches(appRegex))
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

    private List<AddOn> createDeleteFiles(Map<Integer, App> newestAddOnAndDependency) {
        Set<String> selectedNames = infoManager.getInstalledWithDependencies().stream()
                .map(addOn -> addOn.name)
                .collect(Collectors.toSet());

        List<AddOn> finalToDeleteList = infoManager.getInstalledWithDependencies().stream()
                .filter(addOn -> !selectedNames.contains(addOn.name))
                .filter(addOn -> addOn.getId()
                        .map(id -> !newestAddOnAndDependency.containsKey(id))
                        .orElse(true)
                ).collect(Collectors.toList());

        finalToDeleteList.forEach(addOn -> {
                    File file = new File(getMain().getFileSystemManager().getNewLibLocation(), addOn.name + "-" + addOn.version + "-delete.zip");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            error("unable to create file "+file, e);
                        }
                    }
                });

        infoManager.getAddOnsToDelete().stream()
                .filter(addOn -> addOn.getId().isPresent())
                .filter(addOn -> newestAddOnAndDependency.containsKey(addOn.getId().get()))
                .forEach(addOn -> {
                    try {
                        Files.list(getMain().getFileSystemManager().getNewLibLocation().toPath())
                                .map(Path::toFile)
                                .filter(File::isFile)
                                .filter(file -> file.getName().startsWith(addOn.name))
                                .filter(file -> file.getName().matches(deleteAppRegex))
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

    private Map<Integer, App> getNewestAppsAndDependency(List<AddOn> selected) {
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
            try {
                serverRequests.getAddonAndDependencies(remove.getId())
                        .ifPresent(app -> {
                            resolved.put(app.getId(), app);
                            app.getVersionsList().stream()
                                    .flatMap(appVersion ->  appVersion.getDependenciesList().stream())
                                    .filter(appD -> !resolved.containsKey(appD.getId()))
                                    .forEach(appD -> toResolve.put(appD.getId(), appD));
                        });
            } catch (ClientHandlerException e) {
                debug("unable to connect to server", e);
                e.printStackTrace();
            }
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
                        .filter(file -> file.getName().matches(deleteAndInstallRegex))
                        .forEach(File::delete);
            } catch (IOException e) {
                error("unable to list files in the NewLib-Folder");
            }
            getMain().getAddOnInformationManager().setAddOsToInstallDownloaded(new ArrayList<>());
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
