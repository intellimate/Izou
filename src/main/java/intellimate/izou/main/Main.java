package intellimate.izou.main;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOnManager;
import intellimate.izou.contentgenerator.ContentGeneratorManager;
import intellimate.izou.events.EventManager;
import intellimate.izou.output.OutputManager;

/**
 * Main Class.
 *
 * This is where our journey begins and all the Managers get initialized
 */
@SuppressWarnings("FieldCanBeLocal")
public class Main {
    private final OutputManager outputManager;
    private final EventManager eventManager;
    private final ContentGeneratorManager contentGeneratorManager;
    private final ActivatorManager activatorManager;
    private final AddOnManager addOnManager;
    private final Thread threadEventManager;

    private Main() {
        outputManager = new OutputManager();
        eventManager = new EventManager(outputManager);
        threadEventManager = new Thread(eventManager);
        threadEventManager.start();
        contentGeneratorManager = new ContentGeneratorManager(eventManager);
        activatorManager = new ActivatorManager(eventManager);
        addOnManager = new AddOnManager(outputManager,eventManager,contentGeneratorManager,activatorManager);
        addOnManager.retrieveAndRegisterAddOns();
    }

    public static void main(String[] args) {
        Main main = new Main();
    }
}
