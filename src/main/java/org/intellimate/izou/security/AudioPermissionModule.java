package org.intellimate.izou.security;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouSoundPermissionException;
import ro.fortsoft.pf4j.PluginDescriptor;

import javax.sound.sampled.AudioPermission;
import java.security.Permission;
import java.util.function.Function;

/**
 * The Audio PermissionModule checks whether one is allowed to play music. It dos not orchestrate the different lines
 * etc. See SoundManager for these functions.
 * @see org.intellimate.izou.system.sound.SoundManager
 */
public final class AudioPermissionModule extends PermissionModule {

    /**
     * Creates a new PermissionModule
     *
     * @param main an isntance of main
     */
    AudioPermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
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

    @Override
    public void checkPermission(Permission permission, AddOnModel addOn) throws IzouSoundPermissionException {
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

        registerOrThrow(addOn, () -> new IzouSoundPermissionException(permissionMessage), checkPlayPermission);
    }
}
