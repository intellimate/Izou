package org.intellimate.izou.system;

import org.intellimate.izou.addon.AddOnModel;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.intellimate.izou.system.context.*;
import org.intellimate.izou.system.context.System;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface Context {
    /**
     * returns the API used for interaction with Events
     * @return Events
     */
    Events getEvents();

    /**
     * returns the API used for interaction with Resource
     * @return Resource
     */
    Resources getResources();

    /**
     * returns the API used for interaction with Files
     * @return Files
     */
    Files getFiles();

    /**
     * returns the API used to log
     * @return Logger
     */
    ExtendedLogger getLogger();

    /**
     * returns the API used to manage the ThreadPool
     * @return ThreadPool
     */
    ThreadPool getThreadPool();

    /**
     * returns the API to manage the Activators
     * @return Activator
     */
    Activators getActivators();

    /**
     * returns the API used to manage the OutputPlugins and OutputExtensions
     * @return Output
     */
    Output getOutput();

    /**
     * retruns the API used to interact with Izou.
     * @return System.
     */
    System getSystem();

    /**
     * gets addOn
     *
     * @return the addOn
     */
    AddOnModel getAddOn();
}
