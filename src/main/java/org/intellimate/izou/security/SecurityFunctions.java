package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import javax.crypto.*;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;

/**
 * SecurityFunction implements basic cryptographic functions like hash functions or encryption and decryption
 * functions.
 */
public final class SecurityFunctions {
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Applies SHA-3 hash function on the {@code input} string
     *
     * @param input the string to apply to SHA-3 hash on
     * @return the hashed input string
     */
    public String sha3(String input) {
        String hash = "";
        try {
            SHA3.DigestSHA3 md = new SHA3.DigestSHA3(256);
            md.update(input.getBytes("UTF-8"));
            hash = Hex.toHexString(md.digest());
        } catch (UnsupportedEncodingException e) {
            logger.error("Error while hashing with SHA-3", e);
        }

        return hash;
    }

    /**
     * Applies an AES encryption on the string {@code plainText} with they given key
     * <p>
     *     Keys have to be generated using the {@link #generateKey()} function
     * </p>
     * @param plainText the string to encrypt
     * @param key the key to use during the encryption
     * @return a byte array containing the encrypted data
     */
    public byte[] encryptAES(String plainText, SecretKey key) {
        byte[] byteCipherText = new byte[0];

        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            byteCipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
        } catch (BadPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException | NoSuchPaddingException
                | InvalidKeyException | UnsupportedEncodingException | NoSuchProviderException e) {
            logger.error("Unable to apply AES encryption", e);
        }

        return byteCipherText;
    }

    /**
     * Applies an AES decryption on the byte array {@code cipherBytes} with they given key. The key has to be the same
     * key used during encryption, else null is returned
     *
     * @param cipherBytes the byte array to decrypt
     * @param key the key to use during the decryption
     * @return the decrypted string if everything was successful, else null
     */
    public String decryptAES(byte[] cipherBytes, SecretKey key) {
        String plainText = null;
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        try {
            Cipher cipher = Cipher.getInstance("AES", "BC");
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] bytePlainText = cipher.doFinal(cipherBytes);
            plainText = new String(bytePlainText, "UTF-8");
        } catch (IllegalBlockSizeException | InvalidKeyException | NoSuchAlgorithmException | BadPaddingException
                | NoSuchPaddingException | UnsupportedEncodingException | NoSuchProviderException e) {
            logger.error("Unable to apply AES decryption", e);
        }

        return plainText;
    }

    /**
     * Generates a key for the AES encryption
     *
     * @return a new key for the AES encryption
     */
    public SecretKey generateKey() {
        SecretKey key = null;
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(128);
            key = generator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            logger.error("Unable to generate AES key", e);
        }

        return key;
    }
}
