package intellimate.izou.activator;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.EventManager;
import intellimate.izou.events.EventManagerTestSetup;
import intellimate.izou.fullplugintesting.TestAddOn;
import intellimate.izou.main.Main;
import intellimate.izou.system.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);
        Context context = new Context(testAddOn, main, "1", "debug");

        final boolean[] isWorking = {false};

        Activator activator = new Activator(context) {
            @Override
            public void activatorStarts() throws InterruptedException {
                registerEvent("1");
                try {
                    fireEvent("1");
                } catch (EventManager.MultipleEventsException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public boolean terminated(Exception e) {return false;}
        };

        eventMangrSetup.getManager().addActivatorEventListener("1", id -> {
            isWorking[0] = true;
            return null;
        });

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