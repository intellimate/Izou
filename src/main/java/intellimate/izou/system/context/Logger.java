package intellimate.izou.system.context;

import org.apache.logging.log4j.spi.ExtendedLogger;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface Logger {
    /**
     * gets logger for addOn
     *
     * @return the logger
     */
    ExtendedLogger getLogger();
}
