package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import intellimate.izou.main.Main;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * This class provides much of the general Communication with Izou.
 */
public class Context {
    private AddOn addOn;
    private Main main;

    /**
     *
     *
     * @param addOn
     * @param main
     */
    public Context(AddOn addOn, Main main) {
        this.addOn = addOn;
        this.main = main;
    }

    /**
     * gets the file-logger for addOn-specific file
     *
     * @return the file-logger or null if logController is null
     */
    public synchronized ExtendedLogger getFileLogger(String addOnName, String level) {
        IzouLogger izouLogger = main.getIzouLogger();
        if(izouLogger != null)
            return izouLogger.createFileLogger(addOnName, level);
        else
            return null;
    }
}
