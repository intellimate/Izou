package intellimate.izou;

import intellimate.izou.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * The base class for each IzouModule
 * @author Leander Kurscheidt
 * @version 1.0
 */
public abstract class IzouModule implements MainProvider {
    protected Main main;
    protected final Logger log = LogManager.getLogger(this.getClass());

    public IzouModule(Main main) {
        this.main = main;
    }

    /**
     * returns the instance of Main
     *
     * @return Main
     */
    @Override
    public Main getMain() {
        return main;
    }

    /**
     * Used to log messages at debug level
     *
     * @param msg the message
     * @param e   the Throwable
     */
    @Override
    public void debug(String msg, Throwable e) {
        log.debug(msg, e);
    }

    /**
     * Used to log messages at debug level
     *
     * @param msg the message
     */
    @Override
    public void debug(String msg) {
        log.debug(msg);
    }

    /**
     * Used to log messages at error level
     *
     * @param msg the message
     * @param e   the Throwable
     */
    @Override
    public void error(String msg, Throwable e) {
        log.error(msg, e);
    }

    /**
     * Used to log messages at error level
     *
     * @param msg the message
     */
    @Override
    public void error(String msg) {
        log.error(msg);
    }
}
