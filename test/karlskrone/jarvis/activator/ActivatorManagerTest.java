package karlskrone.jarvis.activator;

import karlskrone.jarvis.events.EventManager;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class ActivatorManagerTest {
    static EventManager manager;
    static ActivatorManager activatorManager;
    private static final class Lock { }
    private final Object lock = new Lock();

    @Before
    public void setUp() throws Exception {
        manager = new EventManager();
        activatorManager = new ActivatorManager();
    }

    @Test
    public void testAddActivator() throws Exception {
        final boolean[] isWorking = {false};

        Activator activator = new Activator(manager) {
            @Override
            public void activatorStarts() throws InterruptedException {
                registerEvent("1");
                try {
                    fireEvent("1");
                } catch (EventManager.MultipleEventsException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void terminated(Exception e) {}
        };

        manager.addActivatorEventListener("1", id -> isWorking[0] = true);

        Future<?> future = activatorManager.addActivator(activator);

        synchronized (lock) {
            while (!future.isDone())
            {
                lock.wait(10);
            }
        }
        assertTrue(isWorking[0]);
    }
}