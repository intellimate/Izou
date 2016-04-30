package org.intellimate.izou.addon;

import java.util.concurrent.ConcurrentHashMap;

/**
 * This class contains a list of {@link AddOnInformation} objects. The class is thread-safe.
 */
public final class AddOnInformationManager {
    private static AddOnInformationManager addOnInformationManager = null;

    private final ConcurrentHashMap<String, AddOnInformation> addOnInformations;

    /**
     * Creates a new instance of the AddOnInformationManager if none exists already, else InstanceAlreadyExistsException
     * is thrown.
     *
     * @return A new AddOnInformationManager instance if none existed before.
     */
    public static AddOnInformationManager getInstance() {
        if (addOnInformationManager == null) {
            addOnInformationManager = new AddOnInformationManager();
            return addOnInformationManager;
        } else {
            return addOnInformationManager;
        }
    }

    /**
     * Creates a new instance of AddOnInformationManager.
     */
    private AddOnInformationManager() {
        addOnInformations = new ConcurrentHashMap<>();
    }

    /**
     * Adds a new addOnInformation to the map - package local so that only {@link AddOnManager} can use this method
     *
     * @param addOnInformation The addOnInformation to add
     */
    void addAddOnInformation(AddOnInformation addOnInformation) {
        addOnInformations.put(addOnInformation.getID(), addOnInformation);
    }

    /**
     * Removes a addOnInformation from the map - package local so that only {@link AddOnManager} can use this method
     *
     * @param id The ID of the AddOnInformation to remove
     */
    void remove(String id) {
        addOnInformations.remove(id);
    }

    /**
     * Gets the {@link AddOnInformation} mapped to the given key.
     *
     * @param id The ID of the AddOnInformation to get.
     * @return The map matched to the addOns ID.
     */
    public synchronized AddOnInformation getAddOnInformations(String id) {
        return addOnInformations.get(id);
    }
}
