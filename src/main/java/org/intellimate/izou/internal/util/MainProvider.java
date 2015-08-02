package org.intellimate.izou.internal.util;

import org.intellimate.izou.internal.main.Main;

/**
 * This interface provides an instance of main
 *
 * @author Leander Kurscheidt
 * @version 1.0
 */
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
}
