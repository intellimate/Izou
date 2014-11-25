package intellimate.izou.fullplugintesting;

import intellimate.izou.activator.Activator;
import intellimate.izou.events.LocalEventManager;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestAct extends Activator {
    private boolean start;

    public TestAct() {
        start = false;
    }

    public void setStart(boolean input) {
        start = input;
    }

    @Override
    public void activatorStarts() throws InterruptedException{
        boolean firedEvent = false;
        while (!firedEvent) {
            if(start) {
                System.out.println("1");
                this.registerEvent("1");
                try {
                    this.fireEvent("1");
                    firedEvent = true;
                } catch (LocalEventManager.MultipleEventsException e) {
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
