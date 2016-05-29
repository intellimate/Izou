package org.intellimate.izou.identification;

import org.apache.commons.cli.MissingArgumentException;
import org.intellimate.izou.config.Version;

import java.util.Optional;

/**
 * <p>
 *     The implementation of an {@link AddOnInformation}.
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
class AddOnInformationImpl implements AddOnInformation {
    private final String name;
    private final Version version;
    private final Version sdkVersion;
    private final String provider;
    private final String id;
    private final int serverID;
    private final String artifactID;

    /**
     * Creates a new instance of AddOnInformation, which holds all the public information of an addOn and is registered
     * in the {@link AddOnInformationManager} with the serverID.
     *
     * @param name The name of the AddOn.
     * @param version The version of the AddOn.
     * @param provider The author of the AddOn.
     * @param id The unique ID of the AddOn.
     * @param sdkVersion The version of the SDK that this addOn uses.
     * @param serverID The serverID of the addOn, if it has one (used to match the addOn with the server).
     * @param artifactID The artifactID of the addOn. This is the maven artifactID.
     *
     * @throws MissingArgumentException Thrown if the config file of an addon is not complete, in other words if
     * an argument is missing in the file
     */
    public AddOnInformationImpl(String name, String provider, String version, String id, String sdkVersion,
                                Optional<Integer> serverID, String artifactID) throws MissingArgumentException {

        if (!checkField(name) || !checkField(provider) || !checkField(version) || !checkField(id)
                || !checkField(sdkVersion) || !checkField(artifactID)) {
            throw new MissingArgumentException("AddOnInformation is not complete - an argument is missing");
        }

        this.name = name;
        this.version = new Version(version);
        this.provider = provider;
        this.id = id;
        this.sdkVersion = new Version(sdkVersion);
        this.serverID = serverID.orElse(-1);
        this.artifactID = artifactID;
    }

    /**
     * Gets the name of the addOn.
     *
     * @return The name of the addOn.
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Gets the version of the addOn.
     *
     * @return The version of the addOn.
     */
    @Override
    public Version getVersion() {
        return version;
    }

    /**
     * Gets the provider, or the author, of the addOn.
     *
     * @return the author of the addOn.
     */
    @Override
    public String getProvider() {
        return provider;
    }

    /**
     * Gets the fully classified ID. An example would be intellimate.izou.addon.myaddon.
     *
     * @return The fully classified ID. An example would be intellimate.izou.addon.myaddon.
     */
    @Override
    public String getID() {
        return id;
    }

    /**
     * Gets the SDK version that this addOn uses.
     *
     * @return The SDK version that this addOn uses.
     */
    @Override
    public Version getSdkVersion() {
        return sdkVersion;
    }

    /**
     * Gets the serverID of the addOn, if it has one (used to match the addOn with the server).
     *
     * @return The serverID of the addOn, if it has one (used to match the addOn with the server).
     */
    @Override
    public Optional<Integer> getServerID() {
        if (serverID == -1) {
            return Optional.empty();
        } else {
            return Optional.of(serverID);
        }
    }

    /**
     * Gets the artifactID of the addOn. This is the maven artifactID.
     *
     * @return The artifactID of the addOn. This is the maven artifactID.
     */
    @Override
    public String getArtifactID() {
        return artifactID;
    }

    /**
     * Checks whether the given string is filled out or not.
     *
     * @param field The string that should not be null.
     * @return True if the string is not null or empty or has a value of 'null' and false otherwise.
     */
    private boolean checkField(String field) {
        return field != null && !field.equals("");
    }
}
