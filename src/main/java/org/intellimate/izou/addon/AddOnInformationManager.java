package org.intellimate.izou.addon;

import javax.management.InstanceAlreadyExistsException;
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
     * @return a new AddOnInformationManager instance if none existed before
     * @throws InstanceAlreadyExistsException thrown if this method is called when an instance of AddOnInformationManager
     *          has already been created.
     */
    public static AddOnInformationManager createAddOnInformationManager() throws InstanceAlreadyExistsException {
        if (addOnInformationManager == null) {
            addOnInformationManager = new AddOnInformationManager();
            return addOnInformationManager;
        } else {
            throw new InstanceAlreadyExistsException("AddOnInformationManager has already been instantiated");
        }
    }

    public static AddOnInformationManager getInstance() {
        return addOnInformationManager;
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
