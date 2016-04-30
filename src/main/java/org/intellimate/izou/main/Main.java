package org.intellimate.izou.main;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.activator.ActivatorManager;
import org.intellimate.izou.addon.AddOnManager;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.config.Config;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.events.EventDistributor;
import org.intellimate.izou.events.LocalEventManager;
import org.intellimate.izou.identification.AddOnInformationManager;
import org.intellimate.izou.output.OutputControllerManager;
import org.intellimate.izou.output.OutputManager;
import org.intellimate.izou.resource.ResourceManager;
import org.intellimate.izou.security.SecurityManager;
import org.intellimate.izou.server.CommunicationManager;
import org.intellimate.izou.support.SystemMail;
import org.intellimate.izou.system.SystemInitializer;
import org.intellimate.izou.system.file.FileManager;
import org.intellimate.izou.system.file.FilePublisher;
import org.intellimate.izou.system.file.FileSystemManager;
import org.intellimate.izou.system.javafx.JavaFXInitializer;
import org.intellimate.izou.system.logger.IzouLogger;
import org.intellimate.izou.system.sound.SoundManager;
import org.intellimate.izou.threadpool.ThreadPoolManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
    private final OutputControllerManager outputControllerManager;
    private final ResourceManager resourceManager;
    private final EventDistributor eventDistributor;
    private final LocalEventManager localEventManager;
    private final ActivatorManager activatorManager;
    private final AddOnManager addOnManager;
    private final AddOnInformationManager addOnInformationManager;
    private final FileManager fileManager;
    private final FilePublisher filePublisher;
    private final IzouLogger izouLogger;
    private final ThreadPoolManager threadPoolManager;
    private final SecurityManager securityManager;
    private final SystemInitializer systemInitializer;
    private final SoundManager soundManager;
    private final SystemMail systemMail;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());
    private FileSystemManager fileSystemManager;
    private CommunicationManager communicationManager;

    /**
     * Creates a new Main instance with a optionally disabled lib-folder.
     *
     * @param javaFX true if javaFX should be started, false otherwise
     * @param disableLibFolder  if true, izou will not load plugin from the lib-folder
     */
    private Main(boolean javaFX, boolean disableLibFolder) {
        this(null, javaFX, disableLibFolder, true, null, null);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method, JavaFX is disabled
     *
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOnModel> addOns) {
        this(addOns, false, false, true, null, null);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param disableLibFolder  if true, izou will not load plugin from the lib-folder
     * @param addOns a List of AddOns to run
     */
    public Main(List<AddOnModel> addOns, boolean disableLibFolder) {
        this(addOns, false, disableLibFolder, true, null, null);
    }

    /**
     * If you want to debug your Plugin, you can get an Main instance with this Method
     *
     * @param javaFX true if javaFX should be started, false otherwise
     * @param disableLibFolder  if true, izou will not load plugin from the lib-folder
     * @param addOns a List of AddOns to run
     * @param config null if disabled, otherwise set pointing to the location of the izou-config
     * @param addonsConfig null if disabled, otherwise set pointing to the location of the addons-config
     */
    public Main(List<AddOnModel> addOns, boolean javaFX, boolean disableLibFolder, boolean disabledUpdate, String config, String addonsConfig) {
        fileLogger.debug("Starting Izou");
        fileLogger.debug("Initializing...");
        systemInitializer = initSystem();
        if (addOns != null) {
            fileLogger.debug("setting to debug");
            System.setProperty("debug", "true");
        }

        addOnInformationManager = new AddOnInformationManager(this); // Put this before the addOnManager, it needs it
        addOnManager = new AddOnManager(this);
        threadPoolManager = new ThreadPoolManager(this);
        izouLogger = new IzouLogger();
        outputManager = new OutputManager(this);
        outputControllerManager = new OutputControllerManager();
        resourceManager = new ResourceManager(this);
        eventDistributor = new EventDistributor(this);
        localEventManager = new LocalEventManager(this);
        threadPoolManager.getIzouThreadPool().submit(localEventManager);
        activatorManager = new ActivatorManager(this);
        filePublisher = new FilePublisher(this);
        soundManager = new SoundManager(this);

        fileManager = initFileManager();

        systemInitializer.registerWithPropertiesManager();

        setUpJavaFX(javaFX);

        fileLogger.debug("Done initializing.");
        fileLogger.debug("Adding addons..");

        // Starting security manager
        systemMail = initMail();
        securityManager = startSecurity(systemMail);

        if (addOns != null && !disableLibFolder) {
            fileLogger.debug("adding addons from the parameter without registering");
            addOnManager.addAddOnsWithoutRegistering(addOns);
        } else if (addOns != null) {
            fileLogger.debug("adding and registering addons from the parameter");
            addOnManager.addAndRegisterAddOns(addOns);
        }
        if (!disableLibFolder) {
            fileLogger.debug("retrieving addons & registering them");
            addOnManager.retrieveAndRegisterAddOns();
        }

        this.communicationManager = initCommunication(config, addonsConfig, disableLibFolder, disabledUpdate);
    }

    public static void main(String[] args) {
        File config = new File("./izou.yml");
        if (!config.exists()) {
            System.out.println("izou config is not existing, path: "+config.getAbsolutePath());
            System.exit(-1);
        }
        File addonsConfig = new File("./addons.yml");
        if (!config.exists()) {
            System.out.println("addons config is not existing, path: "+config.getAbsolutePath());
            System.exit(-1);
        }
        if (args.length > 0) {
            @SuppressWarnings("UnusedAssignment") Main main = new Main(new ArrayList<>(), Boolean.getBoolean(args[0]), false, false, config.getAbsolutePath(), addonsConfig.getAbsolutePath());
        } else {
            @SuppressWarnings("UnusedAssignment") Main main = new Main(new ArrayList<>(), false, false, false, config.getAbsolutePath(), addonsConfig.getAbsolutePath());
        }
    }

    private SystemMail initMail() {
        // Create system mail
        SystemMail mailTemp = null;
        try {
            mailTemp = SystemMail.createSystemMail();
        } catch (IllegalAccessException e) {
            fileLogger.fatal("Unable to create a system mail object");
        }
        return mailTemp;
    }

    private void setUpJavaFX(boolean javaFX) {
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
    }

    private SecurityManager startSecurity(SystemMail systemMail) {
        SecurityManager securityManagerTemp;
        try {
            securityManagerTemp = SecurityManager.createSecurityManager(systemMail, this);
        } catch (IllegalAccessException e) {
            securityManagerTemp = null;
            fileLogger.fatal("Security manager already exists", e);
        }
        if (!Boolean.getBoolean("noSecurity")) {
            try {
                System.setSecurityManager(securityManagerTemp);
            } catch (SecurityException e) {
                fileLogger.fatal("Security manager already exists", e);
            }
        }
        return securityManagerTemp;
    }

    private FileManager initFileManager() {
        FileManager fileManagerTemp;
        try {
            fileManagerTemp = new FileManager(this);
        } catch (IOException e) {
            fileManagerTemp = null;
            fileLogger.fatal("Failed to create the FileManager", e);
        }

        return fileManagerTemp;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }

    public OutputManager getOutputManager() {
        return outputManager;
    }

    public OutputControllerManager getOutputControllerManager() {
        return outputControllerManager;
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

    public AddOnInformationManager getAddOnInformationManager() {
        return addOnInformationManager;
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

    public FileSystemManager getFileSystemManager() {
        return fileSystemManager;
    }

    public SystemInitializer getSystemInitializer() {
        return systemInitializer;
    }

    public InternalIdentificationManager getInternalIdentificationManager() {
        return internalIdentificationManager;
    }

    public Optional<CommunicationManager> getCommunicationManager() {
        return Optional.ofNullable(communicationManager);
    }

    private SystemInitializer initSystem() {
        // Setting up file system
        this.fileSystemManager = new FileSystemManager(this);
        try {
            fileSystemManager.createIzouFileSystem();
        } catch (IOException e) {
            fileLogger.fatal("Failed to create the FileSystemManager", e);
        }

        SystemInitializer systemInitializer = new SystemInitializer(this);
        systemInitializer.initSystem();
        return systemInitializer;
    }

    private CommunicationManager initCommunication(String configPath, String addonsConfigPath, boolean disabledLib, boolean disabledUpdate) {
        if (configPath == null || addonsConfigPath == null) {
            return null;
        }
        File configFile = new File(configPath);
        if (!configFile.exists()) {
            fileLogger.error("unable to load izou config");
            System.out.println(-1);
        }

        File addonsConfigFile = new File(addonsConfigPath);
        if (!addonsConfigFile.exists()) {
            fileLogger.error("unable to load izou config");
            System.out.println(-1);
        }
        YamlReader reader = null;
        try {
            reader = new YamlReader(new FileReader(configFile));
        } catch (FileNotFoundException e) {
            fileLogger.error("unable to load izou config", e);
            System.out.println(-1);
            return null;
        }
        Config config;
        try {
            config = reader.read(Config.class);
        } catch (YamlException e) {
            fileLogger.error("unable to load izou config", e);
            System.out.println(-1);
            return null;
        }

        try {
            reader = new YamlReader(new FileReader(addonsConfigFile));
        } catch (FileNotFoundException e) {
            fileLogger.error("unable to load addons config", e);
            System.out.println(-1);
            return null;
        }

        List<AddOn> addOns;
        try {
            addOns = reader.read(List.class,AddOn.class);
        } catch (YamlException e) {
            fileLogger.error("unable to load addons config", e);
            System.out.println(-1);
            return null;
        }

        AddOn addOn =  new AddOn();
        String rawVersion = addOn.getClass().getPackage().getImplementationVersion();
        Version version = null;
        if (rawVersion != null) {
            version = new Version(rawVersion);
        }
        CommunicationManager communicationManager = null;
        try {
            CommunicationManager finalCommunManager = new CommunicationManager(version, this, addOns, disabledLib, config.token);
            communicationManager = finalCommunManager;
            if (!disabledUpdate) {
                LocalDateTime firstInterval = LocalDateTime.now().withHour(2);
                if (firstInterval.plusMinutes(10).isBefore(LocalDateTime.now())) {
                    firstInterval = firstInterval.minusDays(1);
                }
                Timer timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        boolean shouldRestart = false;
                        //TODO maybe a more sophisticated algorithm?
                        try {
                            shouldRestart = finalCommunManager.checkForUpdates();
                        } catch (IOException e) {
                            fileLogger.error("unable to search for updates", e);
                            try {
                                Thread.sleep(300000);
                                shouldRestart = finalCommunManager.checkForUpdates();
                            } catch (IOException e1) {
                                fileLogger.error("unable to search for updates", e);
                            } catch (InterruptedException e1) {
                                fileLogger.error("interrupted", e);
                            }
                        }
                        if (shouldRestart) {
                            fileLogger.info("restarting to apply updates");
                            System.exit(0);
                        }
                    }
                }, Date.from(firstInterval.atZone(ZoneId.systemDefault()).toInstant()), 8640000);
            }
        } catch (IllegalStateException e) {
            fileLogger.error("unable to instantiate CommunicationManager", e);
            System.exit(-1);
        }
        return communicationManager;
    }

    public SoundManager getSoundManager() {
        return soundManager;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
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

    public FileSystemManager getFileSystemManager() {
        return fileSystemManager;
    }

    public SystemInitializer getSystemInitializer() {
        return systemInitializer;
    }

    public InternalIdentificationManager getInternalIdentificationManager() {
        return internalIdentificationManager;
    }

    public Optional<CommunicationManager> getCommunicationManager() {
        return Optional.ofNullable(communicationManager);
    }
}
