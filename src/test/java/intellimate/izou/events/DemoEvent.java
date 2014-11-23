package intellimate.izou.events;

import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.Resource;

import java.util.List;
import java.util.concurrent.Future;

/**
 * Demo Event Reciever to explain the Event-Listening.
 *
 * This class implements EventManager.ActivatorEventListener, the Function eventFired will be called when
 * an event was fired.
 */
public class DemoEvent implements EventListener {
    EventManager manager;

    @Override
    public Future<List<Resource>> eventFired(String id) {
       //code here will be executed whe an event was fired
       System.out.println("Hello!");
        return null;
    }

    public DemoEvent() {
        //first you have create an EventManager instance
        manager = new EventManager(new OutputManager());

        //this function lets this class listen to the event "1"
        manager.registerEventListener("1", this);


        //to fire an event you have to retrieve an ActivatorEventCaller through the registerCaller method
        EventManager.EventCaller caller1 = manager.registerCaller("1");
        try {
            //the method fire fires the event
            caller1.fire();
        } catch (Exception ignored) {  }
    }
}
