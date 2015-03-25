package intellimate.izou.activator;

import intellimate.izou.AddonThreadPoolUser;
import intellimate.izou.IdentifiableSet;
import intellimate.izou.IzouModule;
import intellimate.izou.identification.IllegalIDException;
import intellimate.izou.main.Main;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The ActivatorManager holds all the Activator-instances and runs them parallel in Threads.
 * It automatically restarts Activators, which did finish exceptionally, up to 100 times.
 */
@SuppressWarnings("WeakerAccess")
public class ActivatorManager extends IzouModule implements AddonThreadPoolUser {
    IdentifiableSet<ActivatorModel> activatorModels = new IdentifiableSet<>();
    ConcurrentHashMap<ActivatorModel, CompletableFuture> futures = new ConcurrentHashMap<>();
    ConcurrentHashMap<ActivatorModel, AtomicInteger> crashCounter = new ConcurrentHashMap<>();
    
    public ActivatorManager(Main main) {
        super(main);
    }

    /**
     * adds an activator and automatically submits it to the Thread-Pool
     * @param activatorModel the activator to add
     * * @throws IllegalIDException not yet implemented
     */
    public void addActivator(ActivatorModel activatorModel) throws IllegalIDException {
        activatorModels.add(activatorModel);
        crashCounter.put(activatorModel, new AtomicInteger(0));
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
                error("Activator: " + activatorModel.getID() + " crashed", e);
                return true;
            }
        }).thenAccept(restart -> {
            if (restart != null && !restart) {
                debug("Activator: " + activatorModel.getID() + " returned false, will not restart");
            } else {
                error("Activator: " + activatorModel.getID() + " returned not true");
                if (crashCounter.get(activatorModel).get() < 100) {
                    error("Until now activator: " + activatorModel.getID() + " was restarted: " +
                            crashCounter.get(activatorModel).get() + " times, attempting restart.");
                    crashCounter.get(activatorModel).incrementAndGet();
                    submitActivator(activatorModel);
                } else {
                    error("Activator: " + activatorModel.getID() + " reached restarting limit with " +
                            crashCounter.get(activatorModel).get() + " restarts.");
                }
            }
        });

        CompletableFuture existing = futures.put(activatorModel, future);
        if (!existing.isDone()) existing.cancel(true);
    }
}
