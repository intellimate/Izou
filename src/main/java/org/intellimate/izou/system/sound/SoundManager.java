package org.intellimate.izou.system.sound;

import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.events.EventMinimalImpl;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.EventsControllerModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * the SoundManager manages all IzouSoundLine, tracks them and is responsible for enforcing that only one permanent-
 * AddOn can play at one time.
 * @author LeanderK
 * @version 1.0
 */
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
    private Future<Void> permissionWithoutUsageCloseThread = null;
    private final Object permanentUserReadWriteLock = new Object();
    private AtomicBoolean isUsing = new AtomicBoolean(false);

    public SoundManager(Main main) {
        super(main);
        main.getEventDistributor().registerEventsController(this);
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
        if (permanentAddOn != null && permanentAddOn.equals(addOnModel)) {
            addPermanent(izouSoundLine);
        } else {
            addNonPermanent(addOnModel, izouSoundLine);
        }
        izouSoundLine.registerCloseCallback(voit -> closeCallback(addOnModel, izouSoundLine));
        izouSoundLine.registerMuteCallback(voit -> muteOthers(addOnModel));
    }

    /**
     * mutes the other Addons
     * @param addOnModel the addonModel responsible
     */
    private void muteOthers(AddOnModel addOnModel) {
        nonPermanent.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(addOnModel))
                .flatMap(entry -> entry.getValue().stream())
                .map(Reference::get)
                .filter(Objects::nonNull)
                .forEach(izouSoundLine -> izouSoundLine.setMuted(true));
        if (permanentAddOn != null && !permanentAddOn.equals(addOnModel) && permanentLines != null) {
            permanentLines.stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .forEach(izouSoundLine -> izouSoundLine.setMuted(true));
        }
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
     * the close-callback or the AddonModel, removes now redundant references
     * @param addOnModel the addOnModel where the IzouSoundLine belongs to
     * @param izouSoundLine the izouSoundLine
     */
    private void closeCallback(AddOnModel addOnModel, IzouSoundLine izouSoundLine) {
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
     * unmutes all
     */
    private void unmute() {
        nonPermanent.entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .map(Reference::get)
                .filter(Objects::nonNull)
                .forEach(izouSoundLine -> izouSoundLine.setMuted(false));

        if (permanentLines != null)
            permanentLines.stream()
                    .map(Reference::get)
                    .filter(Objects::nonNull)
                    .forEach(izouSoundLine -> izouSoundLine.setMuted(false));
    }

    /**
     * creates a LocaleDateTime-Object 5 seconds in the Future and a Thread which will remove it, if it passes the threshold.
     * the Thread
     */
    private void permissionWithoutUsage() {
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
        if (!izouSoundLine.isPermanent())
            izouSoundLine.setToPermanent();
        synchronized (permanentUserReadWriteLock) {
            endWaitingForUsage();
            if (permanentLines == null) {
                permanentLines = Collections.synchronizedList(new ArrayList<>());
            }
            permanentLines.add(new WeakReference<>(izouSoundLine));
        }
    }

    /**
     * adds the IzouSoundLine as NonPermanent
     * @param addOnModel the AddonModel to
     * @param izouSoundLine the IzouSoundLine to add
     */
    private void addNonPermanent(AddOnModel addOnModel, IzouSoundLineBaseClass izouSoundLine) {
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
     * @return trie if registered, false if not
     */
    public boolean requestPermanent(AddOnModel addOnModel, Identification source) {
        boolean notUsing = isUsing.compareAndSet(false, true);
        if (!notUsing) {
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
            List<WeakReference<IzouSoundLineBaseClass>> weakReferences = nonPermanent.remove(addOnModel);
            if (weakReferences == null) {
                permissionWithoutUsage();
            } else {
                nonPermanent.remove(addOnModel);
                permanentLines = weakReferences;
                permanentLines.forEach(weakReferenceLine -> {
                    if (weakReferenceLine.get() != null)
                        weakReferenceLine.get().setToPermanent();
                });
            }
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
     * Controls whether the fired Event should be dispatched to all the listeners
     * <p>
     * This method should execute quickly
     *
     * @param event the ID of the event
     * @return true if events should be dispatched
     */
    @Override
    public boolean controlEventDispatcher(EventModel event) {
        if (!event.containsDescriptor(SoundIDs.StartEvent.descriptor) ||
                !event.containsDescriptor(SoundIDs.EndedEvent.descriptor))
            return true;
        if (event.containsDescriptor(SoundIDs.StartEvent.descriptor)) {
            try {
                AddOnModel addOnModel = getMain().getSecurityManager().getOrThrowAddOnModelForClassLoader();
                return requestPermanent(addOnModel, event.getSource());
            } catch (IzouPermissionException e) {
                error("no AddonModel found for event: " + event);
                return true;
            }
        } else {
            try {
                AddOnModel addOnModel = getMain().getSecurityManager().getOrThrowAddOnModelForClassLoader();
                endPermanent(addOnModel);
            } catch (IzouPermissionException e) {
                error("no AddonModel found for event: " + event);
            }
        }
        return true;
    }
}
