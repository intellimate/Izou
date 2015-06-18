package org.intellimate.izou.system.sound;

import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.events.EventMinimalImpl;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.EventsControllerModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.main.Main;
import ro.fortsoft.pf4j.AspectOrAffected;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * the SoundManager manages all IzouSoundLine, tracks them and is responsible for enforcing that only one permanent-
 * AddOn can play at one time.
 * @author LeanderK
 * @version 1.0
 */
//TODO: native sound code enforcing (mute, stop(?) etc.)
public class SoundManager extends IzouModule implements AddonThreadPoolUser, EventsControllerModel {
    //non-permanent and general fields
    private ConcurrentHashMap<AddOnModel, List<WeakReference<IzouSoundLineBaseClass>>> nonPermanent = new ConcurrentHashMap<>();
    //not null if this AddOn is currently muting the others Lines
    private AddOnModel muting = null;

    //permanent fields, there is a Read/Write lock!
    private List<WeakReference<IzouSoundLineBaseClass>> permanentLines = null;
    private AddOnModel permanentAddOn = null;
    //gets filled when the event got fired
    private Identification knownIdentification = null;
    //Addon has 10 sec to obtain an IzouSoundLine
    private LocalDateTime permissionWithoutUsageLimit = null;
    //if true we can do nothing to check whether he closed.
    private boolean isUsingNonJava = false;
    private Future<Void> permissionWithoutUsageCloseThread = null;
    private final Object permanentUserReadWriteLock = new Object();
    private AtomicBoolean isUsing = new AtomicBoolean(false);

    public SoundManager(Main main) {
        super(main);
        main.getEventDistributor().registerEventsController(this);

        URL mixerURL = this.getClass().getClassLoader().getResource("org/intellimate/izou/system/sound/replaced/MixerAspect.class");
        AspectOrAffected mixer = new AspectOrAffected(mixerURL,
                "org.intellimate.izou.system.sound.replaced.MixerAspect",
                aClass -> {
                    try {
                        Method init = aClass.getMethod("init", Main.class);
                        init.invoke(null, main);
                        return aClass;
                    } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
                        error("error while trying to initialize the MixerAspect");
                        return aClass;
                    }
                },
                true);
        URL audioSystemURL = this.getClass().getClassLoader().getResource("javax/sound/sampled/AudioSystem.class");
        AspectOrAffected audioSystem = new AspectOrAffected(audioSystemURL,
                "javax.sound.sampled.AudioSystem",
                Function.identity(),
                false);
        getMain().getAddOnManager().addAspectOrAffected(audioSystem);
        getMain().getAddOnManager().addAspectOrAffected(mixer);
    }

    /**
     * removes obsolete references
     */
    private void tidy() {
        nonPermanent.entrySet().stream()
                .map(entry -> {
                    List<WeakReference<IzouSoundLineBaseClass>> collect = entry.getValue().stream()
                            .filter(izouSoundLineWeakReference -> izouSoundLineWeakReference.get() != null)
                            .collect(Collectors.toList());
                    if (!collect.isEmpty()) {
                        nonPermanent.put(entry.getKey(), collect);
                        return null;
                    } else {
                        return entry;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(entry -> nonPermanent.remove(entry.getKey()));
    }

    /**
     * adds an IzouSoundLine, will now be tracked by the SoundManager
     * @param addOnModel the addOnModel where the IzouSoundLine belongs to
     * @param izouSoundLine the IzouSoundLine to add
     */
    public void addIzouSoundLine(AddOnModel addOnModel, IzouSoundLineBaseClass izouSoundLine) {
        debug("adding soundLine " + izouSoundLine + " from " + addOnModel);
        if (permanentAddOn != null && permanentAddOn.equals(addOnModel)) {
            addPermanent(izouSoundLine);
        } else {
            addNonPermanent(addOnModel, izouSoundLine);
        }
        izouSoundLine.registerCloseCallback(voit -> closeCallback(addOnModel, izouSoundLine));
        izouSoundLine.registerMuteCallback(voit -> muteOthers(addOnModel));
    }

    /**
     * the close-callback or the AddonModel, removes now redundant references
     * @param addOnModel the addOnModel where the IzouSoundLine belongs to
     * @param izouSoundLine the izouSoundLine
     */
    private void closeCallback(AddOnModel addOnModel, IzouSoundLine izouSoundLine) {
        debug("removing soundline " + izouSoundLine + " from " + addOnModel);
        Predicate<WeakReference<IzouSoundLineBaseClass>> removeFromList =
                weakReference -> weakReference.get() != null && weakReference.get().equals(izouSoundLine);
        synchronized (permanentUserReadWriteLock) {
            if (permanentAddOn != null && permanentAddOn.equals(addOnModel) && permanentLines != null) {
                permanentLines.removeIf(removeFromList);
                if (permanentLines.isEmpty()) {
                    permanentLines = null;
                    permissionWithoutUsage();
                }
            }
        }
        List<WeakReference<IzouSoundLineBaseClass>> weakReferences = nonPermanent.get(addOnModel);
        if (weakReferences != null) {
            weakReferences.removeIf(removeFromList);
            if (muting != null && muting.equals(addOnModel) && !weakReferences.stream()
                    .map(WeakReference::get)
                    .filter(Objects::nonNull)
                    .findAny()
                    .isPresent())
                unmute();
        }
        submit(this::tidy);
    }

    /**
     * creates a LocaleDateTime-Object 5 seconds in the Future and a Thread which will remove it, if it passes the threshold.
     * the Thread
     */
    private void permissionWithoutUsage() {
        if (isUsingNonJava)
            return;
        synchronized (permanentUserReadWriteLock) {
            permissionWithoutUsageLimit = LocalDateTime.now().plus(5, ChronoUnit.SECONDS);
            permissionWithoutUsageCloseThread =  submit(() -> {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    return;
                }
                endPermanent(permanentAddOn);
                firePermanentEndedNotification();
            });
        }
    }

    /**
     * removes the LocaleDateTime and Thread (if exisiting)
     */
    private void endWaitingForUsage() {
        synchronized (permanentUserReadWriteLock) {
            if (permissionWithoutUsageLimit != null)
                permissionWithoutUsageLimit = null;
            if (permissionWithoutUsageCloseThread != null) {
                permissionWithoutUsageCloseThread.cancel(true);
                permissionWithoutUsageLimit = null;
            }
        }
    }

    /**
     * adds the IzouSoundLine as permanent
     * @param izouSoundLine the izouSoundLine to add
     */
    private void addPermanent(IzouSoundLineBaseClass izouSoundLine) {
        debug("adding " + izouSoundLine + " to permanent");
        if (!izouSoundLine.isPermanent())
            izouSoundLine.setToPermanent();
        synchronized (permanentUserReadWriteLock) {
            endWaitingForUsage();
            if (permanentLines == null) {
                permanentLines = Collections.synchronizedList(new ArrayList<>());
            }
            permanentLines.add(new WeakReference<>(izouSoundLine));
        }
        //TODO: STOP the addon via the stop event
    }

    /**
     * adds the IzouSoundLine as NonPermanent
     * @param addOnModel the AddonModel to
     * @param izouSoundLine the IzouSoundLine to add
     */
    private void addNonPermanent(AddOnModel addOnModel, IzouSoundLineBaseClass izouSoundLine) {
        debug("adding " + izouSoundLine + " from " + addOnModel + " to non-permanent");
        if (izouSoundLine.isPermanent())
            izouSoundLine.setToNonPermanent();
        List<WeakReference<IzouSoundLineBaseClass>> weakReferences = nonPermanent.get(addOnModel);
        if (weakReferences == null)
            weakReferences = Collections.synchronizedList(new ArrayList<>());
        nonPermanent.put(addOnModel, weakReferences);
        weakReferences.add(new WeakReference<>(izouSoundLine));
    }

    /**
     * tries to register the AddonModel as permanent
     * @param addOnModel the AddonModel to register
     * @param source the Source which requested the usage
     * @param nonJava true if it is not using java to play sounds
     * @return trie if registered, false if not
     */
    public boolean requestPermanent(AddOnModel addOnModel, Identification source, boolean nonJava) {
        debug("requesting permanent for addon: " + addOnModel);
        boolean notUsing = isUsing.compareAndSet(false, true);
        if (!notUsing) {
            debug("already used by " + permanentAddOn);
            synchronized (permanentUserReadWriteLock) {
                if (permanentAddOn.equals(addOnModel)) {
                    if (knownIdentification == null)
                        knownIdentification = source;
                    return true;
                }
                if (permissionWithoutUsageLimit != null && permissionWithoutUsageLimit.isBefore(LocalDateTime.now())) {
                    endWaitingForUsage();
                } else {
                    return false;
                }
                permanentAddOn = addOnModel;
            }
        }
        synchronized (permanentUserReadWriteLock) {
            permanentAddOn = addOnModel;
            knownIdentification = source;
            isUsingNonJava = nonJava;
            permissionWithoutUsageLimit = null;
            if (permissionWithoutUsageCloseThread != null)
                permissionWithoutUsageCloseThread.cancel(true);
            permissionWithoutUsageCloseThread = null;

            List<WeakReference<IzouSoundLineBaseClass>> weakReferences = nonPermanent.remove(addOnModel);
            if (weakReferences == null) {
                if (isUsingNonJava) {
                    permanentLines = new ArrayList<>();
                } else {
                    permissionWithoutUsage();
                }
            } else {
                nonPermanent.remove(addOnModel);
                permanentLines = weakReferences;
                permanentLines.forEach(weakReferenceLine -> {
                    IzouSoundLineBaseClass izouSoundLineBaseClass = weakReferenceLine.get();
                    if (izouSoundLineBaseClass != null) {
                        izouSoundLineBaseClass.setToPermanent();
                        izouSoundLineBaseClass.setResponsibleID(source);
                    }
                });
            }
            if (muting.equals(addOnModel))
                unmute();
        }
        return true;
    }

    /**
     * unregisters the AddonModel as permanent
     * @param addOnModel the addonModel to check
     */
    public void endPermanent(AddOnModel addOnModel) {
        if (!isUsing.get() || !permanentAddOn.equals(addOnModel))
            return;
        synchronized (permanentUserReadWriteLock) {
            permanentAddOn = null;
            knownIdentification = null;
            if (permanentLines != null) {
                permanentLines.forEach(weakReferenceLine -> {
                    if (weakReferenceLine.get() != null)
                        weakReferenceLine.get().setToNonPermanent();
                });
                nonPermanent.put(addOnModel, permanentLines);
                permanentLines = null;
            }
            endWaitingForUsage();
            isUsing.set(false);
        }
    }

    /**
     * fires the EndedEvent event.
     */
    private void firePermanentEndedNotification() {
        if (knownIdentification != null) {
            EventModel event = new EventMinimalImpl(SoundIDs.EndedEvent.type, knownIdentification, SoundIDs.EndedEvent.descriptors);
            getMain().getEventDistributor().fireEventConcurrently(event);
        }
    }

    /**
     * mutes the other Addons
     * @param addOnModel the addonModel responsible
     */
    private void muteOthers(AddOnModel addOnModel) {
        List<IzouSoundLineBaseClass> toMute = nonPermanent.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(addOnModel))
                .flatMap(entry -> entry.getValue().stream())
                .map(Reference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if (permanentAddOn != null && !permanentAddOn.equals(addOnModel) && permanentLines != null) {
            toMute.addAll(
                    permanentLines.stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList())
            );
        }
        mute(toMute);
        List<WeakReference<IzouSoundLineBaseClass>> weakReferences = nonPermanent.get(addOnModel);
        if (weakReferences != null) {
            weakReferences.stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .forEach(izouSoundLine -> izouSoundLine.setMuted(false));
        }
        muting = addOnModel;
    }

    /**
     * mutes the list of soundLines and fires the Mute-Event
     * @param soundLines the soundLines to mute
     */
    private void mute(List<IzouSoundLineBaseClass> soundLines) {
        soundLines.forEach(soundLine -> soundLine.setMuted(true));
        IdentificationManager.getInstance()
                .getIdentification(this)
                .map(id -> new EventMinimalImpl(SoundIDs.MuteEvent.type, id, SoundIDs.MuteEvent.descriptors))
                .ifPresent(event -> getMain().getEventDistributor().fireEventConcurrently(event));
    }

    /**
     * unmutes the list of soundLines and fires the UnMute-Event
     * @param soundLines the soundlines to unmute
     */
    private void unMute(List<IzouSoundLineBaseClass> soundLines) {
        soundLines.forEach(soundLine -> soundLine.setMuted(false));
        IdentificationManager.getInstance()
                .getIdentification(this)
                .map(id -> new EventMinimalImpl(SoundIDs.UnMuteEvent.type, id, SoundIDs.UnMuteEvent.descriptors))
                .ifPresent(event -> getMain().getEventDistributor().fireEventConcurrently(event));
    }

    /**
     * unmutes all
     */
    private void unmute() {
        List<IzouSoundLineBaseClass> unmute = nonPermanent.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(Reference::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (permanentLines != null)
            unmute.addAll(
                    permanentLines.stream()
                            .map(Reference::get)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList())
            );
        unMute(unmute);
    }

    /**
     * Controls whether the fired Event should be dispatched to all the listeners
     * <p>
     * This method should execute quickly.
     * </p>
     *
     * @param event the ID of the event
     * @return true if events should be dispatched
     */
    @Override
    public boolean controlEventDispatcher(EventModel event) {
        if (!event.containsDescriptor(SoundIDs.StartEvent.descriptor) &&
                !event.containsDescriptor(SoundIDs.EndedEvent.descriptor))
            return true;
        if (event.containsDescriptor(SoundIDs.StartEvent.descriptor)) {
            AddOnModel addOnModel = getMain().getInternalIdentificationManager().getAddonModel(event.getSource());
            if (addOnModel == null) {
                error("no AddonModel found for event: " + event);
                return true;
            }
            return requestPermanent(addOnModel, event.getSource(), event.containsDescriptor(SoundIDs.StartEvent.isUsingNonJava));
        } else {
            AddOnModel addOnModel = getMain().getInternalIdentificationManager().getAddonModel(event.getSource());
            if (addOnModel == null) {
                error("no AddonModel found for event: " + event);
                return true;
            }
            endPermanent(addOnModel);
        }
        return true;
    }
}
