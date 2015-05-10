package org.intellimate.izou.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.activator.ActivatorManager;
import org.intellimate.izou.addon.AddOnManager;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.events.EventDistributor;
import org.intellimate.izou.events.LocalEventManager;
import org.intellimate.izou.output.OutputManager;
import org.intellimate.izou.resource.ResourceManager;
import org.intellimate.izou.security.SecurityManager;
import org.intellimate.izou.support.SystemMail;
import org.intellimate.izou.system.SystemInitializer;
import org.intellimate.izou.system.file.FileManager;
import org.intellimate.izou.system.file.FilePublisher;
import org.intellimate.izou.system.file.FileSystemManager;
import org.intellimate.izou.system.javafx.JavaFXInitializer;
import org.intellimate.izou.system.logger.IzouLogger;
import org.intellimate.izou.threadpool.ThreadPoolManager;

import java.io.IOException;
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
    private final SecurityManager securityManager;
    private final SystemInitializer systemInitializer;
    private final SystemMail systemMail;
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
    public Main(List<AddOnModel> addOns) {
        this(addOns, false, false);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param debug if true, izou will not load plugin from the lib-folder
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOnModel> addOns, boolean debug) {
        this(addOns, false, debug);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param javaFX true if javaFX should be started, false otherwise
     * @param debug if true, izou will not load plugin from the lib-folder
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOnModel> addOns, boolean javaFX, boolean debug) {
        fileLogger.debug("starting izou");
        fileLogger.debug("initializing");
        systemInitializer = new SystemInitializer();
        systemInitializer.initSystem();
        // Starts javaFX if desired
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

        // Create system mail
        SystemMail mailTemp = null;
        try {
            mailTemp = SystemMail.createSystemMail();
        } catch (IllegalAccessException e) {
            fileLogger.fatal("Unable to create a system mail object");
        }
        systemMail = mailTemp;

        // Setting up file system
        FileSystemManager fileSystemManager = new FileSystemManager(this);
        try {
            fileSystemManager.createIzouFileSystem();
        } catch (IOException e) {
            fileLogger.fatal("Failed to create the FileSystemManager", e);
        }

        // Starting security manager
        SecurityManager securityManagerTemp;
        try {
            securityManagerTemp = SecurityManager.createSecurityManager(systemMail);
        } catch (IllegalAccessException e) {
            securityManagerTemp = null;
            fileLogger.fatal("Security manager already exists", e);
        }
        securityManager = securityManagerTemp;
        try {
            System.setSecurityManager(securityManager);
        } catch (SecurityException e) {
            fileLogger.fatal("Security manager already exists", e);
        }

        threadPoolManager = new ThreadPoolManager(this);
        izouLogger = new IzouLogger();
        outputManager = new OutputManager(this);
        resourceManager = new ResourceManager(this);
        eventDistributor = new EventDistributor(this);
        localEventManager = new LocalEventManager(this);
        threadPoolManager.getIzouThreadPool().submit(localEventManager);
        activatorManager = new ActivatorManager(this);
        filePublisher = new FilePublisher(this);

        FileManager fileManagerTemp;
        try {
            fileManagerTemp = new FileManager(this);
        } catch (IOException e) {
            fileManagerTemp = null;
            fileLogger.fatal("Failed to create the FileManager", e);
        }
        fileManager = fileManagerTemp;
        fileLogger.debug("finished initializing");
        fileLogger.debug("adding addons");
        addOnManager = new AddOnManager(this);
        if (addOns != null && !debug) {
            fileLogger.debug("adding addons from the parameter without registering");
            addOnManager.addAddOnsWithoutRegistering(addOns);
        } else if(addOns != null) {
            fileLogger.debug("adding and registering addons from the parameter");
            addOnManager.addAndRegisterAddOns(addOns);
        }
        if (!debug) {
            fileLogger.debug("retrieving addons & registering them");
            addOnManager.retrieveAndRegisterAddOns();
        }
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
