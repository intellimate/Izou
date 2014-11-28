package intellimate.izou.activator;

import intellimate.izou.events.Event;
import intellimate.izou.events.LocalEventManager;
import intellimate.izou.events.EventManagerTestSetup;
import intellimate.izou.system.Identification;
import intellimate.izou.system.IdentificationManager;
import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class ActivatorManagerTest {
    EventManagerTestSetup eventMangrSetup;
    static ActivatorManager activatorManager;
    private static final class Lock { }
    private final Object lock = new Lock();

    public ActivatorManagerTest() {
        eventMangrSetup = new EventManagerTestSetup();
        Thread thread = new Thread(eventMangrSetup.getManager());
        thread.start();
        activatorManager = new ActivatorManager(eventMangrSetup.getManager());
    }

    @Test
    public void testAddActivator() throws Exception {
        final boolean[] isWorking = {false};
        Optional<Event> event = Optional.empty();
        Activator activator = new Activator() {
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
                    Identification identification= IdentificationManager.getInstance().getIdentification(this).get();
                    Optional<Event> event = Event.createEvent("1", identification);
                    fireEvent(event.get());
                } catch (LocalEventManager.MultipleEventsException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean terminated(Exception e) {return false;}
        };

        eventMangrSetup.getManager().registerEventListener(event.get(), id -> isWorking[0] = true);

        Future<?> future = activatorManager.addActivator(activator);

        synchronized (lock) {
            while (!future.isDone())
            {
                lock.wait(10);
            }
        }
        assertTrue(isWorking[0]);
    }
}