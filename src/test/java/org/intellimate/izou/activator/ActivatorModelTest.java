package org.intellimate.izou.activator;

import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.system.Context;
import intellimate.izouSDK.events.EventImpl;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class ActivatorModelTest extends IzouTest {
    static ActivatorModel activatorModel;
    Identification id;

    public ActivatorModelTest() {
        super(false, ActivatorModelTest.class.getCanonicalName());
        Context context = getContext();
        activatorModel = new ActivatorModel(getContext()) {
            @Override
            public String getID() {
                return "unbelievable activator id";
            }

            @Override
            public void activatorStarts() throws InterruptedException {}

            @Override
            public boolean terminated(Exception e) {return false;}
        };
        main.getActivatorManager().addActivator(activatorModel);
        id = IdentificationManager.getInstance().getIdentification(activatorModel).get();
    }

    @Test
    public void testFireEvent() throws Exception {
        EventModel event2 = EventImpl.createEvent(ActivatorModelTest.class.getCanonicalName() + 2, id).get();
        final boolean[] isWorking = {false};
        main.getEventDistributor().registerEventListener(event2, id -> isWorking[0] = true);
        activatorModel.fireEvent(event2);
        waitForMultith(event2);
        assertTrue(isWorking[0]);
    }


}