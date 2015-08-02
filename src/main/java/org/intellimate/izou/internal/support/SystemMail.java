package org.intellimate.izou.internal.support;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
 * The SystemMail class can be used to send emails from the Izou to preferably the owner of the Izou in order
 * to alert him about issues regarding Izou.
 * <p>
 *     It is meant to serve as an emergency communication, and not as a way to send general information. To do that you
 *     can use the available addOns.
 * </p>
 */
public class SystemMail {
    private static boolean exists = false;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Creates a SystemMail. There can only be one single SystemMail, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return a Mail object
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public static SystemMail createSystemMail() throws IllegalAccessException {
        if (!exists) {
            SystemMail permissionManager = new SystemMail();
            exists = true;
            return permissionManager;
        }

        throw new IllegalAccessException("Cannot create more than one instance of Mail");
    }

    /**
     * Creates a new SystemMail instance if and only if none has been created yet
     *
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    private SystemMail() throws IllegalAccessException {
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of Mail");
        }
    }

    /**
     * Sends a mail to the address of {@code toAddress} with a subject of {@code subject} and a content of
     * {@code content} with an attachment
     *
     * @param toAddress the address to send the mail to
     * @param subject the subject of the email to send
     * @param content the content of the email (without attachment)
     * @param attachmentName the name of the attachment
     * @param attachmentPath the file path to the attachment
     */
    public void sendMail(String toAddress, String subject, String content, String attachmentName,
                          String attachmentPath) {
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        logger.debug("Sending mail...");
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

            logger.debug("Mail sent successfully.");
        } catch (MessagingException e) {
            logger.error("Unable to send error report.", e);
        }
    }

    /**
     * Sends a mail to the address of {@code toAddress} with a subject of {@code subject} and a content of
     * {@code content} WITHOUT an attachment
     *
     * @param toAddress the address to send the mail to
     * @param subject the subject of the email to send
     * @param content the content of the email (without attachment)
     */
    public void sendMail(String toAddress, String subject, String content) {
        final String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";

        logger.debug("Sending mail...");
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
            logger.debug("Mail sent successfully.");
        } catch (MessagingException e) {
            logger.error("Unable to send mail.", e);
        }
    }
}
