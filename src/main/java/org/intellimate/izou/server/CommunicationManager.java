package org.intellimate.izou.server;

import com.google.common.io.ByteStreams;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.config.Version;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * @author LeanderK
 * @version 1.0
 */
public class CommunicationManager extends IzouModule {
    private final ServerRequests serverRequests;
    private final Version currentVersion;
    private final File izouFile;
    private final File libLocation;
    private final List<AddOn> installedWithDependencies;
    private final List<AddOn> selectedWithoutDependencies;
    private final boolean disabledLib;

    public CommunicationManager(Version currentVersion, Main main, List<AddOn> selectedWithoutDependencies, boolean disabledLib, String refreshToken) throws IllegalStateException {
        super(main);
        this.serverRequests = new ServerRequests("http://www.izou.org", refreshToken, main);
        try {
            serverRequests.init();
        } catch (UnirestException e) {
            error("unable to init Connection to server", e);
            throw new IllegalStateException("not able to init server-request package");
        }
        this.currentVersion = currentVersion;
        this.izouFile = main.getFileSystemManager().getIzouJarLocation();
        libLocation = main.getFileSystemManager().getLibLocation();
        this.selectedWithoutDependencies = selectedWithoutDependencies;
        this.disabledLib = disabledLib;
        if (disabledLib) {
            installedWithDependencies = new ArrayList<>();
        } else {
            installedWithDependencies = loadInstalledApps();
        }
    }

    private List<AddOn> loadInstalledApps() {
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

        selectedWithoutDependencies.forEach(addOn -> {
            if (installed.containsKey(addOn.name)) {
                AddOn installedAddon = installed.get(addOn.name);
                installedAddon.id = addOn.id;
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
            return synchronizeApps(this.selectedWithoutDependencies);
        }
        return false;
    }

    private boolean synchronizeApps(List<AddOn> selected) {
        selected.stream()
                .filter(app -> app.getId().isPresent())
                .map(app -> {
                    try {
                        return serverRequests.getAddonAndDependencies(app.getId().get());
                    } catch (UnirestException e) {
                        debug("unable to connect to server", e);
                        return new ArrayList<>();
                    }
                })
                .
        return true;
    }

    private boolean updateIzou() throws IOException, UnirestException {
        String currentServerVersion = serverRequests.getNewestVersion();
        if (new Version(currentServerVersion).compareTo(currentVersion) != 0) {
            InputStream input = serverRequests.downloadIzouVersion("url");
            File tempFile = new File(izouFile.getAbsolutePath() + ".new");
            if (!tempFile.exists()) {
                tempFile.createNewFile();
            }
            FileOutputStream fileOutputStream = new FileOutputStream(tempFile);
            ByteStreams.copy(input, fileOutputStream);
            return true;
        }
        return false;
    }
}
