package karlskrone.jarvis.events;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventManagerTest {
    static EventManager manager;
    Thread thread;
    private static final class Lock { }
    private final Object lock = new Lock();

    public EventManagerTest() {
        manager = new EventManager();
        thread = new Thread(manager);
        thread.start();
    }

    @Test
    public void testRegisterActivatorEvent() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorCaller("1");
        final boolean[] isWorking = {false};
        manager.addActivatorEventListener("1", id -> {
            isWorking[0] = true;
            return null;
        });
        manager.addActivatorEventListener("1", id -> {
            isWorking[0] = true;
            return null;
        });
        caller.fire();

        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        assertTrue(isWorking[0]);
    }

    @Test
    public void testUnregisterActivatorEvent() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorCaller("2");
        final boolean[] isWorking = {false};
        manager.unregisterActivatorCaller("2", caller);
        manager.addActivatorEventListener("2", id -> {
            isWorking[0] = true;
            return null;
        });
        caller.fire();


        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        assertFalse(isWorking[0]);
    }

    @Test
    public void testAddActivatorEventListener() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorCaller("3");
        final boolean[] isWorking = {false};
        manager.addActivatorEventListener("3", id -> {
            isWorking[0] = true;
            return null;
        });
        caller.fire();

        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        assertTrue(isWorking[0]);
    }

    @Test
    public void testDeleteActivatorEventListener() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorCaller("4");
        final boolean[] isWorking = {false};
        EventManager.ActivatorEventListener listener1;
        manager.addActivatorEventListener("4", listener1 = id -> {
            isWorking[0] = true;
            return null;
        });
        manager.deleteActivatorEventListener("4", listener1);
        caller.fire();


        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        assertFalse(isWorking[0]);
    }

    @Test
    public void testCommonEvents() throws Exception {
        assertEquals(EventManager.fullWelcomeEvent, "karlskrone.jarvis.events.EventManager.FullWelcomeEvent");
    }
}