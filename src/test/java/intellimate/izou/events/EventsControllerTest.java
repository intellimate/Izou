package intellimate.izou.events;

import org.junit.Test;

public class EventsControllerTest {
    static EventManagerTestSetup eventManagerTestSetup = new EventManagerTestSetup();

    @Test
    public void testControlEventDispatcher () {
        boolean[] isWorking = {false, true};
        EventsController eventsController = eventID -> {
            isWorking[1] = false;
            return false;
        };
        eventManagerTestSetup.getManager().addEventController(eventsController);
        try {
            eventManagerTestSetup.testListenerFalse(isWorking, "1");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}