package org.intellimate.izou.addon;

import org.intellimate.izou.config.Version;
import org.intellimate.izou.identification.Identifiable;

import java.util.Optional;

/**
 * <p>
 *     An AddOnInformation holds all public information of an addOn. That means all information that other addOns are
 *     allowed to see about a given addOn is in here. Each addOn has its own instance of AddOnInformation which is
 *     stored in the {@link AddOnInformationManager}.
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
public interface AddOnInformation extends Identifiable {
    /**
     * Gets the name of the addOn.
     *
     * @return The name of the addOn.
     */
    String getName();

    /**
     * Gets the version of the addOn.
     *
     * @return The version of the addOn.
     */
    Version getVersion();

    /**
     * Gets the provider, or the author, of the addOn.
     *
     * @return the author of the addOn.
     */
    String getProvider();

    /**
     * Gets the SDK version that this addOn uses.
     *
     * @return The SDK version that this addOn uses.
     */
    Version getSdkVersion();

    /**
     * Gets the serverID of the addOn, if it has one (used to match the addOn with the server).
     *
     * @return The serverID of the addOn, if it has one (used to match the addOn with the server).
     */
    Optional<Integer> getServerID();

    /**
     * Gets the artifactID of the addOn. This is the maven artifactID.
     *
     * @return The artifactID of the addOn. This is the maven artifactID.
     */
    String getArtifactID();

    /**
     * Gets the fully classified ID. An example would be intellimate.izou.addon.myaddon.
     *
     * @return The fully classified ID. An example would be intellimate.izou.addon.myaddon.
     */
    @Override
    String getID();
}
