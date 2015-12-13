package org.intellimate.izou.system.logger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.spi.ExtendedLogger;
import org.apache.logging.log4j.spi.LoggerContext;

import java.io.File;

/**
 * Class that manages logging in izou. Can create a new logger for every addOn
 */
public class IzouLogger {
    private static final Logger rootLogger = LogManager.getRootLogger();
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * Creates a new file-logger for an addOn. The logger will log to a file with the addOnId as name in the logs folder
     * of Izou
     *
     * @param addOnId the Id of the addOn the logger is created for
     * @param level level of logger (at what level of log the logger should be activated
     * @return the new logger
     */
    public synchronized ExtendedLogger createFileLogger(String addOnId, String level) {
        try {
            LoggerContext ctx = LogManager.getContext(false);
            Configuration config = ((org.apache.logging.log4j.core.LoggerContext) ctx).getConfiguration();

            //creates a new pattern layout (what determines how the log is formated, i.e. date, thread etc.)
            Layout layout = PatternLayout.createLayout("%d %-5p [%t] %C{10} (%F:%L) - %m%n", config, null, null, true,
                    false, null, null);

            //creates a file appender for the logger (so that it knows what file to log to)
            Appender fileAppender = FileAppender.createAppender("logs" + File.separator + addOnId + ".log", "true",
                    "false", "file", "true", "false", "false", "4000", layout, null, "false", null, config);
            fileAppender.start();
            config.addAppender(fileAppender);

            //creates also a console appender for the logger (so that the logger also outputs the log in the console)
            Appender consoleAppender = ConsoleAppender.createAppender(layout, null, "SYSTEM_OUT", "console", null, null);
            consoleAppender.start();
            config.addAppender(consoleAppender);

            //adds appenders to an array called refs. It will later serve as references to the logger as to what
            // appenders it has
            AppenderRef fileRef = AppenderRef.createAppenderRef("file", Level.DEBUG, null);
            AppenderRef consoleRef = AppenderRef.createAppenderRef("console", Level.DEBUG, null);
            AppenderRef[] refs = new AppenderRef[]{fileRef, consoleRef};

            //creates the logger configurations for the logger, where the appender-references are also added
            LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.DEBUG, addOnId,
                    "true", refs, null, config, null);
            loggerConfig.addAppender(fileAppender, Level.DEBUG, null);
            loggerConfig.addAppender(consoleAppender, Level.DEBUG, null);

            //finally creates the logger and returns it
            config.addLogger(addOnId, loggerConfig);
            ((org.apache.logging.log4j.core.LoggerContext) ctx).updateLoggers();
            ctx.getLogger(addOnId);
            ExtendedLogger logger = ctx.getLogger(addOnId);
            return logger;
        } catch(Exception e) {
            fileLogger.error("Unable to create FileLogger",e);
            return null;
        }
    }
}
