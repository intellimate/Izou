package intellimate.izou.events;

import intellimate.izou.system.Identification;
import intellimate.izou.system.IdentificationManager;
import intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

public class EventsControllerTest extends IzouTest{

    public EventsControllerTest() {
        super(true, EventsControllerTest.class.getCanonicalName());
    }

    @Test
    public void testControlEventDispatcher () {
        boolean[] isWorking = {false, true};
        EventsController eventsController = new EventsController() {
            /**
             * this method gets called when the task submitted to the ThreadPool crashes
             *
             * @param e the exception catched
             */
            @Override
            public void exceptionThrown(Exception e) {
                System.out.println(getID() + " crashed");
            }

            @Override
            public boolean controlEventDispatcher(Event event) {
                isWorking[1] = false;
                return false;
            }
            @Override
            public String getID() {
                return EventsControllerTest.class.getCanonicalName()+1;
            }
        };
        main.getEventDistributor().registerEventsController(eventsController);
        try {
            IdentificationManager.getInstance().registerIdentification(eventsController);
            Identification id = IdentificationManager.getInstance().getIdentification(eventsController).get();
            Event event = Event.createEvent(super.id+1, id).get();
            testListenerFalse(isWorking, event);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}