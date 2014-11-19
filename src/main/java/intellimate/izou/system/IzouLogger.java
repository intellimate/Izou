package intellimate.izou.system;
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

public class IzouLogger {
    private static final Logger rootLogger = LogManager.getRootLogger();
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    public IzouLogger() {
    }

    public synchronized ExtendedLogger createFileLogger(String addOnName, String level) {
        try {
            LoggerContext ctx = LogManager.getContext(false);
            Configuration config = ((org.apache.logging.log4j.core.LoggerContext) ctx).getConfiguration();

            //
            Layout layout = PatternLayout.createLayout("%d %-5p [%t] %C{10} (%F:%L) - %m%n", config, null, null, true,
                    false, null, null);

            //
            Appender fileAppender = FileAppender.createAppender("logs" + File.separator + addOnName + ".log", "true", "false", "file", "true",
                    "false", "false", "4000", layout, null, "false", null, config);
            fileAppender.start();
            config.addAppender(fileAppender);

            //
            Appender consoleAppender = ConsoleAppender.createAppender(layout, null, "SYSTEM_OUT", "console", null, null);
            consoleAppender.start();
            config.addAppender(consoleAppender);

            //
            AppenderRef fileRef = AppenderRef.createAppenderRef("file", null, null);
            AppenderRef consoleRef = AppenderRef.createAppenderRef("console", null, null);
            AppenderRef[] refs = new AppenderRef[]{fileRef, consoleRef};

            //
            LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.toLevel(level), "org.apache.logging.log4j",
                    "true", refs, null, config, null);
            loggerConfig.addAppender(fileAppender, null, null);
            loggerConfig.addAppender(consoleAppender, null, null);

            //
            config.addLogger("org.apache.logging.log4j", loggerConfig);
            ((org.apache.logging.log4j.core.LoggerContext) ctx).updateLoggers();
            ExtendedLogger logger = ctx.getLogger("org.apache.logging.log4j");
            return logger;
        } catch(Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public synchronized static void consoleTrace(String info) {
        rootLogger.trace(info);
    }

    public synchronized static void consoleDebug(String info) {
        rootLogger.debug(info);
    }

    public synchronized static void consoleInfo(String info) {
        rootLogger.info(info);
    }

    public synchronized static void consoleWarn(String info) {
        rootLogger.warn(info);
    }

    public synchronized static void consoleError(String info) {
        rootLogger.error(info);
    }

    public synchronized static void consoleFatal(String info) {
        rootLogger.fatal(info);
    }
}
