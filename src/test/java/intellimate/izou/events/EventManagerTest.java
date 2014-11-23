package intellimate.izou.events;

import intellimate.izou.output.OutputManager;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventManagerTest {
    static EventManager manager;
    Thread thread;
    private static final class Lock { }
    private final Object lock = new Lock();
    private OutputManager outputManager;

    public EventManagerTest() {
        outputManager = new OutputManager();
        manager = new EventManager(outputManager);
        thread = new Thread(manager);
        thread.start();
    }

    @Test
    public void testRegisterActivatorEvent() throws Exception {
        EventManager.EventCaller caller = manager.registerCaller("1");
        final boolean[] isWorking = {false};
        manager.registerEventListener("1", id -> {
            isWorking[0] = true;
            return null;
        });
        manager.registerEventListener("1", id -> {
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
        EventManager.EventCaller caller = manager.registerCaller("2");
        final boolean[] isWorking = {false};
        manager.unregisterCaller("2", caller);
        manager.registerEventListener("2", id -> {
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
        EventManager.EventCaller caller = manager.registerCaller("3");
        final boolean[] isWorking = {false};
        manager.registerEventListener("3", id -> {
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
        EventManager.EventCaller caller = manager.registerCaller("4");
        final boolean[] isWorking = {false};
        EventListener listener1;
        manager.registerEventListener("4", listener1 = id -> {
            isWorking[0] = true;
            return null;
        });
        manager.unregisterEventListener("4", listener1);
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
    public void testAddEventController() throws Exception {
        boolean[] isWorking = {false, true};
        EventManager.EventCaller caller = manager.registerCaller("5");
        manager.registerEventListener("5", id -> {
            isWorking[0] = true;
            return null;
        });
        EventsController eventsController = eventID -> {
            isWorking[1] = false;
            return false;
        };
        manager.addEventsController(eventsController);
        caller.fire();

        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        manager.removeEventsController(eventsController);
        assertFalse(isWorking[0] && isWorking[1]);
    }

    @Test
    public void testRemoveEventController() throws Exception {
        boolean[] isWorking = {false, true};
        EventManager.EventCaller caller = manager.registerCaller("5");
        manager.registerEventListener("5", id -> {
            isWorking[0] = true;
            return null;
        });
        EventsController eventsController = eventID -> {
            isWorking[1] = false;
            return false;
        };
        manager.addEventsController(eventsController);
        manager.removeEventsController(eventsController);
        caller.fire();

        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        assertTrue(isWorking[0] && isWorking[1]);
    }

    @Test
    public void testCommonEvents() throws Exception {
        assertEquals(EventManager.FULL_WELCOME_EVENT, "intellimate.izou.events.EventManager.FullWelcomeEvent");
    }
}