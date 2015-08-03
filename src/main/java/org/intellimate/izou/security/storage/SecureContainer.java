package org.intellimate.izou.security.storage;

import java.util.HashMap;

/**
 * @author LeanderK
 * @version 1.0
 */
public interface SecureContainer {
    /**
     * Encrypts and adds {@code key} and {@code value} to the stored data map
     *
     * @param key the key to save
     * @param value the value to save the key with
     */
    void securePut(String key, String value);

    /**
     * Retrieves and decrypts {@code key} and {@code value} and returns the decrypted value
     *
     * @param key the key to retrieve {@code value} with
     * @return the value if it was found and decrypted successfully, else null
     */
    String secureGet(String key);

    /**
     * Gets the clear text data array
     *
     * @return the clear text data array
     */
    HashMap<String, String> getClearTextData();

    /**
     * Sets the clear text data
     *
     * @param clearTextData the clear text data to set
     */
    void setClearTextData(HashMap<String, String> clearTextData);

    /**
     * Gets the crypt data array
     *
     * @return the crypt data array
     */
    HashMap<byte[], byte[]> getCryptData();

    /**
     * Sets the crypt data array
     *
     * @param cryptData the crypt data array to set
     */
    void setCryptData(HashMap<byte[], byte[]> cryptData);
}
