package intellimate.izou.activator;

import intellimate.izou.events.EventManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import intellimate.izou.events.LocalEventManager;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The ActivatorManager holds all the Activator-instances and runs them parallel in Threads.
 */
@SuppressWarnings("WeakerAccess")
public class ActivatorManager {
    private final ExecutorService executor = Executors.newCachedThreadPool(new CustomThreadFactory());
    private final LocalEventManager localEventManager;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    public ActivatorManager(LocalEventManager localEventManager) {
        this.localEventManager = localEventManager;
    }

    /**
     * Adds an activator-Instance
     *
     * The function activator.activatorStarts() will be called asynchronously.
     * Assuming no error happens, the activator will run indefinitely in his own Thread.
     *
     * @param activator the activator instance to be called
     * @return a Future object, Future. Future.cancel(true) will (if the activator is coded that it honors the
     * interruption) cancel the activator
     */
    public java.util.concurrent.Future<?> addActivator(Activator activator) {
        activator.registerAllNeededDependencies(localEventManager, this);
        return executor.submit(activator);
    }

    /**
     * used to catch Exception in threads
     */
    private class CustomThreadFactory implements ThreadFactory {
        @SuppressWarnings("NullableProblems")
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        r.run();
                    } catch (Exception e) {
                        try {
                            Field target = Thread.class.getDeclaredField("target");
                            target.setAccessible(true);
                            Activator activator = (Activator) target.get(this);
                            activator.exceptionThrown(e);
                        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException ecp ) {
                            fileLogger.fatal(e.getMessage());
                        }
                    }
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }
}
