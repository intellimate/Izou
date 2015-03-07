package intellimate.izou.system.context;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface ThreadPool {
    /**
     * Submits a new Callable to the ThreadPool
     * @param callable the callable to submit
     * @param <V> the type of the callable
     * @return a Future representing pending completion of the task
     */
    <V> Future<V> submitToIzouThreadPool(Callable<V> callable);

    /**
     * returns an ThreadPool where all the IzouPlugins are running
     * @return an instance of ExecutorService
     */
    ExecutorService getThreadPool();
}
