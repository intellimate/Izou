package org.intellimate.izou.identification;

import com.esotericsoftware.yamlbeans.YamlWriter;
import org.apache.commons.cli.MissingArgumentException;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.config.InternalConfig;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IdentifiableSet;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.IzouInstanceStatus;
import ro.fortsoft.pf4j.IzouPluginClassLoader;
import ro.fortsoft.pf4j.PluginDescriptor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static javafx.scene.input.KeyCode.M;
import static org.bouncycastle.asn1.x500.style.RFC4519Style.l;

/**
 * The AddOnInformationManager is a class that gives access to all kinds of information about registered addOns in Izou.
 * For example, any addOn can find out the names of all other registered addOns through this class.
 */
public class AddOnInformationManager extends IzouModule {
    private final IdentifiableSet<AddOnInformation> addOnInformations;
    private final IdentifiableSet<AddOnModel> addOns;
    private InternalConfig internalConfig;
    private List<AddOn> selectedAddOns = new ArrayList<>();
    private final String addonConfigFile;

    /**
     * Creates a new instance of AddOnInformationManager.
     * @param main an instance of main
     * @param addonConfigFile the config file for the addons
     */
    public AddOnInformationManager(Main main, String addonConfigFile) {
        super(main);
        this.addonConfigFile = addonConfigFile;
        addOnInformations = new IdentifiableSet<>();
        addOns = new IdentifiableSet<>();
        IdentificationManagerImpl identificationManager = (IdentificationManagerImpl) IdentificationManagerM.getInstance();
        identificationManager.setAddOnInformationManager(this);
    }

    /**
     * Registers an addOn with the AddOnInformationManager by extracting all relevant information from the addOn and
     * adding it the the sets.
     *
     * Once an addOn is registered, its public data can be viewed by any other addOn through the context.
     *
     * @param addOn The addOn to register with the AddOnInformationManager.
     */
    public void registerAddOn(AddOnModel addOn) {
        PluginDescriptor descriptor = addOn.getPlugin().getDescriptor();

        String version = descriptor.getVersion().toString();
        String provider = descriptor.getProvider();
        String id = descriptor.getPluginId();
        String sdkVersion = descriptor.getSdkVersion().toString();
        String artifactID = descriptor.getArtifactID();
        Optional<Integer> serverID = descriptor.getServerID();

        try {
            AddOnInformation addOnInformation = new AddOnInformationImpl(provider, version, id, sdkVersion,
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
    boolean unregisterAddOn(String id) {
        return unregisterHelper(() -> getAddOn(id), () -> getAddOnInformation(id));
    }

    /**
     * Removes a addOn from the sets. The id is the serverID of an addOn. On success, true is returned
     * and otherwise false is returned.
     *
     * @param serverID The serverID of the addOn to remove.
     * @return True on success, else false.
     */
    boolean unregisterAddOn(int serverID) {
        return unregisterHelper(() -> getAddOn(serverID), () -> getAddOnInformation(serverID));
    }

    /**
     * Helper to unregister an addOn.
     *
     * @param suppAdd The first get function to find the right addon.
     * @param suppAddInf The second get function to find the right addonInformation.
     * @return True if the operation was successful, otherwise false.
     */
    private boolean unregisterHelper(Supplier<Optional<AddOnModel>> suppAdd, Supplier<Optional<AddOnInformation>> suppAddInf) {
        boolean success1 = false;
        Optional<AddOnModel> addOnModel = suppAdd.get();
        if (addOnModel.isPresent()) {
            success1 = addOns.remove(addOnModel.get());
        }

        boolean success2 = false;
        Optional<AddOnInformation> addOnInformation = suppAddInf.get();
        if (addOnInformation.isPresent()) {
            success2 = addOnInformations.remove(addOnInformation.get());
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
     * gets the AddonModel for the Identification, or null if none found
     * @param identification the Identification
     * @return an AddonModel or null
     */
    public AddOnModel getAddonModel(Identification identification) {
        IdentificationImpl impl = (IdentificationImpl) identification;
        return getAddonModel(impl.getIdentifiable());
    }

    /**
     * gets the AddonModel for the Identifiable, or null if none found
     * @param identifiable the Identifiable
     * @return an AddonModel or null
     */
    public AddOnModel getAddonModel(Identifiable identifiable) {
        if (identifiable.getClass().getClassLoader() instanceof IzouPluginClassLoader && !identifiable.getClass().getName().toLowerCase()
                .contains(IzouPluginClassLoader.PLUGIN_PACKAGE_PREFIX_IZOU_SDK)) {
            return getMain().getAddOnInformationManager().getAddOnForClassLoader(identifiable.getClass().getClassLoader())
                    .orElse(null);
        }
        return null;
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

    /**
     * Gets all registered addOnInformations.
     *
     * @return All registered addOnInformations.
     */
    public IdentifiableSet<AddOnInformation> getAllAddOnInformations() {
       return addOnInformations;
    }

    /**
     * adds an addon to selected List
     * @param addOn the addon to Add
     */
    public void addAddonToSelectedList(AddOn addOn) throws IOException {
        ArrayList<AddOn> addOns = new ArrayList<>(selectedAddOns);
        addOns.add(addOn);
        synchro(addOns);
        this.selectedAddOns = addOns;
    }

    /**
     * returns the selected Addons
     * @return the selected
     */
    public List<AddOn> getSelectedAddOns() {
        return selectedAddOns;
    }

    /**
     * sets a new state to the config-file
     * @param status the state to set the file to
     * @throws IOException if an exception occurred while writing to the file
     */
    public void setNewStateToConfig(IzouInstanceStatus.Status status) throws IOException {
        this.internalConfig = internalConfig.createNew(status);
        //needed because only arrayList ist guaranteed to no print
        ArrayList<AddOn> finalAddons = new ArrayList<>(selectedAddOns);
        writeToFile(finalAddons);
    }

    public void initInternalConfigFile(InternalConfig internalConfig) {
        this.internalConfig = internalConfig;
        this.selectedAddOns = internalConfig.addOns;
    }

    private void synchro(List<AddOn> selectedAddOns) throws IOException {
        //needed because only arrayList ist guaranteed to no print
        ArrayList<AddOn> finalAddons = new ArrayList<>(selectedAddOns);
        writeToFile(finalAddons);
    }

    private void writeToFile(ArrayList<AddOn> addOns) throws IOException {
        File configFile = new File(addonConfigFile);
        if (!configFile.exists()) {
            configFile.createNewFile();
        }
        this.internalConfig = internalConfig.createNew(addOns);
        try (FileWriter writer = new FileWriter(addonConfigFile)) {
            YamlWriter yamlWriter = new YamlWriter(writer);
            yamlWriter.getConfig().setPropertyElementType(InternalConfig.class, "addOns", AddOn.class);
            yamlWriter.write(internalConfig);
        }
    }

    /**
     * Gets all registered addOns.
     *
     * @return All registered addOns.
     */
    public IdentifiableSet<AddOnModel> getAllAddOns() {
        return addOns;
    }

    /**
     * Returns the addOn loaded from the given classLoader.
     *
     * @param classLoader The classLoader.
     * @return The (optional) AddOnModel.
     */
    public Optional<AddOnModel> getAddOnForClassLoader(ClassLoader classLoader) {
        return addOns.stream()
                .filter(addOnModel -> addOnModel.getClass().getClassLoader().equals(classLoader))
                .findFirst();
    }
}
