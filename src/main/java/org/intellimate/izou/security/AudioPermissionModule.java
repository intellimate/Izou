package org.intellimate.izou.security;

import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouSoundPermissionException;
import ro.fortsoft.pf4j.PluginDescriptor;
import ro.fortsoft.pf4j.PluginWrapper;

import javax.sound.sampled.AudioPermission;
import java.security.Permission;
import java.util.function.Function;

/**
 * The AudioPermissionModule handles conflicts between addOns regarding audio output. For example if two AddOns
 * want to play music, then the AudioPermissionModule will decide who gets to play it.
 */
public final class AudioPermissionModule extends PermissionModule {
    private Identifiable currentPlaybackID;
    private boolean isPlaying;
    private IdentifiableSet<AddOnModel> shortTermPermissions;

    /**
     * Creates a new PermissionModule
     *
     * @param main an isntance of main
     */
    AudioPermissionModule(Main main) {
        super(main);
        currentPlaybackID = null;
        isPlaying = false;
        shortTermPermissions = new IdentifiableSet<>();
    }

    /**
     * returns true if able to check permissions
     *
     * @param permission the permission to check
     * @return true if able to, false if not
     */
    @Override
    public boolean canCheckPermission(Permission permission) {
        return permission instanceof AudioPermission;
    }

    /**
     * This method grants a one time audio permission for short sounds
     * <p>
     * If you want to play a short sound over another sound (for example a status tone or something else) you can use
     * this method to get permission to play audio over currently playing audio. This method however does not pause the
     * currently playing audio, it does not interfere with it at all. This method should only be used for sounds shorter
     * than 30 seconds, however there is no way right now to stop your sound after 30 seconds, so please do not abuse
     * this. You still need to be registered to play audio in order to use this method
     * </p>
     * @param addOnID the ID of the addOn you would like to grant short term audio permissison to
     * @return true if it has been granted, else false
     */
    public boolean requestShortTermPermission(AddOnModel addOnID) {
        return isRegistered(addOnID) && shortTermPermissions.add(addOnID);
    }

    @Override
    public void checkPermission(Permission permission, AddOnModel addOn) throws IzouSoundPermissionException {
        if (currentPlaybackID.equals(addOn))
            return;
        if (!isPlaying && isRegistered(addOn)) {
            isPlaying = true;
            currentPlaybackID = addOn;
            return;
        }


        String permissionMessage = "Audio Permission Denied: " + addOn + "is not registered to "
                + "play audio or there is already audio being played.";

        registerOrThrow(addOn, permissionMessage);
    }

    /**
     * registers the AddOn or throws the Exception
     * @param addOn the AddOn to register
     * @param permissionMessage the message of the exception
     * @throws IzouSoundPermissionException if not eligible for registering
     */
    private void registerOrThrow(AddOnModel addOn, String permissionMessage) throws IzouSoundPermissionException{
        Function<PluginDescriptor, Boolean> checkPlayPermission = descriptor -> {
            try {
                return descriptor.getAddOnProperties().get("audio_output").equals("true")
                        && !descriptor.getAddOnProperties().get("audio_usage_descripton").equals("null");
            } catch (NullPointerException e) {
                return false;
            }
        };

        getMain().getAddOnManager().getPluginWrapper(addOn)
                .map(PluginWrapper::getDescriptor)
                .map(checkPlayPermission)
                .ifPresent(allowedToPlay -> {
                    if (allowedToPlay) {
                        registerAddOn(addOn);
                    } else {
                        throw new IzouSoundPermissionException(permissionMessage);
                    }
                });
    }

    /**
     * checks if the Addon is able to play audio
     * @param addOn the addon to check
     * @throws IzouSoundPermissionException thrown if the addOn is not allowed to access its requested service
     */
    public void checkShortTimePermission(Permission permission, AddOnModel addOn) throws IzouSoundPermissionException {
        if (!isRegistered(addOn)) {
            String message = "Audio Permission Denied: " + addOn + "is not registered to "
                    + "play audio.";
            registerOrThrow(addOn, message);
        }
    }
}
