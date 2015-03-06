package intellimate.izou.main;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOn;
import intellimate.izou.addon.AddOnManager;
import intellimate.izou.events.EventDistributor;
import intellimate.izou.system.file.FileManager;
import intellimate.izou.system.file.*;
import intellimate.izou.system.file.FileSystemManager;
import intellimate.izou.system.javafx.JavaFXInitializer;
import intellimate.izou.system.logger.*;
import intellimate.izouSDK.events.EventPropertiesManager;
import intellimate.izou.events.LocalEventManager;
import intellimate.izou.output.OutputManager;
import intellimate.izou.resource.ResourceManager;
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
     * Creates a new Main instance with debugging enabled (doesn't search the lib-folder)
     *
     * @param javaFX true if javaFX should be started, false otherwise
     * @param debug if true, izou will not load plugin from the lib-folder
     */
    private Main(boolean javaFX, boolean debug) {
        this(null, javaFX, debug);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method, JavaFX is disabled
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns) {
        this(addOns, false, false);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param debug if true, izou will not load plugin from the lib-folder
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns, boolean debug) {
        this(addOns, false, debug);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param javaFX true if javaFX should be started, false otherwise
     * @param debug if true, izou will not load plugin from the lib-folder
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOn> addOns, boolean javaFX, boolean debug) {
         if (javaFX) {
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
         }

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
                        "local_events.properties", eventPropertiesManager);
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
        if (args.length > 0) {
            @SuppressWarnings("UnusedAssignment") Main main = new Main(Boolean.getBoolean(args[0]), false);
        }
        else {
            @SuppressWarnings("UnusedAssignment") Main main = new Main(false, false);
        }
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
