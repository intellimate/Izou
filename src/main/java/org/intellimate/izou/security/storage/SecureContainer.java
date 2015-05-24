package org.intellimate.izou.security.storage;

import java.io.Serializable;
import java.util.HashMap;

/**
 * The SecureContainer represents a way to store data safely without other addOns having access to it. However the user
 * does have access to it, theoretically.
 * <p>
 *     A SecureContainer has to be completly {@link Serializable} as it is stored as an object. It contains a hashmap
 *     that encrypts any key-value pairs before they are added. You can extend this class to pass additional data to be
 *     stored, however there is no guarantee it is stored safely in that case.
 * </p>
 */
public class SecureContainer implements Serializable {
    private HashMap<byte[], byte[]> cryptData;
    private HashMap<String, String> clearTextData;

    /**
     * Creates a new secure container
     */
    public SecureContainer() {
        cryptData = new HashMap<>();
        clearTextData = new HashMap<>();
    }

    /**
     * Encrypts and adds {@code key} and {@code value} to the stored data map
     *
     * @param key the key to save
     * @param value the value to save the key with
     */
    public void securePut(String key, String value) {
        clearTextData.put(key, value);
    }

    /**
     * Retrieves and decrypts {@code key} and {@code value} and returns the decrypted value
     *
     * @param key the key to retrieve {@code value} with
     * @return the value if it was found and decrypted successfully, else null
     */
    public String secureGet(String key) {
        return clearTextData.get(key);
    }

    /**
     * Gets the clear text data array
     *
     * @return the clear text data array
     */
    public HashMap<String, String> getClearTextData() {
        return clearTextData;
    }

    /**
     * Sets the clear text data
     *
     * @param clearTextData the clear text data to set
     */
    public void setClearTextData(HashMap<String, String> clearTextData) {
        this.clearTextData = clearTextData;
    }

    /**
     * Gets the crypt data array
     *
     * @return the crypt data array
     */
    public HashMap<byte[], byte[]> getCryptData() {
        return cryptData;
    }

    /**
     * Sets the crypt data array
     *
     * @param cryptData the crypt data array to set
     */
    public void setCryptData(HashMap<byte[], byte[]> cryptData) {
        this.cryptData = cryptData;
    }
}
