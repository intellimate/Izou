package org.intellimate.izou.events;

import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.internal.events.LocalEventManager;
import org.intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class LocalEventManagerTest extends IzouTest {
    private static final class Lock { }
    private final Object lock = new Lock();

    public LocalEventManagerTest() {
        super(false, LocalEventManagerTest.class.getCanonicalName());
        IdentificationManager.getInstance().registerIdentification(this);
    }

    @Test
    public void testRegisterActivatorEvent() throws Exception {
        EventModel event = getNextEvent().get();
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
        EventModel event = getNextEvent().get();
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
        EventModel event = getNextEvent().get();
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
        EventModel event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        final boolean[] isWorking = {false};
        EventListenerModel listener = s -> isWorking[0] = true;
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
        EventModel event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        boolean[] isWorking = {false, true};
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        EventsControllerModel eventsController = new EventsControllerModel() {
            /**
             * this method gets called when the task submitted to the ThreadPool crashes
             *
             * @param e the exception catched
             */
            @Override
            public void exceptionThrown(Exception e) {
                fail();
            }

            @Override
            public boolean controlEventDispatcher(EventModel event) {
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
        EventModel event = getNextEvent().get();
        Identification id = getNextIdentification().get();
        boolean[] isWorking = {false, true};
        LocalEventManager.EventCaller caller = main.getLocalEventManager().registerCaller(id).get();
        main.getEventDistributor().registerEventListener(event, s -> isWorking[0] = true);
        EventsControllerModel eventsController = new EventsControllerModel() {
            /**
             * this method gets called when the task submitted to the ThreadPool crashes
             *
             * @param e the exception catched
             */
            @Override
            public void exceptionThrown(Exception e) {
                fail();
            }

            @Override
            public boolean controlEventDispatcher(EventModel event) {
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