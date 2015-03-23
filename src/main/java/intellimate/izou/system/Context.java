package intellimate.izou.system;

import intellimate.izou.addon.AddOnModel;
import intellimate.izou.system.context.*;
import org.apache.logging.log4j.spi.ExtendedLogger;

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
     * gets addOn
     *
     * @return the addOn
     */
    AddOnModel getAddOn();
}
