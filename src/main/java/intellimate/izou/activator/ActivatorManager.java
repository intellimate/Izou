package intellimate.izou.activator;

import intellimate.izou.events.LocalEventManager;
import intellimate.izou.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.ExecutorService;

/**
 * The ActivatorManager holds all the Activator-instances and runs them parallel in Threads.
 */
@SuppressWarnings("WeakerAccess")
public class ActivatorManager {
    private final Main main;
    private final ExecutorService executor;
    private final LocalEventManager localEventManager;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    public ActivatorManager(Main main) {
        this.main = main;
        this.executor = main.getThreadPoolManager().getAddOnsThreadPool();
        this.localEventManager = main.getLocalEventManager();
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
     * restarts an activator
     * @param activator the activator to restart
     */
    public void restartActivator(Activator activator) {
        executor.submit(activator);
    }
}
