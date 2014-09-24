package karlskrone.jarvis.events;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class EventManagerTest {
    static EventManager manager;

    @Before
    public void setUp() throws Exception {
        manager = new EventManager();
    }

    @Test
    public void testRegisterActivatorEvent() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorEvent("1");
        final boolean[] isWorking = {false};
        manager.addActivatorEventListener("1", id -> isWorking[0] = true);
        //the same, just without lambda
        manager.addActivatorEventListener("1", new EventManager.ActivatorEventListener() {
            @Override
            public void activatorEventFired(String id) {
                isWorking[0] = true;
            }
        });
        caller.fire();
        assertTrue(isWorking[0]);
    }

    @Test
    public void testUnregisterActivatorEvent() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorEvent("2");
        final boolean[] isWorking = {false};
        manager.unregisterActivatorEvent("2", caller);
        manager.addActivatorEventListener("2", id -> isWorking[0] = true);
        caller.fire();
        assertFalse(isWorking[0]);
    }

    @Test
    public void testAddActivatorEventListener() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorEvent("3");
        final boolean[] isWorking = {false};
        manager.addActivatorEventListener("3", id -> isWorking[0] = true);
        caller.fire();
        assertTrue(isWorking[0]);
    }

    @Test
    public void testRemoveActivatorEventListener() throws Exception {
        EventManager.ActivatorEventCaller caller = manager.registerActivatorEvent("4");
        final boolean[] isWorking = {false};
        EventManager.ActivatorEventListener listener1;
        manager.addActivatorEventListener("4", listener1 = new EventManager.ActivatorEventListener() {
            @Override
            public void activatorEventFired(String id) {
                isWorking[0] = true;
            }
        });
        manager.deleteActivatorEventListener("4", listener1);
        caller.fire();
        assertFalse(isWorking[0]);
    }

    @Test
    public void testCommonEvents() throws Exception {
        assertEquals(EventManager.CommonEvents.fullWelcomeEvent, "karlskrone.jarvis.events.EventManager.CommonEvents.FullWelcomeEvent");
    }
}