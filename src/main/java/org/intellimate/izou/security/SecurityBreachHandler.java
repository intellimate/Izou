package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.support.Mail;
import org.intellimate.izou.system.file.FileSystemManager;

/**
 * The SecurityBreachHandler takes action when a security exception is thrown in the security manager to deal with the
 * attempted security breach.
 */
class SecurityBreachHandler {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final String toAddress;
    private static boolean exists = false;

    /**
     * Creates a SecurityBreachHandler. There can only be one single SecurityBreachHandler, so calling this method twice
     * will cause an illegal access exception.
     *
     * @param toAddress the email address to send error reports to
     * @return an SecurityBreachHandler
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public static SecurityBreachHandler createBreachHandler(String toAddress) throws IllegalAccessException {
        if (!exists) {
            SecurityBreachHandler breachHandler = new SecurityBreachHandler(toAddress);
            exists = true;
            return breachHandler;
        }

        throw new IllegalAccessException("Cannot create more than one instance of SecurityBreachHandler");
    }

    private SecurityBreachHandler(String toAddress) throws IllegalAccessException {
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of SecurityBreachHandler");
        }
        this.toAddress = toAddress;
    }

    /**
     * Handles the potential security breach (sends out an email and does whatever else is necessary)
     *
     * @param e The exception that will be thrown
     * @param classesStack the current class stack
     */
    public void handleBreach(Exception e, Class[] classesStack) {
        String subject = "Izou Security Exception: " + e.getMessage();
        sendErrorReport(subject, e, classesStack);
    }

    private void sendErrorReport(String subject, Exception e, Class[] classesStack) {
        String content = generateContent(e, classesStack);
        String logName = "org.intellimate.izou.log";
        String logAttachment = FileSystemManager.LOG_PATH + logName;
        Mail mail = new Mail();
        mail.sendMail(toAddress, subject, content, logName, logAttachment);
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
