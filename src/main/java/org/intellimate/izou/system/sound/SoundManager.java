package org.intellimate.izou.system.sound;

import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.EventsControllerModel;
import org.intellimate.izou.main.Main;

import java.lang.ref.WeakReference;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
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
    private ConcurrentHashMap<AddOnModel, List<WeakReference<IzouSoundLine>>> nonPermanent = new ConcurrentHashMap<>();
    private List<WeakReference<IzouSoundLine>> permanentLines = null;
    private AddOnModel permanentAddOn = null;
    //Addon has 10 sec to obtain an IzouSoundLine
    private LocalDateTime permissionWithoutUsageLimit = null;
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
                    List<WeakReference<IzouSoundLine>> collect = entry.getValue().stream()
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
    public void addIzouSoundLine(AddOnModel addOnModel, IzouSoundLine izouSoundLine) {
        if (permanentAddOn != null && permanentAddOn.equals(addOnModel)) {
            addPermanent(izouSoundLine);
        } else {
            addNonPermanent(addOnModel, izouSoundLine);
        }
        izouSoundLine.registerCloseCallback(voit -> closeCallback(addOnModel, izouSoundLine));
    }

    /**
     * the close-callback or the AddonModel, removes now redundant references
     * @param addOnModel the addOnModel where the IzouSoundLine belongs to
     * @param izouSoundLine the izouSoundLine
     */
    private void closeCallback(AddOnModel addOnModel, IzouSoundLine izouSoundLine) {
        Predicate<WeakReference<IzouSoundLine>> removeFromList =
                weakReference -> weakReference.get() != null && weakReference.get().equals(izouSoundLine);
        if (permanentAddOn != null && permanentAddOn.equals(addOnModel) && permanentLines != null) {
            permanentLines.removeIf(removeFromList);
            if (permanentLines.isEmpty()) {
                permanentLines = null;
                permissionWithoutUsageLimit = LocalDateTime.now().plus(10, ChronoUnit.SECONDS);
            }
        }
        List<WeakReference<IzouSoundLine>> weakReferences = nonPermanent.get(addOnModel);
        if (weakReferences != null) {
            weakReferences.removeIf(removeFromList);
        }
        submit(this::tidy);
    }

    /**
     * adds the IzouSoundLine as permanent
     * @param izouSoundLine the izouSoundLine to add
     */
    private void addPermanent(IzouSoundLine izouSoundLine) {
        if (!izouSoundLine.isPermanent())
            izouSoundLine.setToPermanent();
        if (permissionWithoutUsageLimit == null) {
            permissionWithoutUsageLimit = null;
        }
        if (permanentLines == null) {
            permanentLines = Collections.synchronizedList(new ArrayList<>());
        }
        permanentLines.add(new WeakReference<>(izouSoundLine));
    }

    /**
     * adds the IzouSoundLine as NonPermanent
     * @param addOnModel the AddonModel to
     * @param izouSoundLine the IzouSoundLine to add
     */
    private void addNonPermanent(AddOnModel addOnModel, IzouSoundLine izouSoundLine) {
        if (izouSoundLine.isPermanent())
            izouSoundLine.setToNonPermanent();
        List<WeakReference<IzouSoundLine>> weakReferences = nonPermanent.get(addOnModel);
        if (weakReferences == null)
            weakReferences = Collections.synchronizedList(new ArrayList<>());
        nonPermanent.put(addOnModel, weakReferences);
        weakReferences.add(new WeakReference<>(izouSoundLine));
    }

    /**
     * tries to register the AddonModel as permanent
     * @param addOnModel the AddonModel to register
     * @return trie if registered, false if not
     */
    public boolean requestPermanent(AddOnModel addOnModel) {
        boolean notUsing = isUsing.compareAndSet(false, true);
        if (!notUsing) {
            if (permissionWithoutUsageLimit != null && permissionWithoutUsageLimit.isBefore(LocalDateTime.now())) {
                permissionWithoutUsageLimit = null;
            } else {
                return false;
            }
        }
        permanentAddOn = addOnModel;
        List<WeakReference<IzouSoundLine>> weakReferences = nonPermanent.remove(addOnModel);
        if (weakReferences == null) {
            permissionWithoutUsageLimit = LocalDateTime.now().plus(10, ChronoUnit.SECONDS);
        } else {
            nonPermanent.remove(addOnModel);
            permanentLines = weakReferences;
            permanentLines.forEach(weakReferenceLine -> {
                if (weakReferenceLine.get() != null)
                    weakReferenceLine.get().setToPermanent();
            });
        }
        return true;
    }

    /**
     * unregisters the AddonModel as permanent
     * @param addOnModel the addonModel to check
     */
    public void endPermanent(AddOnModel addOnModel) {
        if (!isUsing.get())
            return;
        permanentAddOn = null;
        if (permanentLines != null) {
            permanentLines.forEach(weakReferenceLine -> {
                if (weakReferenceLine.get() != null)
                    weakReferenceLine.get().setToNonPermanent();
            });
            nonPermanent.put(addOnModel, permanentLines);
            permanentLines = null;
        }
        permissionWithoutUsageLimit = null;
        isUsing.set(false);
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
                !event.containsDescriptor(SoundIDs.StopEvent.descriptor))
            return true;

    }
}
