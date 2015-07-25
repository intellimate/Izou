package org.intellimate.izou.system;

import org.intellimate.izou.addon.AddOnModel;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.intellimate.izou.system.context.*;
import org.intellimate.izou.system.context.System;

/**
 * The Izou context is a means for all addOns to get general information they might need. Every addOn its own context
 * and can use it to reach certain Izou components. It controls what an addOn has access to and what it does not have
 * access to.
 * <br>
 * For instance, the addOn should have access to a logger (created in Izou for the addOn), but it should not have
 * access to classes like the AddOnManager, which loads all addOns at the start. Hence the logger is included in the
 * context, but the addOn manager is not. Anything that is not included in the context, and addOn does not have access to.
 * So in short, the context exists to give addOns access to higher Izou components while still denying access to other
 * components.
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
