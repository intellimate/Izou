package org.intellimate.izou.config;

import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author LeanderK
 * @version 1.0
 */
public class AddOn {

    public AddOn(String name, String version, int id) {
        this.name = name;
        this.version = version;
        this.id = id;
    }

    public AddOn() {

    }

    public AddOn(File file) {
        Pattern pattern = Pattern.compile("(?<name>\\w+)-(?<version>[\\.\\d]+)-.+");
        Matcher matcher = pattern.matcher(file.getName());
        if (!matcher.matches()) {
            throw new IllegalArgumentException("name does not match regex: " + file.getName());
        }
        String name = matcher.group("name");
        String version = matcher.group("version");
        this.name = name;
        this.version = version;
        this.id = -1;
    }

    /**
     * the name of the app, used to identify the zip file
     */
    public String name;
    /**
     * the installed version
     */
    public String version;
    /**
     * the id, -1 if not set
     */
    public int id = -1;

    public Optional<Integer> getId() {
        if (id == -1) {
            return Optional.empty();
        } else {
            return Optional.of(id);
        }
    }

    public Version getVersion() {
        try {
            return new Version(this.version);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
