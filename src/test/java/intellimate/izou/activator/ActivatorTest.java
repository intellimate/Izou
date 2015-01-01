package intellimate.izou.activator;

import intellimate.izou.events.Event;
import intellimate.izou.system.Context;
import intellimate.izou.system.Identification;
import intellimate.izou.system.IdentificationManager;
import intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ActivatorTest extends IzouTest{
    static Activator activator;
    Identification id;

    public ActivatorTest() {
        super(false, ActivatorTest.class.getCanonicalName());
        Context context = getContext();
        activator = new Activator(getContext()) {
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
        waitForMultith(event2);
        assertTrue(isWorking[0]);
    }


}