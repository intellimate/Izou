package org.intellimate.izou.server;

import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.exceptions.UnirestException;
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
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author LeanderK
 * @version 1.0
 */
//TODO update to AddonInformationManager
public class CommunicationManager extends IzouModule {
    private final ServerRequests serverRequests;
    private final Version currentVersion;
    private final File izouFile;
    private final File libLocation;
    private final boolean disabledLib;
    private final RequestHandler requestHandler;
    private final Thread connectionThread;
    private boolean run = true;
    private final AddOnInformationManager infoManager = getMain().getAddOnInformationManager();

    public CommunicationManager(Version currentVersion, Main main, boolean disabledLib, String refreshToken, List<AddOn> selected) throws IllegalStateException {
        super(main);
        this.serverRequests = new ServerRequests("http://www.izou.info", refreshToken, main);
        requestHandler = new RequestHandler(main);
        try {
            serverRequests.init();
        } catch (UnirestException e) {
            error("unable to init Connection to server", e);
            throw new IllegalStateException("not able to init server-request package");
        }
        connectionThread = new Thread(() -> {
            while (run) {
                serverRequests.requests(requestHandler::handleRequests);
                try {
                    Thread.sleep(30000);
                } catch (InterruptedException e) {
                    debug("interrupted", e);
                }
            }
        });
        connectionThread.start();
        this.currentVersion = currentVersion;
        this.izouFile = main.getFileSystemManager().getIzouJarLocation();
        libLocation = main.getFileSystemManager().getLibLocation();
        this.disabledLib = disabledLib;
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
            if (installedNames.contains(addOn.name)) {
                installed.add(addOn);
            } else {
                toInstall.add(addOn);
            }
        }
        infoManager.initAddOns(installed, allInstalled, toInstall, toDelete);
    }

    private List<AddOn> loadInstalledApps(List<AddOn> selected) {
        Function<Path, AddOn> pathToApp = path -> {
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
        };

        Map<String, AddOn> installed;
        try (DirectoryStream<Path> dirStream = Files.newDirectoryStream(libLocation.toPath())) {
            installed = StreamSupport.stream(dirStream.spliterator(), false)
                    .filter(path -> path.toFile().isFile())
                    .filter(path -> path.toFile().getName().matches("\\w+-[\\.\\d]+-.+\\.zip"))
                    .map(pathToApp)
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

    public boolean checkForUpdates() throws IOException {
        boolean mustRestart = false;
        try {
            mustRestart = updateIzou();
        } catch (UnirestException e) {
            error("unable to update izou", e);
        }
        if (mustRestart) {
            return true;
        }
        if (!disabledLib) {
            return synchronizeApps(infoManager.getInstalledAddOns());
        }
        return false;
    }

    private boolean synchronizeApps(List<AddOn> selected) {
        List<App> initialSelected = selected.stream()
                .filter(app -> app.getId().isPresent())
                .map(app -> {
                    try {
                        return serverRequests.getAddonAndDependencies(app.getId().get());
                    } catch (UnirestException e) {
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
            } catch (UnirestException e) {
                debug("unable to connect to server", e);
                e.printStackTrace();
            }
        }

        Set<String> selectedNames = infoManager.getInstalledWithDependencies().stream()
                .map(addOn -> addOn.name)
                .collect(Collectors.toSet());

        infoManager.getInstalledWithDependencies().stream()
                .filter(addOn -> !selectedNames.contains(addOn.name))
                .filter(addOn -> addOn.getId()
                        .map(id -> !resolved.containsKey(id))
                        .orElse(true)
                )
                .forEach(addOn -> {
                    File file = new File(getMain().getFileSystemManager().getNewLibLocation(), addOn.name + "-" + addOn.version + "-delete.zip");
                    if (!file.exists()) {
                        try {
                            file.createNewFile();
                        } catch (IOException e) {
                            error("unable to create file "+file, e);
                        }
                    }
                });

        List<App> newApps = resolved.values().stream()
                .filter(app -> infoManager.getInstalledWithDependencies().stream()
                        .filter(addOn -> addOn.name.equals(app.getName()))
                        .findAny()
                        .filter(addOn -> addOn.getVersion().compareTo(new Version(app.getVersions(0).getVersion())) >= 0)
                        .map(ignore -> false)
                        .orElse(true)
                )
                .collect(Collectors.toList());

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
            } catch (UnirestException e) {
                error("unable to download from "+url, e);
            } catch (IOException e) {
                error("unable to create file ", e);
            }
        });

        return newApps.isEmpty();
    }

    private boolean updateIzou() throws IOException, UnirestException {
        String currentServerVersion = serverRequests.getNewestVersion();
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
    }
}
