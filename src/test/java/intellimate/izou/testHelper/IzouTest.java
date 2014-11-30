package intellimate.izou.testHelper;

import intellimate.izou.events.Event;
import intellimate.izou.events.LocalEventManager;
import intellimate.izou.main.Main;
import intellimate.izou.system.Identifiable;
import intellimate.izou.system.Identification;
import intellimate.izou.system.IdentificationManager;

import java.util.Optional;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Helper class for Unit-testing
 */
public class IzouTest implements Identifiable{
    private static Main staticMain;
    public Main main;
    public final String id;
    private IdentificationManager identificationManager;
    private static int identificationNumber;
    private static int eventNumber;
    private static final class Lock { }
    private final Object lock = new Lock();

    /**
     * creates a new instance of IzouTest
     * @param isolated whether the fields (manager etc.) should be Isolated from all the other instances.
     */
    public IzouTest(boolean isolated, String id) {
        if(isolated) {
            main = new Main(null, true);
        } else {
            if(staticMain == null) {
                staticMain = new Main(null, true);
            }
            this.main = staticMain;
        }
        this.id = id;
        identificationManager = IdentificationManager.getInstance();
        identificationManager.registerIdentification(this);
    }

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
        return id;
    }

    public Optional<Event> getEvent(String type) {
        Optional<Identification> id = identificationManager.getIdentification(this);
        if(!id.isPresent()) return Optional.empty();
        return Event.createEvent(type, id.get());
    }

    /**
     * checks if is working is set everywhere to false, [0] should be reserved for the test here
     */
    public void testListenerTrue(final boolean[] isWorking, Event event) throws InterruptedException {
        main.getEventDistributor().registerEventListener(event, id -> {
            isWorking[0] = true;
        });
        try {
            Identification id = IdentificationManager.getInstance().getIdentification(this).get();
            main.getLocalEventManager().registerCaller(id).get().fire(event);
        } catch (LocalEventManager.MultipleEventsException e) {
            fail();
        }

        synchronized (lock) {
            lock.wait(10);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(2);
            }
        }
        boolean result = false;
        for (boolean temp : isWorking)
        {
            if(temp) result = true;
        }
        assertTrue(result);
    }

    /**
     * checks if is working is set everywhere to false, [0] should be reserved for the test here
     */
    public void testListenerFalse(final boolean[] isWorking, Event eventId) throws InterruptedException {
        main.getEventDistributor().registerEventListener(eventId, id -> isWorking[0] = true);
        try {
            Identification id = IdentificationManager.getInstance().getIdentification(this).get();
            main.getLocalEventManager().registerCaller(id).get().fire(eventId);
        } catch (LocalEventManager.MultipleEventsException e) {
            fail();
        }

        synchronized (lock) {
            lock.wait(10);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(2);
            }
        }
        boolean result = false;
        for (boolean temp : isWorking)
        {
            if(temp) result = true;
        }
        assertFalse(result);
    }

    /**
     * fires the event
     * @param event the event to fire
     */
    public void testFireEvent(Event event) {
        try {
            Identification id = IdentificationManager.getInstance().getIdentification(this).get();
            main.getLocalEventManager().registerCaller(id).get().fire(event);
        } catch (LocalEventManager.MultipleEventsException e) {
            fail();
        }
    }

    /**
     * waits for multitasking
     * @throws InterruptedException
     */
    public void waitForMultith() throws InterruptedException {
        synchronized (lock) {

            synchronized (lock) {
                lock.wait(10);
                while(!main.getLocalEventManager().getEvents().isEmpty())
                {
                    lock.wait(2);
                }
            }
        }
    }

    public Optional<Identification> getNextIdentification() {
        Identifiable identifiable = () -> id + identificationNumber;
        identificationManager.registerIdentification(identifiable);
        identificationNumber++;
        return identificationManager.getIdentification(identifiable);
    }

    public Optional<Event> getNextEvent() {
        eventNumber++;
        return getEvent(id + eventNumber);
    }
}
