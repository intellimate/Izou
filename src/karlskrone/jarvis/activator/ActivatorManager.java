package karlskrone.jarvis.activator;

import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * The ActivatorManager holds all the Activator-instances and runs them parallel in Threads.
 */
public class ActivatorManager {
    ExecutorService executor = Executors.newCachedThreadPool(new CustomThreadFactory());
    public ActivatorManager() {

    }

    public java.util.concurrent.Future<?> executeActivator(Activator activator) {
        return executor.submit(activator);
    }


    private class CustomThreadFactory implements ThreadFactory {
        @SuppressWarnings("NullableProblems")
        @Override
        public Thread newThread(Runnable r) {
            return new Thread() {
                public void run() {
                    try {
                        r.run();
                    } finally {
                        //TODO: Is this working? What is the definition of Threads not used? Does Sleep count
                        /*
                        try {
                            Field target = Thread.class.getDeclaredField("target");
                            target.setAccessible(true);
                            Activator activator = (Activator) target.get(this);

                        } catch (NoSuchFieldException | IllegalAccessException | ClassCastException e ) {
                            Errorhandling is not implemented jet
                            e.printStackTrace();
                        }
                         */
                    }
                }
            };
        }
    }
}
