package org.intellimate.izou.addon;

import org.apache.logging.log4j.Level;
import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.system.Context;
import org.intellimate.izou.system.context.ContextImplementation;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.IzouPluginClassLoader;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Manages all the AddOns.
 */
public class AddOnManager extends IzouModule implements AddonThreadPoolUser {
    private IdentifiableSet<AddOnModel> addOns = new IdentifiableSet<>();
    private HashMap<AddOnModel, PluginWrapper> pluginWrappers = new HashMap<>();
    
    public AddOnManager(Main main) {
        super(main);
    }

    /**
    * retrieves and registers all AddOns.
    */
    public void retrieveAndRegisterAddOns() {
        addOns.addAll(loadAddOns());
        registerAllAddOns(addOns);
    }

    /**
     * Adds AddOns without registering them.
     * @param addOns a List containing all the AddOns
     */
    public void addAddOnsWithoutRegistering(List<AddOnModel> addOns) {
        this.addOns.addAll(addOns);
    }

    /**
     * registers all AddOns.
     *
     * @param addOns a List containing all the AddOns
     */
    public void addAndRegisterAddOns(List<AddOnModel> addOns) {
        this.addOns.addAll(addOns);
        registerAllAddOns(this.addOns);
    }
    
    public void registerAllAddOns(IdentifiableSet<AddOnModel> addOns) {
        initAddOns(addOns);
        List<CompletableFuture<Void>> futures = addOns.stream()
                .map(addOn -> submit((Runnable) addOn::register))
                .collect(Collectors.toList());
        try {
            timeOut(futures, 3000);
        } catch (InterruptedException e) {
            debug("interrupted while trying to mite out the addOns", e);
        }
    }

    private void initAddOns(IdentifiableSet<AddOnModel> addOns) {
        addOns.forEach(addOn -> {
            Context context = new ContextImplementation(addOn, main, Level.DEBUG.name());
            submit(() -> addOn.initAddOn(context));
        });
    }

    /**
     * This method searches all the "/lib"-directory for AddOns and adds them to the addOnList
     * @return the retrieved addOns
     */
    private List<AddOnModel> loadAddOns() {
        debug("searching for addons in: " + getMain().getFileSystemManager().getLibLocation());
        PluginManager pluginManager = new DefaultPluginManager(getMain().getFileSystemManager().getLibLocation());
        // load the plugins
        debug("loading plugins");
        pluginManager.loadPlugins();
        debug("loaded: " + pluginManager.getPlugins().toString());

        // start (active/resolved) the plugins
        try {
            debug("starting plugins");
            pluginManager.startPlugins();
        } catch (Exception | NoClassDefFoundError e) {
            error("Error while trying to start the PF4J-Plugins", e);
        }
        try {
            debug("retrieving addons from the plugins");
            List<AddOnModel> addOns = pluginManager.getExtensions(AddOnModel.class);
            debug("retrieved: " + addOns.toString());
            addOns.stream()
                    .filter(addOn -> addOn.getClass().getClassLoader() instanceof IzouPluginClassLoader)
                    .forEach(addOn -> {
                        IzouPluginClassLoader izouPluginClassLoader = (IzouPluginClassLoader) addOn.getClass().getClassLoader();
                        PluginWrapper plugin = pluginManager.getPlugin(izouPluginClassLoader.getPluginDescriptor().getPluginId());
                        addOn.setPlugin(plugin);
                        pluginWrappers.put(addOn, plugin);
                    });
            return addOns;
        } catch (Exception e) {
            log.fatal("Error while trying to start the AddOns", e);
            return new ArrayList<>();
        }
    }

    /**
     * returns the addOn loaded from the ClassLoader
     * @param classLoader the classLoader
     * @return the (optional) AddOnModel
     */
    public Optional<AddOnModel> getAddOnForClassLoader(ClassLoader classLoader) {
        return addOns.stream()
                .filter(addOnModel -> addOnModel.getClass().getClassLoader().equals(classLoader))
                .findFirst();
    }

    /**
     * returns the (optional) PluginWrapper for the AddonModel.
     * If the return is empty, it means that the AddOn was not loaded through pf4j
     * @param addOnModel the AddOnModel
     * @return the PluginWrapper if loaded through pf4j or empty if added as an argument
     */
    public Optional<PluginWrapper> getPluginWrapper(AddOnModel addOnModel) {
        return Optional.of(pluginWrappers.get(addOnModel));
    }

    /**
     * checks whether the AddOn was loaded through pf4j
     * @param addOnModel the AddOnModel to check
     * @return true if loaded, false if not
     */
    public boolean loadedThroughPF4J (AddOnModel addOnModel) {
        return pluginWrappers.get(addOnModel) != null;
    }
}
