package intellimate.izou.activator;

import intellimate.izou.AddOnsCollection;
import intellimate.izou.AddonThreadPoolUser;
import intellimate.izou.IzouModule;
import intellimate.izou.main.Main;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * The ActivatorManager holds all the Activator-instances and runs them parallel in Threads.
 * It automatically restarts Activators, which did finish exceptionally, up to 100 times.
 */
@SuppressWarnings("WeakerAccess")
public class ActivatorManager extends IzouModule implements AddonThreadPoolUser {
    AddOnsCollection<Activator> activators = new AddOnsCollection<>();
    ConcurrentHashMap<Activator, CompletableFuture> futures = new ConcurrentHashMap<>();
    ConcurrentHashMap<Activator, AtomicInteger> crashCounter = new ConcurrentHashMap<>();
    
    public ActivatorManager(Main main) {
        super(main);
    }
    
    public void addActivator(Activator activator) {
        activators.add(activator);
        crashCounter.put(activator, new AtomicInteger(0));
        submitActivator(activator);
    }
    
    public void submitActivator(Activator activator) {
        CompletableFuture<Void> future = submit((Supplier<Boolean>) () -> {
            try {
                return activator.call();
            } catch (Throwable e) {
                log.error("Activator: " + activator.getID() + "crashed", e);
                return true;
            }
        }).thenAccept(restart -> {
            if (restart != null && !restart) {
                log.debug("Activator: " + activator.getID() + "returned false, will not restart");
            } else {
                log.error("Activator: " + activator.getID() + "returned not true");
                if (crashCounter.get(activator).get() < 100) {
                    log.error("Until now activator: " + activator.getID() + "was restarted: " +
                            crashCounter.get(activator).get() + " times, attempting restart.");
                    crashCounter.get(activator).incrementAndGet();
                    submitActivator(activator);
                } else {
                    log.error("Activator: " + activator.getID() + "reached restarting limit with " +
                            crashCounter.get(activator).get() + " restarts.");
                }
            }
        });

        CompletableFuture existing = futures.put(activator, future);
        if (!existing.isDone()) existing.cancel(true);
    }
    
}
