package intellimate.izou.threadpool;

import intellimate.izou.IzouModule;
import intellimate.izou.main.Main;

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
                                ExceptionCallback exceptionCallback = (ExceptionCallback) target.get(this);
                                if (e instanceof Exception) {
                                    exceptionCallback.exceptionThrown((Exception) e);
                                } else {
                                    exceptionCallback.exceptionThrown(new RuntimeException(e));
                                }
                            } catch (IllegalArgumentException | IllegalAccessException e1) {
                                log.fatal("unable to provide callback for:" + target.toString(), e);
                            }
                        } catch (NoSuchFieldException ecp ) {
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
