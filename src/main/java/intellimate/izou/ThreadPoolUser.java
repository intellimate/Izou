package intellimate.izou;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;

/**
 * ALWAYS implement this interface when you have to put some Tasks into the ThreadPool
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface ThreadPoolUser extends MainProvider {

    /**
     * submits the Runnable to the AddOns Thread-Pool
     * @param runnable the runnable to submit
     * @return the new CompletableFuture
     */
    default CompletableFuture<Void> submit(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, getMain().getThreadPoolManager().getAddOnsThreadPool());
    }

    /**
     * submits the Callable to the AddOns Thread-Pool
     * @param callable the Callable to submit
     * @return a Future representing pending completion of the task
     */
    default <V> Future<V> submit(Callable<V> callable) {
        return getMain().getThreadPoolManager().getAddOnsThreadPool().submit(callable);
    }

    /**
     * submits the Supplier to the AddOns Thread-Pool 
     * @param supplier the supplier executed
     * @param <U> the return type
     * @return the new CompletableFuture
     */
    default <U> CompletableFuture<U> submit(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, getMain().getThreadPoolManager().getAddOnsThreadPool());
    }
}
