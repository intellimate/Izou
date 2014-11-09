package intellimate.izou.addon;

import ro.fortsoft.pf4j.Plugin;
import ro.fortsoft.pf4j.PluginWrapper;

/**
 * Created by LeanderK on 09/11/14.
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
