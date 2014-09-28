package karlskrone.jarvis.events;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by Leander on 28.09.2014.
 */
public class EventManagerTestSetup {

    public EventManager getManager() {
        return manager;
    }

    private static EventManager manager;
    Thread thread;
    private static final class Lock { }

    private final Object lock = new Lock();

    public EventManagerTestSetup() {
        manager = new EventManager();
        thread = new Thread(manager);
        thread.start();
    }

    public void testListenerTrue(final boolean[] isWorking, String eventId) throws InterruptedException {
        manager.addActivatorEventListener(eventId, id -> {
            isWorking[0] = true;
            return null;
        });

        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        assertTrue(isWorking[0]);
    }

    public void testListenerfalse(final boolean[] isWorking, String eventId) throws InterruptedException {
        manager.addActivatorEventListener(eventId, id -> {
            isWorking[0] = true;
            return null;
        });

        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        assertFalse(isWorking[0]);
    }

    public void waitForMultith() throws InterruptedException {
        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
    }
}
