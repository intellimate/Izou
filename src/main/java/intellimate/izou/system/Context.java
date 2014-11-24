package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.Event;
import intellimate.izou.events.EventListener;
import intellimate.izou.events.EventManager;
import intellimate.izou.main.Main;
import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceBuilder;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * This class provides much of the general Communication with Izou.
 */
@SuppressWarnings("UnusedDeclaration")
public class Context {
    @SuppressWarnings("FieldCanBeLocal")
    private AddOn addOn;
    private Main main;
    public Events events = new Events();
    public Resources resources = new Resources();
    public Context(AddOn addOn, Main main) {
        this.addOn = addOn;
        this.main = main;
    }

    private class Events {
        public void registerEventListener(Event event, EventListener eventListener) {
            main.getEventManager().registerEventListener(event, eventListener);
        }

        public void unregisterEventListener(Event event, EventListener eventListener) {
            main.getEventManager().unregisterEventListener(event, eventListener);
        }

        public Optional<EventManager.EventCaller> registerEventCaller(Identification identification) {
            return main.getEventManager().registerCaller(identification);
        }

        public void unregisterEventCaller(Identification identification) {
            main.getEventManager().unregisterCaller(identification);
        }
    }

    private class Resources {
        public void registerResourceBuilder(ResourceBuilder resourceBuilder) {
            main.getResourceManager().registerResourceBuilder(resourceBuilder);
        }

        public void unregisterResourceBuilder(ResourceBuilder resourceBuilder) {
            main.getResourceManager().unregisterResourceBuilder(resourceBuilder);
        }

        public void generateResource(Resource resource, Consumer<List<Resource>> consumer) {
            main.getResourceManager().generatedResource(resource, consumer);
        }
    }
}
