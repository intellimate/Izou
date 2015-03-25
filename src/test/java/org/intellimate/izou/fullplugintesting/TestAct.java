package org.intellimate.izou.fullplugintesting;

import org.intellimate.izou.activator.ActivatorModel;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.system.Context;
import intellimate.izouSDK.events.EventImpl;
import org.intellimate.izou.events.MultipleEventsException;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IdentificationManager;

import java.util.Optional;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestAct extends ActivatorModel {
    private boolean start;

    public TestAct(Context context) {
        super(context);
        start = true;
    }

    public void setStart(boolean input) {
        start = input;
    }

    @Override
    public void activatorStarts() throws InterruptedException{
        boolean firedEvent = false;
        while (!firedEvent) {
            if(start) {
                start = false;
                System.out.println("1");
                Optional<Identification> id = IdentificationManager.getInstance().getIdentification(this);
                if(!id.isPresent()) return;
                Optional<EventModel> event = EventImpl.createEvent("1", id.get());
                if(!event.isPresent()) return;
                try {
                    this.fireEvent(event.get());
                    firedEvent = true;
                } catch (MultipleEventsException e) {
                    e.printStackTrace();
                }
                start = false;
            }
            else {
                Thread.sleep(20);
            }
        }

    }

    @Override
    public boolean terminated(Exception e) {
        return false;
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
        return TestAct.class.getCanonicalName();
    }
}
