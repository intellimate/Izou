package intellimate.izou.contentgenerator;

import intellimate.izou.events.EventManagerTestSetup;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class ContentGeneratorTest {
    public static ContentGeneratorOld<Boolean> contentGenerator;
    public static EventManagerTestSetup eventManagerTestSetup;
    public static ContentGeneratorManager contentGeneratorManager;
    private static final class Lock { }
    private final Object lock = new Lock();

    public ContentGeneratorTest() {
        eventManagerTestSetup = new EventManagerTestSetup();
        contentGeneratorManager = new ContentGeneratorManager(eventManagerTestSetup.getManager());
        contentGenerator = new ContentGeneratorOld<Boolean>("1") {
            @Override
            public ContentData<Boolean> generate(String eventID) throws Exception {
               ContentData<Boolean> cd = new ContentData<>("1");
               cd.setData(true);
               return cd;
            }

            @Override
            public void handleError(Exception e) {

            }
        };
        contentGenerator.registerAllNeededDependencies(contentGeneratorManager, eventManagerTestSetup.getManager());
    }

    @Test
    public void testSetContentGeneratorManager() throws Exception {
        final boolean[] isWorking = {false};
        ContentGeneratorOld cg = new ContentGeneratorOld("2") {
            @Override
            public ContentData generate(String eventID) throws Exception {
                isWorking[0] = true;
                return null;
            }

            @Override
            public void handleError(Exception e) {

            }
        };
        cg.registerAllNeededDependencies(contentGeneratorManager, eventManagerTestSetup.getManager());
        Future future = cg.eventFired("1");
        synchronized (lock) {
            while (!future.isDone())
            {
                lock.wait(10);
            }
        }
        assertTrue(isWorking[0]);
    }

    @Test
    public void testActivatorEventFired() throws Exception {
        Future<ContentData> future = contentGenerator.eventFired("2");
        assertTrue((Boolean)future.get().getData());
    }

    @Test
    public void testCall() throws Exception {
        final boolean[] isWorking = {false, false};
        ContentGeneratorOld cg = new ContentGeneratorOld("3") {
            @Override
            public ContentData generate(String eventID) throws Exception {
                if(eventID.equals("3"))
                {
                    isWorking[0] = true;
                }
                throw new Exception("4");
            }

            @Override
            public void handleError(Exception e) {
                isWorking[1] = true;
            }
        };
        cg.registerAllNeededDependencies(contentGeneratorManager, eventManagerTestSetup.getManager());
        Future future = cg.eventFired("3");
        synchronized (lock) {
            while (!future.isDone())
            {
                lock.wait(10);
            }
        }
        assertTrue(isWorking[0]&&isWorking[1]);
    }

    @Test
    public void testRegisterEvent() throws Exception {
        final boolean[] isWorking = {false};
        ContentGeneratorOld cg = new ContentGeneratorOld("4") {
            @Override
            public ContentData generate(String eventID) throws Exception {
                if(eventID.equals("5")) isWorking[0] = true;
                return null;
            }

            @Override
            public void handleError(Exception e) {}
        };
        cg.registerAllNeededDependencies(contentGeneratorManager, eventManagerTestSetup.getManager());
        cg.registerEvent("5");
        eventManagerTestSetup.testFireEvent("5");
        eventManagerTestSetup.waitForMultith();
        assertTrue(isWorking[0]);
    }

    @Test
    public void testUnregisterEvent() throws Exception {
        final boolean[] isWorking = {false};
        ContentGeneratorOld cg = new ContentGeneratorOld("5") {
            @Override
            public ContentData generate(String eventID) throws Exception {
                isWorking[0] = true;
                return null;
            }

            @Override
            public void handleError(Exception e) {}
        };
        cg.registerAllNeededDependencies(contentGeneratorManager, eventManagerTestSetup.getManager());
        cg.registerEvent("6");
        cg.unregisterEvent("6");
        eventManagerTestSetup.testFireEvent("6");
        eventManagerTestSetup.waitForMultith();
        assertFalse(isWorking[0]);
    }

    @Test
    public void testUnregisterAllEvents() throws Exception {
        final boolean[] isWorking = {false};
        ContentGeneratorOld cg = new ContentGeneratorOld("6") {
            @Override
            public ContentData generate(String eventID) throws Exception {
                isWorking[0] = true;
                return null;
            }

            @Override
            public void handleError(Exception e) {}
        };
        cg.registerAllNeededDependencies(contentGeneratorManager, eventManagerTestSetup.getManager());
        cg.registerEvent("7");
        cg.unregisterAllEvents();
        eventManagerTestSetup.testFireEvent("7");
        eventManagerTestSetup.waitForMultith();
        assertFalse(isWorking[0]);
    }

    @Test
    public void testGetRegisteredEvents() throws Exception {
        contentGenerator.unregisterAllEvents();
        contentGenerator.registerEvent("6");
        List list= contentGenerator.getRegisteredEvents();
        assertTrue( (list.size() == 1) && list.contains("6") );
    }
}