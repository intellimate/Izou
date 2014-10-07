package karlskrone.jarvis.main;

import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.addon.AddOnManager;
import karlskrone.jarvis.contentgenerator.ContentGeneratorManager;
import karlskrone.jarvis.events.EventManager;
import karlskrone.jarvis.output.OutputManager;

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
