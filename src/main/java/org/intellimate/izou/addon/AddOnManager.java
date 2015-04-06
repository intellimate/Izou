package org.intellimate.izou.addon;

import org.intellimate.izou.AddonThreadPoolUser;
import org.intellimate.izou.IdentifiableSet;
import org.intellimate.izou.IzouModule;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.system.Context;
import org.intellimate.izou.system.context.ContextImplementation;
import org.apache.logging.log4j.Level;
import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.IzouPluginClassLoader;
import ro.fortsoft.pf4j.PluginManager;
import ro.fortsoft.pf4j.PluginWrapper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Manages all the AddOns.
 */
public class AddOnManager extends IzouModule implements AddonThreadPoolUser {
    private IdentifiableSet<AddOnModel> addOns = new IdentifiableSet<>();
    
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
            addOn.initAddOn(context);
        });
    }

    /**
     * This method searches all the "/lib"-directory for AddOns and adds them to the addOnList
     * @return the retrieved addOns
     */
    private List<AddOnModel> loadAddOns() {
        File libFile;
        try {
            String libPath = new File(".").getCanonicalPath() + File.separator + "lib";
            libFile = new File(libPath);
        } catch (IOException e) {
            error("Error while trying to get the lib-directory" + e);
            return new ArrayList<>();
        }

        PluginManager pluginManager = new DefaultPluginManager(libFile);
        // load the plugins
        pluginManager.loadPlugins();

        // start (active/resolved) the plugins
        try {
            pluginManager.startPlugins();
        } catch (Exception e) {
            error("Error while trying to start the PF4J-Plugins", e);
        }
        try {
            List<AddOnModel> addOns = pluginManager.getExtensions(AddOnModel.class);
            addOns.stream()
                    .filter(addOn -> addOn.getClass().getClassLoader() instanceof IzouPluginClassLoader)
                    .forEach(addOn -> {
                        IzouPluginClassLoader izouPluginClassLoader = (IzouPluginClassLoader) addOn.getClass().getClassLoader();
                        PluginWrapper plugin = pluginManager.getPlugin(izouPluginClassLoader.getPluginDescriptor().getPluginId());
                        addOn.setPlugin(plugin);
                    });
            return addOns;
        } catch (Exception e) {
            log.fatal("Error while trying to start the AddOns", e);
            return new ArrayList<>();
        }
    }
}
