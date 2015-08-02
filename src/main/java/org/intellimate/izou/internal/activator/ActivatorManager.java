package org.intellimate.izou.internal.activator;

import org.intellimate.izou.activator.ActivatorModel;
import org.intellimate.izou.internal.util.AddonThreadPoolUser;
import org.intellimate.izou.internal.util.IdentifiableSet;
import org.intellimate.izou.internal.util.IzouModule;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.internal.main.Main;
import org.intellimate.izou.internal.security.exceptions.IzouPermissionException;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ActivatorManager holds all the Activator-instances and runs them parallel in Threads.
 * It automatically restarts Activators, which did finish exceptionally, up to 100 times.
 */
@SuppressWarnings("WeakerAccess")
public class ActivatorManager extends IzouModule implements AddonThreadPoolUser {
    private final int MAX_CRASH = 100;
    private final int MAX_PERMISSION_DENIED = 2;
    IdentifiableSet<ActivatorModel> activatorModels = new IdentifiableSet<>();
    ConcurrentHashMap<ActivatorModel, CompletableFuture> futures = new ConcurrentHashMap<>();
    ConcurrentHashMap<ActivatorModel, AtomicInteger> crashCounter = new ConcurrentHashMap<>();
    ConcurrentHashMap<ActivatorModel, AtomicInteger> permissionDeniedCounter = new ConcurrentHashMap<>();
    private List<URL> aspectsOrAffected = Collections.synchronizedList(new ArrayList<>());
    
    public ActivatorManager(Main main) {
        super(main);
    }

    /**
     * adds an activator and automatically submits it to the Thread-Pool
     * @param activatorModel the activator to add
     * @throws IllegalIDException not yet implemented
     */
    public void addActivator(ActivatorModel activatorModel) throws IllegalIDException {
        activatorModels.add(activatorModel);
        crashCounter.put(activatorModel, new AtomicInteger(0));
        permissionDeniedCounter.put(activatorModel, new AtomicInteger(0));
        submitActivator(activatorModel);
    }

    /**
     * removes the activator and stops the Thread
     * @param activatorModel the activator to remove
     */
    public void removeActivator(ActivatorModel activatorModel) {
        activatorModels.remove(activatorModel);
        CompletableFuture remove = futures.remove(activatorModel);
        if (remove != null) {
            remove.cancel(true);
        }
        crashCounter.remove(activatorModel);
        permissionDeniedCounter.remove(activatorModel);
    }

    /**
     * submits the activator to the ThreadPool
     * @param activatorModel teh activator to submit
     */
    private void submitActivator(ActivatorModel activatorModel) {
        CompletableFuture<Void> future = submit(() -> {
            try {
                return activatorModel.call();
            } catch (Throwable e) {
                if (e instanceof IzouPermissionException) {
                    error("Activator: " + activatorModel.getID() + " was denied permission.", e);

                    // Return null if permission was denied by a permission module, in this case restart 2 times
                    return null;
                } else if (e instanceof SecurityException) {
                    error("Activator: " + activatorModel.getID() + " was denied access.", e);

                    // Return false if access was denied by the security manager, in this case, do not restart
                    return false;
                }
                error("Activator: " + activatorModel.getID() + " crashed", e);

                // Return true if the addOn did not crash because of security reasons, restart 100 times
                return true;
            }
        }).thenAccept(restart -> {
            if (restart != null && restart.equals(false)) {
                debug("Activator: " + activatorModel.getID() + " returned false, will not restart");
            } else if (restart == null) {
                error("Activator: " + activatorModel.getID() + " returned not true");
                if (permissionDeniedCounter.get(activatorModel).get() < MAX_PERMISSION_DENIED) {
                    error("Until now activator: " + activatorModel.getID() + " was restarted: " +
                            permissionDeniedCounter.get(activatorModel).get() + " times, attempting restart.");
                    permissionDeniedCounter.get(activatorModel).incrementAndGet();
                    submitActivator(activatorModel);
                } else {
                    error("Activator: " + activatorModel.getID() + " reached permission based restarting limit with " +
                            permissionDeniedCounter.get(activatorModel).get() + " restarts.");
                }
            } else {
                if (crashCounter.get(activatorModel).get() < MAX_CRASH) {
                    error("Until now activator: " + activatorModel.getID() + " was restarted: " +
                            crashCounter.get(activatorModel).get() + " times, attempting restart.");
                    crashCounter.get(activatorModel).incrementAndGet();
                    submitActivator(activatorModel);
                } else {
                    error("Activator: " + activatorModel.getID() + " reached crash based restarting limit with " +
                            crashCounter.get(activatorModel).get() + " restarts.");
                }
            }
        });

        CompletableFuture existing = futures.put(activatorModel, future);
        if (existing != null && !existing.isDone()) existing.cancel(true);
    }
}
