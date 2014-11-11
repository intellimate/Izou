package intellimate.izou.addon;

import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * This class must be extended for an AddOn to work properly.
 * It is used to identify the zip Files as candidates for AddOns
 */
public abstract class ZipFileManager extends Plugin{
    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper
     */
    public ZipFileManager(PluginWrapper wrapper) {
        super(wrapper);
    }
}
