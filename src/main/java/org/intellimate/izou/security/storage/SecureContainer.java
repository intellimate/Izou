package org.intellimate.izou.security.storage;

import org.intellimate.izou.security.SecurityModule;

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
    private HashMap<String, String> storedData;
    private SecurityModule securityModule;

    /**
     * Creates a new secure container
     */
    public SecureContainer() {
        securityModule = new SecurityModule();
    }

    /**
     * Encrypts and adds {@code key} and {@code value} to the stored data map
     *
     * @param key the key to save
     * @param value the value to save the key with
     * @return true if the key-value pair was added successfully
     */
    public boolean securePut(String key, String value) {

    }

    /**
     * Retrieves and decrypts {@code key} and {@code value} and returns the decrypted value
     *
     * @param key the key to retrieve {@code value} with
     * @return the value if it was found and decrypted successfully, else null
     */
    public boolean secureGet(String key) {

    }
}
