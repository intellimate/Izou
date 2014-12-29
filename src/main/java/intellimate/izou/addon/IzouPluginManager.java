package intellimate.izou.addon;

import ro.fortsoft.pf4j.DefaultPluginManager;
import ro.fortsoft.pf4j.ExtensionFactory;
import ro.fortsoft.pf4j.ExtensionFinder;

import java.io.File;

/**
 * @author LeanderK
 * @version 1.0
 */
public class IzouPluginManager extends DefaultPluginManager{
    private ExtensionFactory extensionFactory;
    /**
     * The plugins directory is supplied by System.getProperty("pf4j.pluginsDir", "plugins").
     */
    public IzouPluginManager() {
    }

    /**
     * Constructs DefaultPluginManager which the given plugins directory.
     *
     * @param pluginsDirectory the directory to search for plugins
     */
    public IzouPluginManager(File pluginsDirectory) {
        super(pluginsDirectory);
    }

    /**
     * Add the possibility to override the ExtensionFactory.
     */
    @Override
    protected ExtensionFactory createExtensionFactory() {
        extensionFactory = super.createExtensionFactory();
        return extensionFactory;
    }

    /**
     * Add the possibility to override the ExtensionFinder.
     */
    @Override
    protected ExtensionFinder createExtensionFinder() {
        IzouExtensionFinder extensionFinder = new IzouExtensionFinder(this, extensionFactory);
        addPluginStateListener(extensionFinder);

        return extensionFinder;
    }
}
