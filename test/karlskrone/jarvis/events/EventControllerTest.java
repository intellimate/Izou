package karlskrone.jarvis.events;

import org.junit.Test;

public class EventControllerTest {
    static EventManagerTestSetup eventManagerTestSetup = new EventManagerTestSetup();

    @Test
    public void testControlEventDispatcher () {
        boolean[] isWorking = {false, true};
        EventController eventController = eventID -> {
            isWorking[1] = false;
            return false;
        };
        eventManagerTestSetup.getManager().addEventController(eventController);
        try {
            eventManagerTestSetup.testListenerfalse(isWorking, "1");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}