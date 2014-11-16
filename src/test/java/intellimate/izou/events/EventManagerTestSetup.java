package intellimate.izou.events;

import intellimate.izou.output.OutputManager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("SameParameterValue")
public class EventManagerTestSetup {

    public EventManager getManager() {
        return manager;
    }

    private static EventManager manager;
    Thread thread;
    private OutputManager outputManager;
    private static final class Lock { }

    private final Object lock = new Lock();

    public EventManagerTestSetup() {
        outputManager = new OutputManager();
        manager = new EventManager(outputManager);
        thread = new Thread(manager);
        thread.start();
    }
    /**
     * checks if is working is set everywhere to false, [0] should be reserved for the test here
     */
    public void testListenerTrue(final boolean[] isWorking, String eventId) throws InterruptedException {
        manager.addActivatorEventListener(eventId, id -> {
            isWorking[0] = true;
            return null;
        });
        try {
            manager.registerActivatorCaller(eventId).fire();
        } catch (EventManager.MultipleEventsException e) {
            fail();
        }
        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        boolean result = false;
        for (boolean temp : isWorking)
        {
            if(temp) result = true;
        }
        assertTrue(result);
    }

    /**
     * checks if is working is set everywhere to false, [0] should be reserved for the test here
     */
    public void testListenerFalse(final boolean[] isWorking, String eventId) throws InterruptedException {
        manager.addActivatorEventListener(eventId, id -> {
            isWorking[0] = true;
            return null;
        });
        try {
            manager.registerActivatorCaller(eventId).fire();
        } catch (EventManager.MultipleEventsException e) {
            fail();
        }

        synchronized (lock) {
            while(!manager.getEvents().isEmpty() || !(thread.getState() == Thread.State.WAITING))
            {
                lock.wait(2);
            }
        }
        boolean result = false;
        for (boolean temp : isWorking)
        {
            if(temp) result = true;
        }
        assertFalse(result);
    }

    public void testFireEvent(String id) {
        try {
            manager.registerActivatorCaller(id).fire();
        } catch (EventManager.MultipleEventsException e) {
            fail();
        }
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
