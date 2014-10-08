package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.activator.Activator;
import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.events.EventManager;

/**
 * Created by julianbrendl on 10/7/14.
 */
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
        while (true) {
            if(start) {
                System.out.println("1");
                this.registerEvent("1");
                try {
                    this.fireEvent("1");
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
