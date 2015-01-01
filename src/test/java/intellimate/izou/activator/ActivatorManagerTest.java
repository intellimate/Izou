package intellimate.izou.activator;

import intellimate.izou.events.Event;
import intellimate.izou.events.MultipleEventsException;
import intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import java.util.LinkedList;
import java.util.Optional;
import java.util.concurrent.Future;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ActivatorManagerTest extends IzouTest{
    private static final class Lock { }
    private final Object lock = new Lock();

    public ActivatorManagerTest() {
        super(false, ActivatorManagerTest.class.getCanonicalName());
    }

    @Test
    public void testAddActivator() throws Exception {
        final boolean[] isWorking = {false};
        Optional<Event> event = getEvent(id + 1);
        Activator activator = new Activator(getContext()) {
            /**
             * An ID must always be unique.
             * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
             * If you have to implement this interface multiple times, just concatenate unique Strings to
             * .class.getCanonicalName()
             *
             * @return A String containing an ID
             */
            @Override
            public String getID() {
                return "TEST";
            }

            @Override
            public void activatorStarts() throws InterruptedException {
                try {
                    fireEvent(event.get());
                } catch (MultipleEventsException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean terminated(Exception e) {
                fail();
                return false;
            }
        };
        LinkedList<String> listenerList = new LinkedList<String>();
        listenerList.add(event.get().getType());
        main.getEventDistributor().registerEventListener(listenerList, event1 -> isWorking[0] = true);

        Future<?> future = main.getActivatorManager().addActivator(activator);


        synchronized (lock) {
            while (!future.isDone())
            {
                lock.wait(10);
            }
            for (int i = 0; i <= 10; i++) {
                lock.wait(10);
            }
        }
        assertTrue(isWorking[0]);
    }
}