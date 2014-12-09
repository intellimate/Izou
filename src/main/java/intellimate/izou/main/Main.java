package intellimate.izou.main;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOn;
import intellimate.izou.addon.AddOnManager;
import intellimate.izou.events.EventDistributor;
import intellimate.izou.events.LocalEventManager;
import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.ResourceManager;
import intellimate.izou.system.FileManager;
import intellimate.izou.system.FileSystemManager;
import intellimate.izou.system.IzouLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
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
    private final ActivatorManager activatorManager;
    private final AddOnManager addOnManager;
    private final Thread threadEventManager;
    private final FileManager fileManager;
    private final IzouLogger izouLogger;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    private Main(boolean debug) {
        FileSystemManager fileSystemManager = new FileSystemManager();
        try {
            fileSystemManager.createIzouFileSystem();
        } catch (IOException e) {
            fileLogger.fatal("Failed to create the FileSystemManager", e);
        }

        izouLogger = new IzouLogger();
        outputManager = new OutputManager();
        resourceManager = new ResourceManager();
        eventDistributor = new EventDistributor(resourceManager, outputManager);
        localEventManager = new LocalEventManager(eventDistributor);
        threadEventManager = new Thread(localEventManager);
        threadEventManager.start();
        activatorManager = new ActivatorManager(localEventManager);

        FileManager fileManagerTemp;
        try {
            fileManagerTemp = new FileManager();
        } catch (IOException e) {
            fileManagerTemp = null;
            fileLogger.fatal("Failed to create the FileManager", e);
        }
        fileManager = fileManagerTemp;

        addOnManager = new AddOnManager(outputManager,resourceManager,activatorManager, fileManager, this);
        if(!debug) addOnManager.retrieveAndRegisterAddOns();
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns) {
        this(false);
        if(addOns != null) addOnManager.addAndRegisterAddOns(addOns);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns, boolean debug) {
        this(debug);
        if(addOns != null) addOnManager.addAndRegisterAddOns(addOns);
    }

    public static void main(String[] args) {
        @SuppressWarnings("UnusedAssignment") Main main = new Main(false);
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

    public synchronized FileManager getFileManager() {
        return fileManager;
    }

    public synchronized IzouLogger getIzouLogger() {
        return izouLogger;
    }
}
