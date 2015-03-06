package intellimate.izou.activator;

import intellimate.izou.AddOnsCollection;
import intellimate.izou.IzouModule;
import intellimate.izou.main.Main;

/**
 * The ActivatorManager holds all the Activator-instances and runs them parallel in Threads.
 */
@SuppressWarnings("WeakerAccess")
public class ActivatorManager extends IzouModule{
    AddOnsCollection<Activator>
    public ActivatorManager(Main main) {
        super(main);
    }
//    private final Main main;
//    private final ExecutorService executor;
//    private final LocalEventManager localEventManager;
//    private final Logger fileLogger = LogManager.getLogger(this.getClass());
//
//    public ActivatorManager(Main main) {
//        this.main = main;
//        this.executor = main.getThreadPoolManager().getAddOnsThreadPool();
//        this.localEventManager = main.getLocalEventManager();
//    }
//
//    /**
//     * Adds an activator-Instance
//     *
//     * The function activator.activatorStarts() will be called asynchronously.
//     * Assuming no error happens, the activator will run indefinitely in his own Thread.
//     *
//     * @param activator the activator instance to be called
//     * @return a Future object, Future. Future.cancel(true) will (if the activator is coded that it honors the
//     * interruption) cancel the activator
//     */
//    public java.util.concurrent.Future<?> addActivator(Activator activator) {
//        return executor.submit(activator);
//    }
//
//    /**
//     * restarts an activator
//     * @param activator the activator to restart
//     */
//    public void restartActivator(Activator activator) {
//        executor.submit(activator);
//    }
    
}
