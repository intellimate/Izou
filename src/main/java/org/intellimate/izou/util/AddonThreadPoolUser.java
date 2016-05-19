package org.intellimate.izou.util;

import ro.fortsoft.pf4j.AddonAccessible;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * ALWAYS implement this interface when you have to put some Tasks into the ThreadPool
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface AddonThreadPoolUser extends MainProvider {

    /**
     * submits the Runnable to the AddOns Thread-Pool
     * @param runnable the runnable to submit
     * @return the new CompletableFuture
     */
    default CompletableFuture<Void> submit(Runnable runnable) {
        return CompletableFuture.runAsync(runnable, getMain().getThreadPoolManager().getAddOnsThreadPool())
                .whenComplete((u, ex) -> {
                    if (ex != null) {
                        getMain().getThreadPoolManager().handleThrowable(ex, runnable);
                    }
                });
    }

    /**
     * submits the Supplier to the AddOns Thread-Pool 
     * @param supplier the supplier executed
     * @param <U> the return type
     * @return the new CompletableFuture
     */
    default <U> CompletableFuture<U> submit(Supplier<U> supplier) {
        return CompletableFuture.supplyAsync(supplier, getMain().getThreadPoolManager().getAddOnsThreadPool())
                .whenComplete((u, ex) -> {
                    if (ex != null) {
                        getMain().getThreadPoolManager().handleThrowable(ex, supplier);
                    }
                });
    }

    /**
     * times out the collection of futures
     * @param futures the collection of futures
     * @param milliseconds the limit in milliseconds (everything under 20 milliseconds makes no sense)
     * @param <U> the return type of the futures
     * @param <V> the type of the futures
     * @return a List of futures
     * @throws InterruptedException if the process was interrupted
     */
    default <U, V extends Future<U>> List<V> timeOut(Collection<? extends V> futures,
                                                   int milliseconds) throws InterruptedException {
        //Timeout
        int start = 0;
        boolean notFinished = true;
        while ( (start < milliseconds) && notFinished) {
            notFinished = futures.stream()
                    .anyMatch(future -> !future.isDone());
            start = start + 10;
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw e;
            }
        }

        //cancel all running tasks
        if(notFinished) {
            futures.stream()
                    .filter(future -> !future.isDone())
                    .peek(future -> error(future.toString()+ " timed out",
                            new Exception(future.toString() + " timed out")))
                    .forEach(future -> future.cancel(true));
        }
        return futures.stream()
                .filter(Future::isDone)
                .collect(Collectors.<V>toList());
    }
}
