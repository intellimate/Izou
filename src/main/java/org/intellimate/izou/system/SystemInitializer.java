package org.intellimate.izou.system;

import org.intellimate.izou.main.Main;
import org.intellimate.izou.system.file.ReloadableFile;
import org.intellimate.izou.util.IzouModule;

import java.io.*;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.Properties;

/**
 * The SystemInitializer does any system wide initialization before Izou is started. An example of this would be setting
 * system properties relevant to izou etc.
 */
public class SystemInitializer extends IzouModule implements ReloadableFile {
    /**
     * The name of the Izou properties file
     */
    public static final String IZOU_PROPERTIES_FILE_NAME = "izou.properties";

    private static final String IZOU_CONFIGURED = "izou.configured";

    private Properties properties;
    private File propertiesFile;

    /**
     * Creates a new SystemInitializer object
     */
    public SystemInitializer(Main main) {
        super(main);
        properties = new Properties();
    }

    /**
     * Initializes the system by calling all init methods
     */
    public void initSystem() {
        createIzouPropertiesFile();
        setSystemProperties();
        reloadFile(null);

        try {
            if (System.getProperty(IZOU_CONFIGURED).equals("false")) {
                fatal("Izou not completely configured, please configure Izou and launch again.");
                System.exit(0);
            }
        } catch (NullPointerException e) {
            fatal("Izou not completely configured, please configure Izou by editing izou.properties in the " +
                    "properties folder and launch again.");
            System.exit(0);
        }
    }

    /**
     * This method registers the SystemInitializer with the Properties manager, but this can only be done once the
     * properties manager has been created, thus this method is called later.
     */
    public void registerWithPropertiesManager() {
        try {
            main.getFileManager().registerFileDir(propertiesFile.getParentFile().toPath(),
                    propertiesFile.getName(), this);
        } catch (IOException e) {
            error("Unable to register ");
        }
    }

    /**
     * Creates the propreties file for Izou. This is the file that has to be configured before Izou can run and it
     * contains some basic configuartion for Izou.
     */
    private void createIzouPropertiesFile() {
        String propertiesPath = getMain().getFileSystemManager().getPropertiesLocation() + File.separator
                + IZOU_PROPERTIES_FILE_NAME;

        propertiesFile = new File(propertiesPath);
        if (!propertiesFile.exists()) try (PrintWriter writer = new PrintWriter(propertiesFile.getAbsolutePath(), "UTF-8")) {
            propertiesFile.createNewFile();

            writer.println("# --------------------");
            writer.println("# Izou Properties File");
            writer.println("# --------------------");
            writer.println("#");
            writer.println("# This file has some general configuration options that have to be configured before");
            writer.println("# Izou can run successfully.");
        } catch (IOException e) {
            error("Error while trying to create the new Properties file", e);
        }
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
            error("Unable to resolve hostname, setting hostname as 'unkown'");
        }

        System.setProperty("host.name", hostName);
    }

    /**
     * Reload the properties from the properties file into the system properties
     */
    private void reloadProperties() {
        Properties temp = new Properties();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(propertiesFile), "UTF8"));
            temp.load(bufferedReader);
            this.properties = temp;
        } catch (IOException e) {
            error("Error while trying to load the Properties-File: "
                    + propertiesFile.getAbsolutePath(), e);
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    error("Unable to close input stream", e);
                }
            }
        }
    }

    @Override
    public void reloadFile(String eventType) {
        Enumeration propertyNames = properties.propertyNames();
        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String value = properties.getProperty(key);

            System.getProperties().remove(value);
        }

        reloadProperties();
        propertyNames = properties.propertyNames();

        boolean configured = true;

        while (propertyNames.hasMoreElements()) {
            String key = (String) propertyNames.nextElement();
            String value = properties.getProperty(key);

            if (key.equals(IZOU_CONFIGURED)) {
                continue;
            }

            if (value.equals("")) {
                configured = false;
            }

            System.setProperty(key, value);
        }

        if (configured) {
            System.setProperty(IZOU_CONFIGURED, "true");
        } else {
            System.setProperty(IZOU_CONFIGURED, "false");
        }
    }
}
