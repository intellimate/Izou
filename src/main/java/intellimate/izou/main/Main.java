package intellimate.izou.main;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOn;
import intellimate.izou.addon.AddOnManager;
import intellimate.izou.events.EventDistributor;
import intellimate.izou.events.EventPropertiesManager;
import intellimate.izou.events.LocalEventManager;
import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.ResourceManager;
import intellimate.izou.system.*;
import intellimate.izou.threadpool.ThreadPoolManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Main Class.
 *
 * This is where our journey begins and all the Managers get initialized
 */
@SuppressWarnings("FieldCanBeLocal")
public class Main {
    public static AtomicBoolean jfxToolKitInit;
    private static final long INIT_TIME_LIMIT = 10000;
    private final OutputManager outputManager;
    private final ResourceManager resourceManager;
    private final EventDistributor eventDistributor;
    private final LocalEventManager localEventManager;
    private final ActivatorManager activatorManager;
    private final AddOnManager addOnManager;
    private final FileManager fileManager;
    private final FilePublisher filePublisher;
    private final IzouLogger izouLogger;
    private final ThreadPoolManager threadPoolManager;
    private final EventPropertiesManager eventPropertiesManager;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * creates a new Main instance with debugging enabled (doesn't search the lib-folder)
     * @param debug if true, izou will not load plugin from the lib-folder
     */
    private Main(boolean debug) {
        this(null, debug);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns) {
        this(addOns, false);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param debug if true, izou will not load plugin from the lib-folder
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns, boolean debug) {
        jfxToolKitInit = new AtomicBoolean(false);
        JavaFXInitializer.initToolKit();
        fileLogger.debug("Initializing JavaFX ToolKit");

        long startTime = System.currentTimeMillis();
        long duration = 0;
        while (!jfxToolKitInit.get() && duration < INIT_TIME_LIMIT) {
            duration = System.currentTimeMillis() - startTime;
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                fileLogger.error("Error happened while thread was sleeping", e);
            }
        }

        if (!jfxToolKitInit.get()) {
            fileLogger.error("Unable to Initialize JavaFX ToolKit");
        }
        fileLogger.debug("Done initializing JavaFX ToolKit");

        FileSystemManager fileSystemManager = new FileSystemManager();
        try {
            fileSystemManager.createIzouFileSystem();
        } catch (IOException e) {
            fileLogger.fatal("Failed to create the FileSystemManager", e);
        }
        threadPoolManager = new ThreadPoolManager();
        izouLogger = new IzouLogger();
        outputManager = new OutputManager(this);
        resourceManager = new ResourceManager(this);
        eventDistributor = new EventDistributor(this);
        localEventManager = new LocalEventManager(eventDistributor);
        threadPoolManager.getIzouThreadPool().submit(localEventManager);
        activatorManager = new ActivatorManager(this);
        filePublisher = new FilePublisher(this);
        eventPropertiesManager = new EventPropertiesManager();

        FileManager fileManagerTemp;
        try {
            fileManagerTemp = new FileManager(filePublisher);
        } catch (IOException e) {
            fileManagerTemp = null;
            fileLogger.fatal("Failed to create the FileManager", e);
        }
        fileManager = fileManagerTemp;

        try {
            if (fileManager != null) {
                fileManager.registerFileDir(Paths.get(FileSystemManager.PROPERTIES_PATH),
                        "PopularEvents.properties", eventPropertiesManager);
            }
        } catch (IOException e) {
            fileLogger.error("Unable to register the eventPropertiesManager", e);
        }

        addOnManager = new AddOnManager(this);
        if(addOns != null && !debug) {
            addOnManager.addAddOnsWithoutRegistering(addOns);
        } else if(addOns != null) {
            addOnManager.addAndRegisterAddOns(addOns);
        }
        if(!debug) addOnManager.retrieveAndRegisterAddOns();
    }

    public static void main(String[] args) {
        @SuppressWarnings("UnusedAssignment") Main main = new Main(false);

        /*
        try {
            AudioInputStream inputStream = AudioSystem.getAudioInputStream(new File("./resources/IzouClock/mama-geb.mp3"));
        } catch (UnsupportedAudioFileException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
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

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public EventDistributor getEventDistributor() {
        return eventDistributor;
    }

    public EventPropertiesManager getEventPropertiesManager() {
        return eventPropertiesManager;
    }

    public ThreadPoolManager getThreadPoolManager() {
        return threadPoolManager;
    }

    public FileManager getFileManager() {
        return fileManager;
    }

    public FilePublisher getFilePublisher() {
        return filePublisher;
    }

    public IzouLogger getIzouLogger() {
        return izouLogger;
    }
}
