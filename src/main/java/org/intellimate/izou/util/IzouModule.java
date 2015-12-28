package org.intellimate.izou.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.spi.AbstractLogger;
import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.IdentificationManager;
import org.intellimate.izou.main.Main;

/**
 * The base class for each IzouModule
 * FIXME: What is an izou module exactly?
 *
 * @author Leander Kurscheidt
 * @version 1.0
 */
public abstract class IzouModule implements MainProvider, Identifiable {
    private static final String FQCN = IzouModule.class.getName();
    private String ID = this.getClass().getCanonicalName();
    protected Main main;
    protected final AbstractLogger log = (AbstractLogger) LogManager.getLogger(this.getClass());

    public IzouModule(Main main) {
        this(main, true);
    }

    public IzouModule(Main main, boolean register) {
        this.main = main;
        if (register) {
            if (!IdentificationManager.getInstance().registerIdentification(this)) {
                log.fatal("unable to register! " + getClass().getCanonicalName());
            }
        }
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
        log.logIfEnabled(FQCN, Level.DEBUG, null, msg, e);
    }

    /**
     * Used to log messages at debug level
     *
     * @param msg the message
     */
    @Override
    public void debug(String msg) {
        log.logIfEnabled(FQCN, Level.DEBUG, null, msg, (Object) null);
    }

    /**
     * Used to log messages at error level
     *
     * @param msg the message
     * @param e   the Throwable
     */
    @Override
    public void error(String msg, Throwable e) {
        log.logIfEnabled(FQCN, Level.ERROR, null, msg, e);
    }

    /**
     * Used to log messages at error level
     *
     * @param msg the message
     */
    @Override
    public void error(String msg) {
        log.logIfEnabled(FQCN, Level.ERROR, null, msg, (Object) null);
    }

    /**
     * Used to log messages at fatal level
     *
     * @param msg the message
     */
    @Override
    public void fatal(String msg) {
        log.logIfEnabled(FQCN, Level.FATAL, null, msg, (Object) null);
    }

    /**
     * Used to log messages at fatal level
     *
     * @param msg the message
     * @param e   the Throwable
     */
    @Override
    public void fatal(String msg, Throwable e) {
        log.logIfEnabled(FQCN, Level.FATAL, null, msg, e);
    }

    /**
     * An ID must always be unique.
     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
     * If you have to implement this interface multiple times, just concatenate unique Strings to
     * .class.getCanonicalName()
     *
     * @return A String containing an ID
     */
    @Override
    public String getID() {
        return ID;
    }
}
