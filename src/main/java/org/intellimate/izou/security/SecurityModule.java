package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;

/**
 * SecurityModule implements basic security-relevant functions like hash functions or encryption and decryption
 * functions.
 */
public class SecurityModule {
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
}
