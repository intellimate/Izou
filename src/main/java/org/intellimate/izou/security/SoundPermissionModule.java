package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * The IzouSoundPermissionManager manages conflicts between addOns regarding audio output. For example if two AddOns
 * want to play music, then the IzouSoundPermissionManager will decide who gets to play it.
 */
public final class SoundPermissionModule extends PermissionModule {
    private final List<String> registeredAddOns;
    private String currentPlaybackID;
    private boolean isPlaying;
    private final Logger logger = LogManager.getLogger(this.getClass());


    /**
     * Creates a new instance of IzouSoundPermissionManager
     */
    public SoundPermissionModule() {
        registeredAddOns = new ArrayList<>();
        currentPlaybackID = null;
        isPlaying = false;
    }

    /**
     * Adds an addOn to the registered addOns list. Only addOns on that list can request to play sound
     *
     * @param addOnID the ID of the addOn to add
     */
    public void registerAddOn(String addOnID) {
        registeredAddOns.add(addOnID);
    }

    /**
     * Checks if an addOn is registered to request audio playback
     *
     * @param addOnID the id of the addOn to check
     * @return true if the addOn is registered to request audio playback, else false
     */
    public boolean isRegistered(String addOnID) {
        return registeredAddOns.contains(addOnID);
    }

    /**
     * Requests permission to play sound, if the permission is granted, the {@link SecurityManager} will let the
     * addOn with {@code addOnID} play sound, else it will block it.
     * <p>
     * There is always ONLY ONE permission that is given out to an addOn, (first come, first serve) and if the
     * permission has already been given out, then the requesting addOn will have to wait until it is returned. Once
     * the this method returns true, the requesting addOn will have been registered as the currently playing addOn, so
     * it is responsible for calling the {@link #returnPlaybackPermission(String)} method to signal it is done. Only
     * then can the next addOn play sound again.
     * </p>
     * @param addOnID the addOn id of the addOn that is requesting permission to play sound
     * @return true if the permission was granted, false if it was denied
     */
    public boolean requestPlaybackPermission(String addOnID) {
        if (!isPlaying) {
            currentPlaybackID = sha3(addOnID);
            isPlaying = true;
            return true;
        }
        return false;
    }

    /**
     * If this method is called from the addOn currently playing sound, then the lock on that addOn is given up and
     * anyone can request to play sound again. If the permission was successfully returned, true is returned and
     * otherwise false. (For instance if {@code addOnID} does not match the currently playing addOn ID)
     *
     * @param addOnID the addOnID of the addOn requesting to return its audio permission
     * @return true if the permission was returned successfully, else false
     */
    public boolean returnPlaybackPermission(String addOnID) {
        if (sha3(addOnID).equals(currentPlaybackID)) {
            isPlaying = false;
            currentPlaybackID = null;
            return true;
        }

        return false;
    }
}
