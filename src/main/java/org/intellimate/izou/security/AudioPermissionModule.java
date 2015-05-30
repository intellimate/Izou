package org.intellimate.izou.security;

import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.events.EventMinimalImpl;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.EventsControllerModel;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouSoundPermissionException;
import org.intellimate.izou.system.sound.SoundIDs;
import ro.fortsoft.pf4j.PluginDescriptor;

import javax.sound.sampled.AudioPermission;
import java.security.Permission;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The AudioPermissionModule handles conflicts between addOns regarding audio output. For example if two AddOns
 * want to play music, then the AudioPermissionModule will decide who gets to play it.
 */
public final class AudioPermissionModule extends PermissionModule implements EventsControllerModel {
    private Identification currentPlaybackID;
    private boolean isPlaying;
    private IdentifiableSet<AddOnModel> shortTermPermissions;
    private Map<Identifiable, CompletableFuture> permissionsPending = new ConcurrentHashMap<>();

    /**
     * Creates a new PermissionModule
     *
     * @param main an isntance of main
     */
    AudioPermissionModule(Main main, SecurityManager securityManager) {
        super(main, securityManager);
        currentPlaybackID = null;
        isPlaying = false;
        shortTermPermissions = new IdentifiableSet<>();
        try {
            main.getEventDistributor().registerEventsController(this);
        } catch (IllegalIDException e) {
            error("unable to register as EventsController!");
            System.exit(1);
        }
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
        if (addOn.isOwner(currentPlaybackID))
            return;

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

    /**
     * Controls whether the fired Event should be dispatched to all the listeners
     * <p>
     * This method should execute quickly
     *
     * @param event the ID of the event
     * @return true if events should be dispatched
     */
    @Override
    public boolean controlEventDispatcher(EventModel event) {
        if (event.containsDescriptor(SoundIDs.StartEvent.descriptor)) {
            if (isPlaying && !currentPlaybackID.equals(event.getSource())) {
                IdentificationManager.getInstance().getIdentification(this)
                        .map(id -> new EventMinimalImpl(SoundIDs.EndedEvent.type, id, SoundIDs.EndedEvent.descriptors))
                        .ifPresent(stopEvent -> getMain().getEventDistributor().fireEventConcurrently(event));
                return false;
            } else if (!isPlaying) {
                isPlaying = true;
                currentPlaybackID = event.getSource();
            }
        }
        return true;
    }
}
