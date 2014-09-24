package karlskrone.jarvis.events;

/**
 * Demo Event Reciever to explain the Event-Listening.
 *
 * This class implements EventManager.ActivatorEventListener, the Function activatorEventFired will be called when
 * an event was fired.
 */
public class DemoEvent implements EventManager.ActivatorEventListener{
    EventManager manager;

    @Override
    public void activatorEventFired(String id) {
       //code here will be executed whe an event was fired
       System.out.println("Hello!");
    }

    public DemoEvent() {
        //first you have create an EventManager instance
        manager = new EventManager();

        //this function lets this class listen to the event "1"
        manager.addActivatorEventListener("1", this);


        //to fire an event you have to retrieve an ActivatorEventCaller through the registerActivatorEvent method
        EventManager.ActivatorEventCaller caller1 = manager.registerActivatorEvent("1");
        try {
            //the method fire fires the event
            caller1.fire();
        } catch (Exception ignored) {  }
    }
}
