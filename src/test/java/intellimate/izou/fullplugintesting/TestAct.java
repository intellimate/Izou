package intellimate.izou.fullplugintesting;

import intellimate.izou.activator.Activator;
import intellimate.izou.events.EventManager;
import intellimate.izou.system.Context;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestAct extends Activator {
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
        this.registerEvent(EventManager.FULL_WELCOME_EVENT);

        boolean firedEvent = false;
        while (!firedEvent) {
            if(start) {
                start = false;
                System.out.println("1");
                try {
                    this.fireEvent(EventManager.FULL_WELCOME_EVENT);
                    firedEvent = true;
                } catch (EventManager.MultipleEventsException e) {
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
}
