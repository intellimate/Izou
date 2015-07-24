package old.org.intellimate.izou.activator;

import old.org.intellimate.izou.testHelper.IzouTest;
import org.intellimate.izou.activator.ActivatorModel;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.events.MultipleEventsException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@Ignore
public class ActivatorModelManagerTest extends IzouTest {
//    private static final class Lock { }
//    private final Object lock = new Lock();
//
//    public ActivatorModelManagerTest() {
//        super(false, ActivatorModelManagerTest.class.getCanonicalName());
//    }
//
//    @Test
//    public void testAddActivator() throws Exception {
//        final boolean[] isWorking = {false};
//        Optional<EventModel> event = getEvent(id + 1);
//        ActivatorModel activatorModel = new ActivatorModel(getContext()) {
//            /**
//             * An ID must always be unique.
//             * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
//             * If you have to implement this interface multiple times, just concatenate unique Strings to
//             * .class.getCanonicalName()
//             *
//             * @return A String containing an ID
//             */
//            @Override
//            public String getID() {
//                return "TEST";
//            }
//
//            @Override
//            public void activatorStarts() throws InterruptedException {
//                try {
//                    fireEvent(event.get());
//                } catch (MultipleEventsException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public boolean terminated(Exception e) {
//                fail();
//                return false;
//            }
//        };
//        LinkedList<String> listenerList = new LinkedList<String>();
//        listenerList.add(event.get().getType());
//        main.getEventDistributor().registerEventListener(listenerList, event1 -> isWorking[0] = true);
//
//        Future<?> future = main.getActivatorManager().addActivator(activatorModel);
//
//
//        synchronized (lock) {
//            while (!future.isDone())
//            {
//                lock.wait(10);
//            }
//            for (int i = 0; i <= 10; i++) {
//                lock.wait(10);
//            }
//        }
//        assertTrue(isWorking[0]);
//    }
}