package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.system.file.FileSystemManager;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

/**
 * The SecurityBreachHandler takes action when a security exception is thrown in the security manager to deal with the
 * attempted security breach.
 */
public class SecurityBreachHandler {
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
            exists = true;
            return new SecurityBreachHandler(toAddress);
        }

        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    private SecurityBreachHandler(String toAddress) {
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
        sendMail(toAddress, subject, content, logName, logAttachment);
    }

    private String generateContent(Exception excep, Class[] classesStack) {
        String intro = "An attempted security breach was discovered in Izou: \n\n\n";
        String exception = "EXCEPTION: \n\n";
        exception += excep.getMessage();
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

    private void sendMail(String toAddress, String subject, String content, String attachmentName,
                          String attachmentPath) {
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        logger.debug("Sending error report...");
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.transport.protocol", "smtp");
        final String username = "intellimate.izou@gmail.com";
        final String password = "Karlskrone"; // TODO: hide this when password stuff is done

        try{
            Session session = Session.getDefaultInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            message.setSubject(subject);

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Now set the actual message
            messageBodyPart.setText(content);

            // Create a multipart message
            Multipart multipart = new MimeMultipart();

            // Set text message part
            multipart.addBodyPart(messageBodyPart);

            // Part two is attachment
            messageBodyPart = new MimeBodyPart();
            DataSource source = new FileDataSource(attachmentPath);
            messageBodyPart.setDataHandler(new DataHandler(source));
            messageBodyPart.setFileName(attachmentName);
            multipart.addBodyPart(messageBodyPart);

            // Send the complete message parts
            message.setContent(multipart);

            // Send message
            Transport.send(message);

            logger.debug("Error report sent successfully.");
        } catch (MessagingException e) {
            logger.error("Unable to send error report.", e);
        }
    }

    /**
     * Sends a mail to the address of {@code toAddress} with a subject of {@code subject} and a content of
     * {@code content} WITHOUT an attachment
     *
     * @param toAddress
     * @param subject
     * @param content
     */
    private void sendMail(String toAddress, String subject, String content) {
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        logger.debug("Sending error report...");
        Properties props = System.getProperties();
        props.setProperty("mail.smtp.host", "smtp.gmail.com");
        props.setProperty("mail.smtp.socketFactory.class", SSL_FACTORY);
        props.setProperty("mail.smtp.socketFactory.fallback", "false");
        props.setProperty("mail.smtp.port", "465");
        props.setProperty("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.auth", "true");
        props.put("mail.debug", "true");
        props.put("mail.store.protocol", "pop3");
        props.put("mail.transport.protocol", "smtp");
        final String username = "intellimate.izou@gmail.com";
        final String password = "Karlskrone"; // TODO: hide this when password stuff is done

        try{
            Session session = Session.getDefaultInstance(props, new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(toAddress));
            message.setSubject(subject);
            message.setText(content);

            Transport.send(message);
            logger.debug("Error report sent successfully.");
        } catch (MessagingException e) {
            logger.error("Unable to send error report.", e);
        }
    }

}
