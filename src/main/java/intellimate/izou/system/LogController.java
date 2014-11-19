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

public class LogController {
    private static final Logger rootLogger = LogManager.getRootLogger();
    //private static final Logger fileLogger = LogManager.getLogger("intellimate.izou");

    public LogController() {

    }

    public static synchronized ExtendedLogger createFileLogger(String addOnName, String level) {
        try {
            LoggerContext ctx = LogManager.getContext(false);
            Configuration config = ((org.apache.logging.log4j.core.LoggerContext) ctx).getConfiguration();

            //
            Layout layout = PatternLayout.createLayout("%d %-5p [%t] %C{10} (%F:%L) - %m%n", config, null, null, true,
                    false, null, null);

            //
            Appender fileAppender = FileAppender.createAppender("logs" + File.separator + addOnName + ".log", "true", "false", "File", "true",
                    "false", "false", "4000", layout, null, "false", null, config);
            fileAppender.start();
            config.addAppender(fileAppender);

            //
            Appender consoleAppender = ConsoleAppender.createAppender(layout, null, "SYSTEM_OUT", "console", null, null);
            consoleAppender.start();
            config.addAppender(consoleAppender);

            //
            AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
            AppenderRef[] refs = new AppenderRef[]{ref};

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

    public static synchronized ExtendedLogger createIzouFileLogger(String level) {
        LoggerContext ctx = LogManager.getContext(false);
        Configuration config = ((org.apache.logging.log4j.core.LoggerContext) ctx).getConfiguration();

        //
        Layout layout = PatternLayout.createLayout("%d %-5p [%t] %C{10} (%F:%L) - %m%n", config, null, null, true,
                false, null, null);

        //
        Appender appender = FileAppender.createAppender("logs" + File.separator + "izou.log", "true", "false", "File", "true",
                "false", "false", "4000", layout, null, "false", null, config);
        appender.start();
        config.addAppender(appender);

        //
        Appender consoleAppender = ConsoleAppender.createAppender(layout, null, "SYSTEM_OUT", "console", null, null);
        consoleAppender.start();
        config.addAppender(consoleAppender);

        //
        AppenderRef ref = AppenderRef.createAppenderRef("File", null, null);
        AppenderRef[] refs = new AppenderRef[] {ref};

        //
        LoggerConfig loggerConfig = LoggerConfig.createLogger("false", Level.toLevel(level), "org.apache.logging.log4j",
                "true", refs, null, config, null );
        loggerConfig.addAppender(appender, null, null);

        //
        config.addLogger("org.apache.logging.log4j", loggerConfig);
        ((org.apache.logging.log4j.core.LoggerContext) ctx).updateLoggers();
        ExtendedLogger logger = ctx.getLogger("org.apache.logging.log4j");
        return logger;
    }

    public synchronized static void logToConsole(String info, String level) {
        switch (level.toLowerCase()) {
            case "trace":
                rootLogger.trace(info);
                break;
            case "debug":
                rootLogger.debug(info);
                break;
            case "info":
                rootLogger.info(info);
                break;
            case "warn":
                rootLogger.warn(info);
                break;
            case "error":
                rootLogger.error(info);
                break;
            case "fatal":
                rootLogger.fatal(info);
                break;
        }
    }

    public static void main(String[] args) {
        //rootLogger.info("root logger");
        //fileLogger.warn("izou file logger");

        ExtendedLogger logger1 = createFileLogger("test", "fatal");
        ExtendedLogger logger2 = createIzouFileLogger(("error"));

        logger1.debug("static file logger");
        logger2.debug("dynamic file logger");

        LogController.logToConsole("console logger", "info");
    }
}
