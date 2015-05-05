package org.intellimate.izou.system;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * The SystemInitializer does any system wide initialization before Izou is started. An example of this would be setting
 * system properties relevant to izou etc.
 */
public class SystemInitializer {
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * Creates a new SystemInitializer object
     */
    public SystemInitializer() {

    }

    /**
     * Initializes the system by calling all init methods
     */
    public void initSystem() {
        setSystemProperties();
    }

    /**
     * Sets all system properties relevant for Izou
     */
    private void setSystemProperties() {
        setLocalHostProperty();
    }

    /**
     * Gets the local host if it can, and sets it as a system property
     */
    private void setLocalHostProperty() {
        String hostName = "unkown";
        try {
            hostName = InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            fileLogger.error("Unable to resolve hostname, setting hostname as 'unkown'");
        }

        System.setProperty("host.name", hostName);
    }
}
