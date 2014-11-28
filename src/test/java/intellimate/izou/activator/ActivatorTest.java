package intellimate.izou.activator;

import intellimate.izou.events.EventManagerTestSetup;
import org.junit.Test;

import static org.junit.Assert.*;

public class ActivatorTest {
    static EventManagerTestSetup eventManagerTestSetup;
    static Activator activator;
    static ActivatorManager activatorManager;

    public ActivatorTest() {
        eventManagerTestSetup = new EventManagerTestSetup();
        activator = new Activator() {
            @Override
            public void activatorStarts() throws InterruptedException {}

            @Override
            public boolean terminated(Exception e) {return false;}
        };
        activatorManager = new ActivatorManager(eventManagerTestSetup.getManager());
        activatorManager.addActivator(activator);
    }

    @Test
    public void testRegisterEvent() throws Exception {
        activator.registerEvent("1");
        final boolean[] isWorking = {false};
        eventManagerTestSetup.getManager().registerEventListener("1", event -> {
            isWorking[0] = true;
            return null;
        });
        activator.fireEvent("1");
        eventManagerTestSetup.waitForMultith();
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
    public void testUnregisterAllEvent() throws Exception {
        activator.registerEvent("5");
        activator.registerEvent("6");
        boolean isWorking = false;
        activator.unregisterAllEvents();
        try {
            activator.fireEvent("5");
        }
        catch (IllegalArgumentException e)
        {
            isWorking = true;
        }
        try {
            activator.fireEvent("6");
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
        eventManagerTestSetup.getManager().registerEventListener("4", id -> {
            isWorking[0] = true;
            return null;
        });
        activator.fireEvent("4");
        eventManagerTestSetup.waitForMultith();
        assertTrue(isWorking[0]);
    }
}