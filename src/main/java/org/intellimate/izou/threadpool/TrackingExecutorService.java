package org.intellimate.izou.threadpool;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.IllegalIDException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * This is a ExecutorService where the Source of every submitted Thread is easily identifiable.
 * <p>
 * The future purpose of this class is to track which addOn started which task, which makes it possible to hotswap
 * AddOns.
 * </p>
 * @author Leander Kurscheidt
 * @version 1.0
 */
//at the moment this is essentially a dummy class, but it is needed to advance the Identification-system (hotswap, kill
// AddOn etc)
@SuppressWarnings("NullableProblems")
public class TrackingExecutorService implements ExecutorService {
    private final ExecutorService executorService;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final Identifiable identifiable;

    private TrackingExecutorService(ExecutorService executorService, Identifiable identifiable) {
        this.executorService = executorService;
        this.identifiable = identifiable;
    }

    /**
     * creates a new ExecutorService
     * @param executorService the ExecutorService to use under the hood
     * @param identifiable the identifiable to associate each submitted thread with
     * @return an instance of TrackingExecutorService
     * @throws IllegalIDException not yet implemented
     */
    public static TrackingExecutorService createTrackingExecutorService(ExecutorService executorService,
                                                                        Identifiable identifiable) throws IllegalIDException {
        return new TrackingExecutorService(executorService, identifiable);
    }

    /**
     * method not implemented
     */
    @Override
    //(maybe set executorService to null?)
    public void shutdown() {}

    /**
     * method not fully implemented.
     * It will not return the List of waiting Runnables and after calling it is still possible to submit Tasks
     * @return empty list
     */
    @Override
    public List<Runnable> shutdownNow() {
        return new ArrayList<>();
    }

    /**
     * method not implemented, will always return false
     */
    @Override
    public boolean isShutdown() {
        return executorService.isShutdown();
    }

    /**
     * method not implemented, will always return false
     */
    @Override
    public boolean isTerminated() {
        return executorService.isTerminated();
    }

    /**
     * method not implemented, will do nothing
     */
    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    /**
     * submits a Task to the ThreadPool
     * @param task the Task to submit
     * @param <T> the type of the return
     * @return null if unable to obtain Identification
     */
    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return executorService.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return executorService.submit(task, result);
    }

    @Override
    public Future<?> submit(Runnable task) {
        return executorService.submit(task);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return executorService.invokeAll(tasks);
    }

    @Override
    public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return executorService.invokeAll(tasks, timeout, unit);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return executorService.invokeAny(tasks);
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return executorService.invokeAny(tasks, timeout, unit);
    }

    @Override
    public void execute(Runnable command) {
        executorService.execute(command);
    }
}
