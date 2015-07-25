package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.support.SystemMail;
import org.intellimate.izou.util.IzouModule;

import java.io.File;

/**
 * The SecurityBreachHandler takes action when a security exception is thrown in the security manager to deal with the
 * attempted security breach.
 */
final class SecurityBreachHandler extends IzouModule {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String toAddress;
    private final SystemMail systemMail;
    private static boolean exists = false;

    /**
     * Creates a SecurityBreachHandler. There can only be one single SecurityBreachHandler, so calling this method twice
     * will cause an illegal access exception.
     *
     * @param main the main instance of izou
     * @param systemMail the system mail object in order to send e-mails to owner in case of emergency
     * @param toAddress the email address to send error reports to
     * @return an SecurityBreachHandler
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    static SecurityBreachHandler createBreachHandler(Main main, SystemMail systemMail, String toAddress)
            throws IllegalAccessException {
        if (!exists) {
            SecurityBreachHandler breachHandler = new SecurityBreachHandler(main, systemMail, toAddress);
            exists = true;
            return breachHandler;
        }

        throw new IllegalAccessException("Cannot create more than one instance of SecurityBreachHandler");
    }

    /**
     Creates a new SecurityBreachHandler instance if and only if none has been created yet
     *
     * @param main the main instance of izou
     * @param systemMail the system mail object in order to send e-mails to owner in case of emergency
     * @param toAddress the email address to send error reports to
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    private SecurityBreachHandler(Main main, SystemMail systemMail, String toAddress) throws IllegalAccessException {
        super(main);
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of SecurityBreachHandler");
        }
        this.systemMail = systemMail;
        this.toAddress = toAddress;
    }

    /**
     * Handles the potential security breach (sends out an email and does whatever else is necessary)
     *
     * @param e The exception that will be thrown
     * @param classesStack the current class stack
     */
    void handleBreach(Exception e, Class[] classesStack) {
        String subject = "Izou Security Exception: " + e.getMessage();
        sendErrorReport(subject, e, classesStack);
    }

    private void sendErrorReport(String subject, Exception e, Class[] classesStack) {
        String content = generateContent(e, classesStack);
        String logName = "org.intellimate.izou.log";
        String logAttachment = getMain().getFileSystemManager().getLogsLocation() + File.separator + logName;
        systemMail.sendMail(toAddress, subject, content, logName, logAttachment);
    }

    private String generateContent(Exception excep, Class[] classesStack) {
        String intro = "An attempted security breach was discovered in Izou: \n\n\n";
        String exception = "EXCEPTION: \n\n";
        exception += excep.getMessage() + "\n";
        StackTraceElement[] stackTraceElements = excep.getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            exception += element.toString() + "\n";
        }
        exception += "\n\n\n";

        String classesStackString = "";
        if (classesStack != null) {
            classesStackString = "CLASS STACK:\n\n";
            for (int i = 0;  i < classesStack.length; i++) {
                Class clazz = classesStack[i];
                classesStackString += "Class " + i + ": \n";
                classesStackString += clazz.toString() + "\n";
                classesStackString += "Class Loader " + i + ": \n";
                ClassLoader classLoader = clazz.getClassLoader();
                if (classLoader == null) {
                    classesStackString += "null\n";
                } else {
                    classesStackString += clazz.getClassLoader().toString() + "\n";
                }
            }
        }

        return intro + exception + classesStackString;
    }
}
