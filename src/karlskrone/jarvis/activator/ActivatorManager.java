package karlskrone.jarvis.activator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by Leander on 24.09.2014.
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
                        //TODO: Handling when Thread gets terminated
                    }
                }
            };
        }
    }
}
