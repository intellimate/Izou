package intellimate.izouSDK.addon;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * This class must be extended for an AddOn to work properly.
 * It is used to identify the zip Files as candidates for AddOns
 */
public abstract class ZipFileManager extends Plugin{
    private final Logger fileLogger = LogManager.getLogger(this.getClass());


    /**
     * Constructor to be used by plugin manager for plugin instantiation.
     * Your plugins have to provide constructor with this exact signature to
     * be successfully loaded by manager.
     *
     * @param wrapper the PluginWrapper to assign the ZipFileManager to
     */
    public ZipFileManager(PluginWrapper wrapper) {
        super(wrapper);
    }
}
