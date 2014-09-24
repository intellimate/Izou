package karlskrone.jarvis.activator;

import karlskrone.jarvis.events.EventManager;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActivatorTest {
    static EventManager manager;
    static Activator activator;
    @Before
    public void setUp() throws Exception {
        manager = new EventManager();
        activator = new Activator(manager) {
            @Override
            public void activatorStarts() throws InterruptedException {}
        };
    }

    @Test
    public void testRegisterEvent() throws Exception {
        activator.registerEvent("1");
        final boolean[] isWorking = {false};
        manager.addActivatorEventListener("1", id -> isWorking[0] = true);
        activator.fireEvent("1");
        assertTrue(isWorking[0]);
    }

    @Test
    public void testUnregisterEvent() throws Exception {
        activator.registerEvent("2");
       boolean isWorking = false;
        activator.unregisterEvent("2");
        try {
            activator.fireEvent("2");
        }
        catch (IllegalArgumentException e)
        {
            isWorking = true;
        }
        assertTrue(isWorking);
    }

    @Test
    public void testGetAllRegisteredEvents() throws Exception {
        //empty registered events
        String[] events = activator.getAllRegisteredEvents();
        for ( String event : events)
        {
            activator.unregisterEvent(event);
        }
        activator.registerEvent("3");
        events = activator.getAllRegisteredEvents();
        assertTrue(events.length == 1 && events[0].equals("3"));
    }

    @Test
    public void testFireEvent() throws Exception {
        activator.registerEvent("4");
        final boolean[] isWorking = {false};
        manager.addActivatorEventListener("4", id -> isWorking[0] = true);
        activator.fireEvent("4");
        assertTrue(isWorking[0]);
    }
}