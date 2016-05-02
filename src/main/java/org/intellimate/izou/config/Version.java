package org.intellimate.izou.config;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Version implements Comparable<Version> {
    private final int major;
    private final int minor;
    private final int patch;

    public Version(String version) throws IllegalArgumentException {
        String[] split = version.split(".");
        if (split.length > 3) {
            throw new IllegalArgumentException("illegal version: "+version);
        }
        major = Integer.parseInt(split[0]);
        verifyVersion(major, version);
        if (split.length > 1) {
            minor = Integer.parseInt(split[1]);
            verifyVersion(minor, version);
        } else {
            minor = 0;
        }
        if (split.length == 2) {
            patch = Integer.parseInt(split[2]);
            verifyVersion(patch, version);
        } else {
            patch = 0;
        }
    }

    private void verifyVersion(int part, String wholeVersion) {
        if (part > 999) {
            throw new IllegalArgumentException("illegal version: "+wholeVersion);
        }
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

    public String toString() {
        return major+"."+minor+"."+patch;
    }

    @Override
    public int compareTo(Version other) {
        int majorDiff = major - other.getMajor();
        if (majorDiff != 0) {
            return majorDiff;
        }
        int minorDiff = minor - other.getMinor();
        if (majorDiff != 0) {
            return majorDiff;
        }
        int patchDiff = patch - other.getPatch();
        return patchDiff;
    }
}
