package intellimate.izou.events;

import intellimate.izou.system.Identification;
import intellimate.izou.system.IdentificationManager;
import intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LocalEventManagerTest extends IzouTest{
    private static final class Lock { }
    private final Object lock = new Lock();

    public LocalEventManagerTest() {
        super(false, LocalEventManagerTest.class.getCanonicalName());
        IdentificationManager.getInstance().registerIdentification(this);
    }

    @Test
    public void testRegisterActivatorEvent() throws Exception {
        Event event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        final boolean[] isWorking = {false};
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        caller.fire(event);

        synchronized (lock) {
            lock.wait(30);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(10);
            }
            while(!main.getEventDistributor().getEvents().isEmpty())
            {
                lock.wait(10);
            }
        }
        assertTrue(isWorking[0]);
    }

    @Test
    public void testUnregisterEvent() throws Exception {
        Event event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        final boolean[] isWorking = {false};
        main.getLocalEventManager().unregisterCaller(id);
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        caller.fire(event);

        synchronized (lock) {
            lock.wait(30);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(2);
            }
            while(!main.getEventDistributor().getEvents().isEmpty())
            {
                lock.wait(10);
            }
        }
        assertFalse(isWorking[0]);
    }

    @Test
    public void testAddActivatorEventListener() throws Exception {
        Event event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        final boolean[] isWorking = {false};
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        caller.fire(event);

        synchronized (lock) {
            lock.wait(30);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(2);
            }
            while(!main.getEventDistributor().getEvents().isEmpty())
            {
                lock.wait(10);
            }
        }
        assertTrue(isWorking[0]);
    }

    @Test
    public void testDeleteActivatorEventListener() throws Exception {
        Event event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        final boolean[] isWorking = {false};
        EventListener listener = s -> isWorking[0] = true;
        main.getEventDistributor().registerEventListener(event, listener);
        main.getEventDistributor().unregisterEventListener(event, listener);
        caller.fire(event);

        synchronized (lock) {
            lock.wait(30);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(2);
            }
            while(!main.getEventDistributor().getEvents().isEmpty())
            {
                lock.wait(10);
            }
        }
        assertFalse(isWorking[0]);
    }

    @Test
    public void testAddEventController() throws Exception {
        Event event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        boolean[] isWorking = {false, true};
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        EventsController eventsController = new EventsController() {
            @Override
            public boolean controlEventDispatcher(Event event) {
                isWorking[1] = false;
                return false;
            }

            @Override
            public String getID() {
                return "eventsController";
            }
        };
        main.getEventDistributor().registerEventsController(eventsController);
        caller.fire(event);

        synchronized (lock) {
            lock.wait(30);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(2);
            }
            while(!main.getEventDistributor().getEvents().isEmpty())
            {
                lock.wait(10);
            }
        }

        main.getEventDistributor().unregisterEventsController(eventsController);
        assertFalse(isWorking[0] && isWorking[1]);
    }

    @Test
    public void testRemoveEventController() throws Exception {
        Event event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        boolean[] isWorking = {false, true};
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        EventsController eventsController = new EventsController() {
            @Override
            public boolean controlEventDispatcher(Event event) {
                isWorking[1] = false;
                return false;
            }

            @Override
            public String getID() {
                return "eventsController2";
            }
        };
        main.getEventDistributor().registerEventsController(eventsController);
        main.getEventDistributor().unregisterEventsController(eventsController);
        caller.fire(event);

        synchronized (lock) {
            lock.wait(30);
            while(!main.getLocalEventManager().getEvents().isEmpty())
            {
                lock.wait(2);
            }
            while(!main.getEventDistributor().getEvents().isEmpty())
            {
                lock.wait(10);
            }
        }
        assertFalse(!isWorking[0] && isWorking[1]);
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
        return LocalEventManagerTest.class.getCanonicalName();
    }
}