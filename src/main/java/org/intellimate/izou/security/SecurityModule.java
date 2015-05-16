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
 * SecurityModule implements basic security-relevant functions like hash functions or encryption and decryption
 * functions.
 */
public final class SecurityModule {
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

    public String decryptAES(byte[] cipherBytes, SecretKey key) {
        String plainText = "";
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
