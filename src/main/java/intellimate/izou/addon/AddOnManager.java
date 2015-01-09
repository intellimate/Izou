package intellimate.izou.addon;

import intellimate.izou.main.Main;
import intellimate.izou.system.Context;
import intellimate.izou.system.Identifiable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.fortsoft.pf4j.PluginManager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Manages all the AddOns.
 */
public class AddOnManager {
    private List<AddOn> addOnList;
    public static final String ADDON_DATA_PATH = "." + File.separator + "resources" + File.separator;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());
    private final Main main;

    public AddOnManager(Main main) {
        addOnList = new LinkedList<>();
        this.main = main;
    }

    /**
     * loops through all AddOns and lets them register all their Activators
     */
    private void registerActivators() {
        registerIzouModule(AddOn::registerActivator,
                activator -> main.getActivatorManager().addActivator(activator));
    }

    /**
     * loops through all AddOns and lets them register all their ContentGenerators
     */
    private void registerContentGenerators() {
        registerIzouModule(AddOn::registerContentGenerator,
                contentGenerator -> main.getResourceManager().registerResourceBuilder(contentGenerator));
    }

    /**
     * loops through all AddOns and lets them register all their EventController
     */
    private void registerEventControllers() {
        registerIzouModule(AddOn::registerEventController,
                eventsController -> main.getEventDistributor().registerEventsController(eventsController));
    }

    /**
     * loops through all AddOns and lets them register all their OutputsPlugins
     */
    private void registerOutputPlugins() {
        registerIzouModule(AddOn::registerOutputPlugin,
                outputPlugin -> main.getOutputManager().addOutputPlugin(outputPlugin));
    }

    /**
     * loops through all AddOns and lets them register all their OutputsExtensions
     */
    private void registerOutputExtensions() {
        registerIzouModule(AddOn::registerOutputExtension,
                outputExtension ->
                        main.getOutputManager().addOutputExtension(outputExtension, outputExtension.getPluginId()));
        /*
        for (AddOn addOn : addOnList) {
            try {
                OutputExtension[] outputExtensions = addOn.registerOutputExtension();
                if (outputExtensions == null || outputExtensions.length == 0) continue;
                for (OutputExtension outputExtension : addOn.registerOutputExtension()) {
                    if (outputExtension == null) continue;
                    fileLogger.debug("registering OutputExtension: " + outputExtension.getID()
                                            + " from AddOn: " + addOn.getID());
                    try {
                        main.getOutputManager().addOutputExtension(outputExtension, outputExtension.getPluginId());
                    } catch (Exception e) {
                        fileLogger.error("Error while registering the OutputExtension: " + outputExtension.getID(), e);
                    }
                }
            } catch (Exception | NoClassDefFoundError e) {
                fileLogger.error("Error while trying to register the OutputExtensions", e);
            }
        }
        */
    }

    /**
     * registers modules from the AddOns (e.g. OutputPlugins)
     * @param supplierFunction the supplier, taking a addOn and returning an array of modules
     * @param consumer use this consumer to register your
     * @param <T> The type of the modules
     */
    public <T extends Identifiable> void registerIzouModule(Function<AddOn, T[]> supplierFunction, Consumer<T> consumer) {
        runOnAddOnsAsync(supplierFunction).stream()
                .filter(Objects::nonNull)
                .map(Arrays::asList)
                .flatMap(List::stream)
                .filter(Objects::nonNull)
                .peek(module -> fileLogger.debug("registering Module: " + module.getID()))
                .forEach(module -> {
                    try {
                        consumer.accept(module);
                    } catch (Exception e) {
                        fileLogger.debug("Exception while trying to register Module: " + module.getID(), e);
                    } catch (LinkageError e) {
                        fileLogger.debug("Error while trying to register Module: " + module.getID(), e);
                    }
                });
    }

    /**
     * registers all property files with the PropertyManager
     */
    private void registerFiles() {
        String dir = "." + File.separator + "properties";
        runOnAddOnsAsync(addOn -> {
            if(!(getFolder(addOn) == null)) {
                try {
                    main.getFileManager().registerFileDir(Paths.get(dir), addOn.getID(), addOn);
                } catch (IOException e) {
                    fileLogger.error("error while trying to register Files for addon" + addOn.getID(), e);
                }
                addOn.setDefaultPropertiesPath(getFolder(addOn));
            } else {
                fileLogger.debug("no property file was found for AddOn: " + addOn.getID());
            }
        });
    }

    /**
     * registers all the properties-Files for the AddOns
     */
    private void registerProperties() {
        runOnAddOnsAsync(AddOn::initProperties);
    }

    /**
     * gets folder of addOn
     *
     * @param addOn the addOn for which to get the folder
     * @return the folder as a String or null if it was not found
     */
    private String getFolder(AddOn addOn) {
        String addOnName = addOn.getClass().getPackage().getName();
        String[] nameParts = addOnName.split("\\.");

        File file = new File("." + File.separator + "lib");
        String[] directories = file.list((current, name) -> new File(current, name).isDirectory());

        for(String fileName : directories) {
            if(nameParts.length - 1 >= 0 && fileName.contains(nameParts[nameParts.length - 1]))
                return fileName;
        }
        return null;
    }

    /**
     * retrieves and registers all AddOns.
     */
    public void retrieveAndRegisterAddOns() {
        addAllAddOns();
        initAllAddOns();
        registerFiles();
        registerProperties();
        prepareAllAddOns();
        registerAllAddOns();
    }

    /**
     * Adds AddOns without registering them.
     * @param addOns a List containing all the AddOns
     */
    public void addAddOnsWithoutRegistering(List<AddOn> addOns) {
        addOnList.addAll(addOns);
    }

    /**
     * registers all AddOns.
     *
     * @param addOns a List containing all the AddOns
     */
    public void addAndRegisterAddOns(List<AddOn> addOns) {
        addOnList.addAll(addOns);
        initAllAddOns();
        registerFiles();
        registerProperties();
        prepareAllAddOns();
        registerAllAddOns();
    }

    /**
     * This method searches all the "/lib"-directory for AddOns and adds them to the addOnList
     */
    private void addAllAddOns() {
        File libFile;
        try {
            String libPath = new File(".").getCanonicalPath() + File.separator + "lib";
            libFile = new File(libPath);
        } catch (IOException e) {
            fileLogger.error("Error while trying to get the lib-directory"+e);
            return;
        }

        PluginManager pluginManager = new IzouPluginManager(libFile);
        // load the plugins
        pluginManager.loadPlugins();

        // enable a disabled plugin
        //pluginManager.enablePlugin("welcome-plugin");

        // start (active/resolved) the plugins
        try {
            pluginManager.startPlugins();
        } catch (Exception e) {
            fileLogger.fatal("Error while trying to start the PF4J-Plugins", e);
        }
        try {
            List<AddOn> addOns = pluginManager.getExtensions(AddOn.class);
            addOnList.addAll(addOns);
        } catch (Exception e) {
            fileLogger.fatal("Error while trying to start the AddOns", e);
        }
    }

    /**
     * prepares all AddOns
     */
    private void prepareAllAddOns() {
        runOnAddOnsAsync(AddOn::prepare);
    }

    /**
     * internal initiation of all addOns
     */
    private void initAllAddOns() {
        runOnAddOnsAsync((Consumer<AddOn>) addOn -> addOn.initAddOn(new Context(addOn, main, "warn")));
    }

    /**
     * a blocking operation which runs in the addonThread pool, blocking and returning all the results
     * @param function the function to execute
     * @param <T> a list containing all the objects returned by the addons
     */
    public <T> List<T> runOnAddOnsAsync(Function<AddOn, T> function) {
        ExecutorService addOnThreadPool = main.getThreadPoolManager().getAddOnsThreadPool();

        return addOnList.stream()
                .map(addOn -> (Supplier<T>) () -> {
                    try {
                        return function.apply(addOn);
                    } catch (LinkageError e) {
                        throw new RuntimeException(e);
                    }
                })
                .map(supplier -> CompletableFuture.supplyAsync(supplier, addOnThreadPool))
                .map(future -> {
                    try {
                        return future.get(2, TimeUnit.SECONDS);
                    } catch (InterruptedException | ExecutionException e) {
                        fileLogger.error("Addon crashed while trying to initialize some part", e);
                    } catch (TimeoutException e) {
                        fileLogger.error("initializing the addon timed out", e);
                        future.cancel(true);
                    }
                    return null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * like {@link #runOnAddOnsAsync(java.util.function.Function)}, but without return type
     * @param consumer the consumer to execute
     */
    public void runOnAddOnsAsync(Consumer<AddOn> consumer) {
        runOnAddOnsAsync(addOn -> {
            consumer.accept(addOn);
            return null;
        });
    }

    /**
     * registers all AddOns
     */
    private void registerAllAddOns() {
        registerOutputPlugins();
        registerOutputExtensions();
        registerContentGenerators();
        registerEventControllers();
        registerActivators();
    }
}
