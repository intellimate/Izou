package org.intellimate.izou.system.context;

import org.intellimate.izou.addon.AddOnInformation;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.util.IdentifiableSet;

import java.util.Optional;

/**
 * <p>
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
public interface AddOns {
    /**
     * Gets addOn
     *
     * @return the addOn
     */
    AddOnModel getAddOn();

    /**
     * Gets the {@link AddOnInformation} that has the given id. The id can either be the fully classified id of an addOn,
     * that is intellimate.izou.addOn.myaddon, or it can just be the artifact id, that is myaddon.
     *
     * @param id The id can either be the fully classified id of an addOn, that is
     * intellimate.izou.addOn.myaddon, or it can just be the artifact id, that is myaddon.
     * @return An optional wrapping the addOnInformation. If the addOn was found the optional will not be empty and
     * otherwise it will.
     */
    Optional<AddOnInformation> getAddOnInformation(final String id);

    /**
     * Gets the {@link AddOnInformation} that has the given id. The id is the serverID of the addOn.
     *
     * @param serverID The id is the serverID of the addOn.
     * @return An optional wrapping the addOn. If the addOn was found the optional will not be empty and otherwise it
     * will.
     */
    Optional<AddOnInformation> getAddOnInformation(final int serverID);

    /**
     * Gets all registered addOnInformations.
     *
     * @return All registered addOnInformations.
     */
    IdentifiableSet<AddOnInformation> getAllAddOnInformations();
}
