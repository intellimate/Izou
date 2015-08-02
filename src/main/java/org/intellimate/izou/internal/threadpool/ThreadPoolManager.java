package org.intellimate.izou.internal.threadpool;

import org.intellimate.izou.internal.util.IzouModule;
import org.intellimate.izou.internal.main.Main;

import java.lang.reflect.Field;
import java.util.concurrent.*;

/**
 * contains all the ThreadPools.
 * @author LeanderK
 * @version 1.0
 */
public class ThreadPoolManager extends IzouModule {
    //holds the threads
    private final ExecutorService izouThreadPool = Executors.newCachedThreadPool(new LoggingThreadFactory());
    //holds the threads
    private final ExecutorService addOnsThreadPool = Executors.newCachedThreadPool(new LoggingThreadFactory());

    /**
     * creates a new ThreadPoolManager
     * @param main instance of Main
     */
    public ThreadPoolManager(Main main) {
        super(main);
    }

    /**
     * returns the ThreadPool where all the Izou-Components are running
     * <p>this method should only be used by IzouModules</p>
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
     * tries everything to log the exception
     * @param e the Throwable
     * @param target an instance of the thing which has thrown the Exception
     */
    public void handleThrowable(Throwable e, Object target) {
        try {
            ExceptionCallback exceptionCallback = (ExceptionCallback) target;
            if (e instanceof Exception) {
                exceptionCallback.exceptionThrown((Exception) e);
            } else {
                exceptionCallback.exceptionThrown(new RuntimeException(e));
            }
        } catch (IllegalArgumentException | ClassCastException e1) {
            log.fatal("unable to provide callback for: " + target.toString(), e);
        }
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
                    } catch (Exception | LinkageError e) {
                        try {
                            Field target = Thread.class.getDeclaredField("target");
                            target.setAccessible(true);
                            try {
                                handleThrowable(e, target.get(this));
                            } catch (IllegalAccessException e1) {
                                log.fatal("unable to provide callback for: " + target.toString(), e);
                            }
                        } catch (NoSuchFieldException e1) {
                            log.fatal(e);
                        }
                    }
                }
            };
            t.setPriority(Thread.MIN_PRIORITY);
            return t;
        }
    }
}
