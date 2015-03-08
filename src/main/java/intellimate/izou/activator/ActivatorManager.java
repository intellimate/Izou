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
    IdentifiableSet<Activator> activators = new IdentifiableSet<>();
    ConcurrentHashMap<Activator, CompletableFuture> futures = new ConcurrentHashMap<>();
    ConcurrentHashMap<Activator, AtomicInteger> crashCounter = new ConcurrentHashMap<>();
    
    public ActivatorManager(Main main) {
        super(main);
    }

    /**
     * adds an activator and automatically submits it to the Thread-Pool
     * @param activator the activator to add
     * * @throws IllegalIDException not yet implemented
     */
    public void addActivator(Activator activator) throws IllegalIDException {
        activators.add(activator);
        crashCounter.put(activator, new AtomicInteger(0));
        submitActivator(activator);
    }

    /**
     * removes the activator and stops the Thread
     * @param activator the activator to remove
     */
    public void removeActivator(Activator activator) {
        activators.remove(activator);
        CompletableFuture remove = futures.remove(activator);
        if (remove != null) {
            remove.cancel(true);
        }
        crashCounter.remove(activator);
    }

    /**
     * submits the activator to the ThreadPool
     * @param activator teh activator to submit
     */
    private void submitActivator(Activator activator) {
        CompletableFuture<Void> future = submit(() -> {
            try {
                return activator.call();
            } catch (Throwable e) {
                error("Activator: " + activator.getID() + "crashed", e);
                return true;
            }
        }).thenAccept(restart -> {
            if (restart != null && !restart) {
                debug("Activator: " + activator.getID() + "returned false, will not restart");
            } else {
                error("Activator: " + activator.getID() + "returned not true");
                if (crashCounter.get(activator).get() < 100) {
                    error("Until now activator: " + activator.getID() + "was restarted: " +
                            crashCounter.get(activator).get() + " times, attempting restart.");
                    crashCounter.get(activator).incrementAndGet();
                    submitActivator(activator);
                } else {
                    error("Activator: " + activator.getID() + "reached restarting limit with " +
                            crashCounter.get(activator).get() + " restarts.");
                }
            }
        });

        CompletableFuture existing = futures.put(activator, future);
        if (!existing.isDone()) existing.cancel(true);
    }
}
