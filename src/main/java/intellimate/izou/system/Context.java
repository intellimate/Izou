package intellimate.izou.system;

import intellimate.izou.addon.AddOn;
import intellimate.izou.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * This class provides much of the general Communication with Izou.
 */
public class Context {
    private final AddOn addOn;
    private final Main main;
    private final ExtendedLogger logger;
    private final FileManager fileManager;

    /**
     * creates a new context for the addOn
     *
     * A context contains all the "global" or generally necessary information an addOn might need that it otherwise does
     * not have access too
     *
     * @param addOn the addOn for which to create a new context
     * @param main instance of main
     */
    public Context(AddOn addOn, Main main, String addOnName, String logLevel) {
        this.addOn = addOn;
        this.main = main;
        this.fileManager = main.getFileManager();

        IzouLogger izouLogger = main.getIzouLogger();
        if (izouLogger != null)
            this.logger = izouLogger.createFileLogger(addOnName, logLevel);
        else {
            Logger fileLogger = LogManager.getLogger(this.getClass());
            fileLogger.error("IzouLogger has not been initialized");
            throw new NullPointerException("IzouLogger has not been initialized");
        }
    }

    /**
     * gets addOn
     *
     * @return the addOn
     */
    public AddOn getAddOn() {
        return addOn;
    }

    /**
     * gets instance of main
     *
     * @return instance of main
     */
    public Main getMain() {
        return main;
    }

    /**
     * gets logger for addOn
     *
     * @return the logger
     */
    public ExtendedLogger getLogger() {
        return logger;
    }
}
