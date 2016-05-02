package org.intellimate.izou.addon;

import org.apache.commons.cli.MissingArgumentException;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IdentifiableSet;
import org.intellimate.izou.util.IzouModule;
import ro.fortsoft.pf4j.PluginDescriptor;

import java.util.Optional;

/**
 *
 */
public class AddOnInformationManager extends IzouModule {
    private final IdentifiableSet<AddOnInformation> addOnInformations;
    private final IdentifiableSet<AddOnModel> addOns;

    /**
     * Creates a new instance of AddOnInformationManager.
     */
    public AddOnInformationManager(Main main) {
        super(main);
        addOnInformations = new IdentifiableSet<>();
        addOns = new IdentifiableSet<>();
    }

    /**
     * Registers an addOn with the AddOnInformationManager by extracting all relevant information from the addOn and
     * adding it the the sets.
     *
     * Once an addOn is registered, its public data can be viewed by any other addOn through the context.
     *
     * @param addOn The addOn to register with the AddOnInformationManager.
     */
    void registerAddOn(AddOnModel addOn) {
        PluginDescriptor descriptor = addOn.getPlugin().getDescriptor();

        String name = descriptor.getTitle();
        String version = descriptor.getVersion().toString();
        String provider = descriptor.getProvider();
        String id = descriptor.getPluginId();
        String sdkVersion = descriptor.getSdkVersion().toString();
        String artifactID = descriptor.getArtifactID();
        Optional<Integer> serverID = descriptor.getServerID();

        try {
            AddOnInformation addOnInformation = new AddOnInformationImpl(name, version, provider, id, sdkVersion,
                    serverID, artifactID);
            addOnInformations.add(addOnInformation);
            addOns.add(addOn);
        } catch (MissingArgumentException e) {
            error("Unable to register addOn: " + addOn.getID() + " with the AddOnInformationManager", e);
        }
    }

    /**
     * Removes a addOn from the sets. The id can either be the fully classified id of an addOn, that is
     * intellimate.izou.addOn.myaddon, or it can just be the artifact id, that is myaddon. On success, true is returned
     * and otherwise false is returned.
     *
     * @param id The ID of the addOn to remove. This can either be intellimate.izou.addOn.myaddon as an
     *           example, or just myaddon.
     * @return True on success, else false.
     */
    @SuppressWarnings("Duplicates")
    boolean unregisterAddOn(String id) {
        boolean success1 = false;
        Optional<AddOnModel> addOnModel = getAddOn(id);
        if (addOnModel.isPresent()) {
            addOns.remove(addOnModel.get());
            success1 = true;
        }

        boolean success2 = false;
        Optional<AddOnInformation> addOnInformation = getAddOnInformation(id);
        if (addOnInformation.isPresent()) {
            addOnInformations.remove(addOnInformation.get());
            success2 = true;
        }

        return success1 && success2;
    }

    /**
     * Removes a addOn from the sets. The id is the serverID of an addOn. On success, true is returned
     * and otherwise false is returned.
     *
     * @param serverID The serverID of the addOn to remove.
     * @return True on success, else false.
     */
    @SuppressWarnings("Duplicates")
    boolean unregisterAddOn(int serverID) {
        boolean success1 = false;
        Optional<AddOnModel> addOnModel = getAddOn(serverID);
        if (addOnModel.isPresent()) {
            addOns.remove(addOnModel.get());
            success1 = true;
        }

        boolean success2 = false;
        Optional<AddOnInformation> addOnInformation = getAddOnInformation(serverID);
        if (addOnInformation.isPresent()) {
            addOnInformations.remove(addOnInformation.get());
            success2 = true;
        }

        return success1 && success2;
    }

    /**
     * Gets the {@link AddOnModel} that has the given id. The id can either be the fully classified id of an addOn,
     * that is intellimate.izou.addOn.myaddon, or it can just be the artifact id, that is myaddon.
     *
     * @param id The id can either be the fully classified id of an addOn, that is
     * intellimate.izou.addOn.myaddon, or it can just be the artifact id, that is myaddon.
     * @return An optional wrapping the addOn. If the addOn was found the optional will not be empty and otherwise it
     * will.
     */
    public Optional<AddOnModel> getAddOn(final String id) {
        return addOns.stream()
                .filter(addOn -> addOn.getID().equals(id) || addOn.getPlugin().getDescriptor().getArtifactID().equals(id))
                .findFirst();
    }

    /**
     * Gets the {@link AddOnModel} that has the given id. The id is the serverID of the addOn.
     *
     * @param serverID The id is the serverID of the addOn.
     * @return An optional wrapping the addOn. If the addOn was found the optional will not be empty and otherwise it
     * will.
     */
    public Optional<AddOnModel> getAddOn(final int serverID) {
        return addOns.stream()
                .filter(addOn -> {
                    Optional<Integer> serverIDOpt = addOn.getPlugin().getDescriptor().getServerID();
                    return serverIDOpt.isPresent() && serverIDOpt.get() == serverID;
                })
                .findFirst();
    }

    /**
     * Gets the {@link AddOnInformation} that has the given id. The id can either be the fully classified id of an addOn,
     * that is intellimate.izou.addOn.myaddon, or it can just be the artifact id, that is myaddon.
     *
     * @param id The id can either be the fully classified id of an addOn, that is
     * intellimate.izou.addOn.myaddon, or it can just be the artifact id, that is myaddon.
     * @return An optional wrapping the addOnInformation. If the addOn was found the optional will not be empty and
     * otherwise it will.
     */
    public Optional<AddOnInformation> getAddOnInformation(final String id) {
        return addOnInformations.stream()
                .filter(addOn -> addOn.getID().equals(id) || addOn.getArtifactID().equals(id))
                .findFirst();
    }

    /**
     * Gets the {@link AddOnInformation} that has the given id. The id is the serverID of the addOn.
     *
     * @param serverID The id is the serverID of the addOn.
     * @return An optional wrapping the addOn. If the addOn was found the optional will not be empty and otherwise it
     * will.
     */
    public Optional<AddOnInformation> getAddOnInformation(final int serverID) {
        if (serverID == -1) {
            return Optional.empty();
        }

        return addOnInformations.stream()
                .filter(addOn -> addOn.getServerID().orElse(-1) == serverID)
                .findFirst();
    }
}
