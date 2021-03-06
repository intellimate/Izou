package org.intellimate.izou.util;

import org.intellimate.izou.main.Main;
import ro.fortsoft.pf4j.AddonAccessible;

/**
 * This interface provides an instance of main
 *
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface MainProvider {
    /**
     * returns the instance of Main
     * @return Main
     */
    Main getMain();

    /**
     * Used to log messages at debug level
     * @param msg the message
     * @param e the Throwable
     */
    void debug(String msg, Throwable e);

    /**
     * Used to log messages at debug level
     * @param msg the message
     */
    void debug(String msg);

    /**
     * Used to log messages at error level
     * @param msg the message
     * @param e the Throwable
     */
    void error(String msg, Throwable e);

    /**
     * Used to log messages at error level
     * @param msg the message
     */
    void error(String msg);

    /**
     * Used to log messages at fatal level
     * @param msg the message
     * @param e the Throwable
     */
    void fatal(String msg, Throwable e);

    /**
     * Used to log messages at fatal level
     * @param msg the message
     */
    void fatal(String msg);
}
