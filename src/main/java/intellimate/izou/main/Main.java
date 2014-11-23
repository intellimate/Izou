package intellimate.izou.main;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOn;
import intellimate.izou.addon.AddOnManager;
import intellimate.izou.events.EventManager;
import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.ResourceManager;

import java.util.List;

/**
 * Main Class.
 *
 * This is where our journey begins and all the Managers get initialized
 */
@SuppressWarnings("FieldCanBeLocal")
public class Main {
    private final OutputManager outputManager;
    private final EventManager eventManager;
    private final ResourceManager resourceManager;
    private final ActivatorManager activatorManager;
    private final AddOnManager addOnManager;
    private final Thread threadEventManager;

    private Main() {
        outputManager = new OutputManager();
        resourceManager = new ResourceManager();
        eventManager = new EventManager(outputManager, resourceManager);
        threadEventManager = new Thread(eventManager);
        threadEventManager.start();
        activatorManager = new ActivatorManager(eventManager);
        addOnManager = new AddOnManager(outputManager,eventManager,resourceManager,activatorManager, this);
        addOnManager.retrieveAndRegisterAddOns();
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns) {
        outputManager = new OutputManager();
        resourceManager = new ResourceManager();
        eventManager = new EventManager(outputManager, resourceManager);
        threadEventManager = new Thread(eventManager);
        threadEventManager.start();
        activatorManager = new ActivatorManager(eventManager);
        addOnManager = new AddOnManager(outputManager,eventManager,resourceManager,activatorManager, this);
        addOnManager.addAndRegisterAddOns(addOns);
    }

    public static void main(String[] args) {
        @SuppressWarnings("UnusedAssignment") Main main = new Main();
    }

    public OutputManager getOutputManager() {
        return outputManager;
    }

    public EventManager getEventManager() {
        return eventManager;
    }

    public ActivatorManager getActivatorManager() {
        return activatorManager;
    }

    public AddOnManager getAddOnManager() {
        return addOnManager;
    }

    public Thread getThreadEventManager() {
        return threadEventManager;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }
}
