package karlskrone.jarvis.events;

import karlskrone.jarvis.output.OutputManager;

import java.util.concurrent.Future;

/**
 * Demo Event Reciever to explain the Event-Listening.
 *
 * This class implements EventManager.ActivatorEventListener, the Function activatorEventFired will be called when
 * an event was fired.
 */
public class DemoEvent implements EventManager.ActivatorEventListener{
    EventManager manager;

    @Override
    public Future activatorEventFired(String id) {
       //code here will be executed whe an event was fired
       System.out.println("Hello!");
        return null;
    }

    public DemoEvent() {
        //first you have create an EventManager instance
        manager = new EventManager(new OutputManager());

        //this function lets this class listen to the event "1"
        manager.addActivatorEventListener("1", this);


        //to fire an event you have to retrieve an ActivatorEventCaller through the registerActivatorCaller method
        EventManager.ActivatorEventCaller caller1 = manager.registerActivatorCaller("1");
        try {
            //the method fire fires the event
            caller1.fire();
        } catch (Exception ignored) {  }
    }
}
