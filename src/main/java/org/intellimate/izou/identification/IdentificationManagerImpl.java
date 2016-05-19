package org.intellimate.izou.identification;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.addon.AddOnModel;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * You can register an Object with the IdentificationManager and receive an Identification Objects.
 */
//FIXME: ConcurrentModification Exception with streams
final class IdentificationManagerImpl implements IdentificationManagerM, IdentificationManager {
    private Map<String, Identifiable> registered = new ConcurrentHashMap<>();
    private AddOnInformationManager addOnInformationManager = null;
    static IdentificationManagerImpl singletonInstance = new IdentificationManagerImpl();
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    private IdentificationManagerImpl() {

    }



    /**
     * sets the AddOnInformationManager if not initialized
     * @param addOnInformationManager the addOnInformationManager
     */
    public void setAddOnInformationManager(AddOnInformationManager addOnInformationManager) {
        if (this.addOnInformationManager == null) {
            this.addOnInformationManager = addOnInformationManager;
        }
    }

    /**
     * If you have registered with an Identifiable interface, you can receive Identification Instances with this method.
     * @param identifiable the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    @Override
    public Optional<Identification> getIdentification(Identifiable identifiable) {
        if(!registered.containsKey(identifiable.getID())) {
            return Optional.empty();
        }
        AddOnModel registered = addOnInformationManager.getAddonModel(this.registered.get(identifiable.getID()));
        AddOnModel requested = addOnInformationManager.getAddonModel(identifiable);
        if (!(registered == requested)) {
            return Optional.empty();
        }
        return Optional.of(IdentificationImpl.createIdentification(identifiable, true));
    }

    /**
     * If a class has registered with an Identifiable interface you can receive an Identification Instance describing
     * the class by providing his ID.
     * @param id the ID of the registered Identifiable
     * @return an Identification Instance or null if not registered
     */
    @Override
    public Optional<Identification> getIdentification(String id) {
        return Optional.ofNullable(registered.get(id))
                .map(IdentificationImpl::createIdentification);
    }

    /**
     * Registers an Identifiable, ID has to be unique.
     * @param identifiable the Identifiable to register
     * @return true if registered/already registered or false if the ID is already existing
     */
    @Override
    public boolean registerIdentification(Identifiable identifiable) {
        if (identifiable == null || identifiable.getID() == null || identifiable.getID().isEmpty()) return false;
        if (registered.containsValue(identifiable) || registered.containsKey(identifiable.getID())) {
            return false;
        }
        registered.put(identifiable.getID(), identifiable);
        return true;
    }
}
