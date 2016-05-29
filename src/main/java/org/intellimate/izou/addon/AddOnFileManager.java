package org.intellimate.izou.addon;

import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.identification.AddOnInformation;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * provides Information about the installed, scheduled etc. AddOns
 * @author LeanderK
 * @version 1.0
 */
class AddOnFileManager extends IzouModule {
    final static String REGEX_DELETE_APP = "\\w+-[\\.\\d]+-delete\\.zip";
    final static String REGEX_DELETE_AND_INSTALL_APP = "\\w+-[\\.\\d]+(-.+)?\\.zip";
    final static String REGEX_INSTALL_APP = "\\w+-[\\.\\d]+\\.zip";

    AddOnFileManager(Main main) {
        super(main);
    }

    /**
     * returns the List of the installed zip-files
     * @return a list of the ZipFiles
     */
    List<String> getInstalledAppsRawNames() throws IOException {
        try (Stream<Path> stream = Files.list(getMain().getFileSystemManager().getLibLocation().toPath())){
            return stream.map(Path::toFile)
                    .map(File::getName)
                    .filter(name -> name.matches(REGEX_INSTALL_APP))
                    .collect(Collectors.toList());
        }
    }

    /**
     * returns all the installed Addons (inclusive the dependencies)
     * @return a list of installed Addons
     * @throws IOException if an Exception occurred while trying to access the File-System
     */
    List<AddOnInformation> getInstalledWithDependencies() throws IOException {
        return getInstalledAppsRawNames().stream()
                .map(fileName -> AddOnFileManager.fileNameToAppExisting(fileName, getMain()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * returns the apps scheduled to install
     * @return a list of addons
     * @throws IOException if an Exception occurred while trying to access the File-System
     */
    List<AddOn> getScheduledToInstall() throws IOException {
        try (Stream<Path> stream = Files.list(getMain().getFileSystemManager().getNewLibLocation().toPath())) {
            return stream.map(Path::toFile)
                    .map(File::getName)
                    .filter(fileName -> fileName.matches(REGEX_INSTALL_APP))
                    .map(AddOnFileManager::pathToApp)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }

    /**
     * returns teh Downloaded apps
     * @return a list of addons
     * @throws IOException if an Exception occurred while trying to access the File-System
     */
    List<AddOn> getScheduledToDelete() throws IOException {
        try (Stream<Path> stream = Files.list(getMain().getFileSystemManager().getNewLibLocation().toPath())) {
            return stream.map(Path::toFile)
                    .map(File::getName)
                    .filter(fileName -> fileName.matches(REGEX_DELETE_APP))
                    .map(AddOnFileManager::pathToApp)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .collect(Collectors.toList());
        }
    }

    /**
     * maps a Path to an {@link AddOnInformation} (if it is already existing)
     * @param fileName the name of the File
     * @return an AddOn or null
     */
    static Optional<AddOnInformation> fileNameToAppExisting(String fileName, Main main) {
        String[] split = fileName.split("-");
        String name = split[0];
        if (name.isEmpty()) {
            return Optional.empty();
        } else {
            return main.getAddOnInformationManager().getAddOnInformation(name);
        }
    }

    /**
     * maps a Path to an App
     * @param fileName the name of the File
     * @return an AddOn or null
     */
    static Optional<AddOn> pathToApp(String fileName) {
        String[] split = fileName.split("-");
        String name = split[0];
        Version version;
        try {
            version = new Version(split[1]);
        } catch (IllegalArgumentException e) {
            return null;
        }
        return Optional.of(new AddOn(name, version.toString(), -1));
    }
}
