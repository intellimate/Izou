package intellimate.izou.main;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOn;
import intellimate.izou.addon.AddOnManager;
import intellimate.izou.events.EventDistributor;
import intellimate.izou.events.LocalEventManager;
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
    private final ResourceManager resourceManager;
    private final EventDistributor eventDistributor;
    private final LocalEventManager localEventManager;
    private final Thread threadEventManager;
    private final ActivatorManager activatorManager;
    private final AddOnManager addOnManager;

    private Main() {
        outputManager = new OutputManager();
        resourceManager = new ResourceManager();
        eventDistributor = new EventDistributor(resourceManager, outputManager);
        localEventManager = new LocalEventManager(outputManager, resourceManager);
        threadEventManager = new Thread(localEventManager);
        threadEventManager.start();
        activatorManager = new ActivatorManager(localEventManager);
        addOnManager = new AddOnManager(outputManager, localEventManager,resourceManager,activatorManager, this);
        addOnManager.retrieveAndRegisterAddOns();
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns) {
        this();
        addOnManager.addAndRegisterAddOns(addOns);
    }

    public static void main(String[] args) {
        @SuppressWarnings("UnusedAssignment") Main main = new Main();
    }

    public OutputManager getOutputManager() {
        return outputManager;
    }

    public LocalEventManager getLocalEventManager() {
        return localEventManager;
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

    public EventDistributor getEventDistributor() {
        return eventDistributor;
    }
}
