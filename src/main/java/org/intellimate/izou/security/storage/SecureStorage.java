package org.intellimate.izou.security.storage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.intellimate.izou.security.SecurityModule;
import org.intellimate.izou.system.file.FileSystemManager;
import ro.fortsoft.pf4j.PluginDescriptor;

import javax.crypto.SecretKey;
import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.HashMap;

/**
 * The SecureStorage class offers a way for addOns to store data so that other addOns cannot access it. For example if
 * addOn A wants to store the users username and password to some service, it can do so using this class without any
 * other addOn having access to that information.
 * <p>
 *     While the stored information is encrypted, it is not safe from the user. In theory and with a lot of effort, the
 *     user could extract the keys and decrypt the stored information since the keys are not hidden from the user (only
 *     from addOns).
 * </p>
 */
public final class SecureStorage {
    private static boolean exists = false;
    private HashMap<String, SecureContainer> containers;
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Creates an SecureStorage. There can only be one single SecureStorage, so calling this method twice
     * will cause an illegal access exception.
     *
     * @return a SecureAccess object
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public static SecureStorage createSecureStorage() throws IllegalAccessException {
        if (!exists) {
            SecureStorage secureStorage = new SecureStorage();
            exists = true;
            return secureStorage;
        }

        throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
    }

    /**
     * Creates a new SecureStorage instance if and only if none has been created yet
     *
     * @throws IllegalAccessException thrown if this method is called more than once
     */
    public SecureStorage() throws IllegalAccessException, NullPointerException {
        if (exists) {
            throw new IllegalAccessException("Cannot create more than one instance of IzouSecurityManager");
        }

        SecretKey key = retrieveKey();
        if (key == null) {
            SecurityModule securityModule = new SecurityModule();
            key = securityModule.generateKey();

            if (key != null) {
                storeKey(key);
            } else {
                throw new NullPointerException("Unable to create security key");
            }
        }

        containers = retrieveContainers();
        if (containers == null) {
            containers = new HashMap<>();
        }
    }

    /**
     * Stores a {@link SecureContainer} with the given secure ID of the plugin descriptor. Each addOn can only have 1
     * secure container, so in order to update it, retrieve it and store it again.
     *
     * @param descriptor The plugin descriptor belonging to an addOn
     * @param container The secure container to be stored with an addOn
     */
    public void store(PluginDescriptor descriptor, SecureContainer container) {
        HashMap<String, String> clearTextData = container.getClearTextData();
        HashMap<byte[], byte[]> cryptData = container.getCryptData();

        SecretKey secretKey = retrieveKey();
        SecurityModule module = new SecurityModule();
        for (byte[] key : cryptData.keySet()) {
            clearTextData.put(module.decryptAES(key, secretKey), module.decryptAES(cryptData.get(key), secretKey));
            cryptData.remove(key);
        }

        container.setCryptData(cryptData);
        //TODO reactivate
        //containers.put(descriptor.getSecureID(), container);
        saveContainers();
    }

    /**
     * Retrieves a {@link SecureContainer} with the given secure ID of the plugin descriptor
     *
     * @param descriptor The plugin descriptor belonging to an addOn
     * @return  container The secure container that was retrieved
     */
    public SecureContainer retrieve(PluginDescriptor descriptor) {
        //TODO reactivate
        /*SecureContainer container = containers.get(descriptor.getSecureID());
        HashMap<byte[], byte[]> cryptData = container.getCryptData();
        HashMap<String, String> clearTextData = container.getClearTextData();

        SecretKey secretKey = retrieveKey();
        SecurityModule module = new SecurityModule();
        for (byte[] key : cryptData.keySet()) {
            clearTextData.put(module.decryptAES(key, secretKey), module.decryptAES(cryptData.get(key), secretKey));
            cryptData.remove(key);
        }

        container.setClearTextData(clearTextData);
        return container;
        */
        return null;
    }

    /**
     * Saves the containers to ./system/data/containers.ser
     */
    private void saveContainers() {
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
        final String containerFile = workingDir + File.separator + "system" + File.separator + "data" + File.separator
                + "containers.ser";
        try {
            FileOutputStream fileOut = new FileOutputStream(containerFile);
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(containers);
            out.close();
            fileOut.close();
        } catch(IOException e) {
            logger.error("Unable to save containers to file", e);
        }
    }

    /**
     * Retrieves the containers from ./system/data/containers.ser if the file is found, else returns null
     *
     * @return the containers from ./system/data/containers.ser if the file is found, else null
     */
    private HashMap<String, SecureContainer> retrieveContainers() {
        HashMap<String, SecureContainer> containers = null;
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
        final String containerFile = workingDir + File.separator + "system" + File.separator + "data" + File.separator
                + "containers.ser";
        try {
            FileInputStream fileIn = new FileInputStream(containerFile);
            ObjectInputStream in = new ObjectInputStream(fileIn);
            Object o = in.readObject();
            if (o instanceof HashMap) {
                containers = (HashMap) o;
            }
            in.close();
            fileIn.close();
        } catch (FileNotFoundException e) {
            return null;
        } catch(IOException | ClassNotFoundException e) {
            logger.error("Unable to retrieve containers from file", e);
        }

        return containers;
    }

    /**
     * Retrieves the izou aes key stored in a keystore
     *
     * @return the izou aes key stored in a keystore
     */
    private SecretKey retrieveKey() {
        SecretKey key = null;
        try {
            String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
            final String keyStoreFile = workingDir + File.separator + "system" + File.separator + "izou.keystore";
            KeyStore keyStore = createKeyStore(keyStoreFile, "4b[X:+H4CS&avY<)");

            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection("Ev45j>eP}QTR?K9_".toCharArray());
            KeyStore.Entry entry = keyStore.getEntry("izou_key", keyPassword);
            key = ((KeyStore.SecretKeyEntry) entry).getSecretKey();
        } catch(NullPointerException e) {
            return null;
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException e) {
            logger.error("Unable to retrieve key", e);
        }

        return key;
    }

    /**
     * Stores the izou aes key in a keystore
     *
     * @param key the key to store
     */
    private void storeKey(SecretKey key) {
        String workingDir = FileSystemManager.FULL_WORKING_DIRECTORY;
        final String keyStoreFile = workingDir + File.separator + "system" + File.separator + "izou.keystore";
        KeyStore keyStore = createKeyStore(keyStoreFile, "4b[X:+H4CS&avY<)");


        try {
            KeyStore.SecretKeyEntry keyStoreEntry = new KeyStore.SecretKeyEntry(key);
            KeyStore.PasswordProtection keyPassword = new KeyStore.PasswordProtection("Ev45j>eP}QTR?K9_".toCharArray());
            keyStore.setEntry("izou_key", keyStoreEntry, keyPassword);
            keyStore.store(new FileOutputStream(keyStoreFile), "4b[X:+H4CS&avY<)".toCharArray());
        } catch (NoSuchAlgorithmException | KeyStoreException
                | CertificateException | IOException e) {
            logger.error("Unable to store key", e);
        }
    }

    /**
     * Creates a new keystore for the izou aes key
     *
     * @param fileName the path to the keystore
     * @param password the password to use with the keystore
     * @return the newly created keystore
     */
    private KeyStore createKeyStore(String fileName, String password)  {
        File file = new File(fileName);
        KeyStore keyStore = null;
        try {
            keyStore = KeyStore.getInstance("JCEKS");
            if (file.exists()) {
                keyStore.load(new FileInputStream(file), password.toCharArray());
            } else {
                keyStore.load(null, null);
                keyStore.store(new FileOutputStream(fileName), password.toCharArray());
            }
        } catch (CertificateException | IOException | KeyStoreException | NoSuchAlgorithmException e) {
            logger.error("Unable to create key store", e);
        }

        return keyStore;
    }
}
