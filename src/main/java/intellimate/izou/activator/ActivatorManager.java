package intellimate.izou.activator;

import intellimate.izou.events.EventManager;

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
    private final EventManager eventManager;

    public ActivatorManager(EventManager eventManager) {
        this.eventManager = eventManager;
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
        activator.registerAllNeededDependencies(eventManager, this);
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
                            //TODO: real Exception handling is not implemented jet
                            e.printStackTrace();
                        }
                    }
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }
}
