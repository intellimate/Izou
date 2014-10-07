package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.activator.Activator;
import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.events.EventManager;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestAct extends Activator {
    private boolean start;

    public TestAct(EventManager eventManager) {
        super(eventManager);
        start = false;
    }

    public void setStart(boolean input) {
        start = input;
    }

    @Override
    public void activatorStarts() throws InterruptedException, EventManager.MultipleEventsException {
        while (true) {
            if(start) {
                this.registerEvent("1");
                this.fireEvent("1");
                start = false;
            }
            else {
                Thread.sleep(20);
            }
        }

    }

    @Override
    public void terminated(Exception e) {

    }
}
