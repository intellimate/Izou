package old.org.intellimate.izou.events;

//import intellimate.izouSDK.events.EventImpl;
import old.org.intellimate.izou.testHelper.IzouTest;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.EventsControllerModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.junit.Test;

public class EventsControllerTest extends IzouTest {

//    public EventsControllerTest() {
//        super(true, EventsControllerTest.class.getCanonicalName());
//    }
//
//    @Test
//    public void testControlEventDispatcher () {
//        boolean[] isWorking = {false, true};
//        EventsControllerModel eventsController = new EventsControllerModel() {
//            /**
//             * this method gets called when the task submitted to the ThreadPool crashes
//             *
//             * @param e the exception catched
//             */
//            @Override
//            public void exceptionThrown(Exception e) {
//                System.out.println(getID() + " crashed");
//            }
//
//            @Override
//            public boolean controlEventDispatcher(EventModel event) {
//                isWorking[1] = false;
//                return false;
//            }
//            @Override
//            public String getID() {
//                return EventsControllerTest.class.getCanonicalName()+1;
//            }
//        };
//        main.getEventDistributor().registerEventsController(eventsController);
//        try {
//            IdentificationManager.getInstance().registerIdentification(eventsController);
//            Identification id = IdentificationManager.getInstance().getIdentification(eventsController).get();
//            EventModel event = EventImpl.createEvent(super.id + 1, id).get();
//            testListenerFalse(isWorking, event);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
}