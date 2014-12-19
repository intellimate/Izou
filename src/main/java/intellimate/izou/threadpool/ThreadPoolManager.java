package intellimate.izou.threadpool;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Field;
import java.util.concurrent.*;

/**
 * contains all the ThreadPools.
 * @author LeanderK
 * @version 1.0
 */
public class ThreadPoolManager {
    private final Logger fileLogger = LogManager.getLogger(this.getClass());
    //holds the threads
    private final ExecutorService izouThreadPool = Executors.newCachedThreadPool(new LoggingThreadFactory());
    //holds the threads
    private final ExecutorService addOnsThreadPool = Executors.newCachedThreadPool(new LoggingThreadFactory());

    /**
     * returns the ThreadPool where all the Izou-Components are running
     * @return an ExecutorService
     */
    public ExecutorService getIzouThreadPool() {
        return izouThreadPool;
    }

    /**
     * returns the ThreadPool where all the AddOns are running
     * @return an ExecutorService
     */
    public ExecutorService getAddOnsThreadPool() {
        return addOnsThreadPool;
    }

    /**
     * Submits a new AddOn Callable to the ThreadPool
     * @param callable the callable to submit
     * @param <V> the type of the callable
     * @return a Future representing pending completion of the task
     */
    public <V> Future<V> submitToIzouThreadPool(Callable<V> callable) {
        return addOnsThreadPool.submit(callable);
    }

    /**
     * Submits a new AddOn Callable to the ThreadPool
     * @param runnable the runnable to submit
     * @return a Future representing pending completion of the task
     */
    public void submitToIzouThreadPool(Runnable runnable) {
        addOnsThreadPool.submit(runnable);
    }

    /**
     * used to catch Exception in threads
     */
    private class LoggingThreadFactory implements ThreadFactory {
        @SuppressWarnings("NullableProblems")
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread() {
                public void run() {
                    try {
                        r.run();
                    } catch (Exception e) {
                        fileLogger.fatal("Something crashed!",e.getMessage());
                    }
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }

    /**
     * used to catch Exception in threads
     */
    private class CallbackThreadFactory implements ThreadFactory {
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
                            try {
                                ExceptionCallback exceptionCallback = (ExceptionCallback) target.get(this);
                                exceptionCallback.exceptionThrown(e);
                            } catch (IllegalArgumentException | IllegalAccessException e1) {
                                fileLogger.debug("unable to provide callback", e);
                            }
                        } catch (NoSuchFieldException ecp ) {
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
