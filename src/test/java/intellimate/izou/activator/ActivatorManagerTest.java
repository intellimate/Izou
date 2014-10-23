package intellimate.izou.activator;

import intellimate.izou.events.EventManager;
import intellimate.izou.events.EventManagerTestSetup;
import org.junit.Test;

import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class ActivatorManagerTest {
    EventManagerTestSetup eventMangrSetup;
    static ActivatorManager activatorManager;
    private static final class Lock { }
    private final Object lock = new Lock();

    public ActivatorManagerTest() {
        eventMangrSetup = new EventManagerTestSetup();
        Thread thread = new Thread(eventMangrSetup.getManager());
        thread.start();
        activatorManager = new ActivatorManager(eventMangrSetup.getManager());
    }

    @Test
    public void testAddActivator() throws Exception {
        final boolean[] isWorking = {false};

        Activator activator = new Activator() {
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
            public boolean terminated(Exception e) {return false;}
        };

        eventMangrSetup.getManager().addActivatorEventListener("1", id -> {
            isWorking[0] = true;
            return null;
        });

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