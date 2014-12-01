package intellimate.izou.activator;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.EventManagerTestSetup;
import intellimate.izou.fullplugintesting.TestAddOn;
import intellimate.izou.main.Main;
import intellimate.izou.system.Context;
import intellimate.izou.events.Event;
import intellimate.izou.system.Identification;
import intellimate.izou.system.IdentificationManager;
import intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ActivatorTest extends IzouTest{
    static Activator activator;
    Identification id;

    public ActivatorTest() {
        Context context = new Context(testAddOn, main, "1", "debug");
        activator = new Activator() {
            @Override
            public String getID() {
                return "unbelievable activator id";
            }

            @Override
            public void activatorStarts() throws InterruptedException {}

            @Override
            public boolean terminated(Exception e) {return false;}
        };
        main.getActivatorManager().addActivator(activator);
        id = IdentificationManager.getInstance().getIdentification(activator).get();
    }

    @Test
    public void testFireEvent() throws Exception {
        Event event2 = Event.createEvent(ActivatorTest.class.getCanonicalName() + 2, id).get();
        final boolean[] isWorking = {false};
        main.getEventDistributor().registerEventListener(event2, id -> isWorking[0] = true);
        activator.fireEvent(event2);
        waitForMultith();
        assertTrue(isWorking[0]);
    }


}