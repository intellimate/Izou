package org.intellimate.izou.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jcajce.provider.digest.SHA3;
import org.bouncycastle.util.encoders.Hex;

import java.io.UnsupportedEncodingException;

/**
 * The PermissionManager manages conflicts between addOns. For example if two AddOns want to play music, then the
 * PermissionManager will sort out this conflict. It manages intra-izou problems, that means it does not worry about
 * general system security. That is what the {@link SecurityManager} does.
 */
public abstract class SecurityModule {
    private final Logger logger = LogManager.getLogger(this.getClass());

    /**
     * Applies SHA-256 hash function on the {@code input} string
     *
     * @param input the string to apply to SHA-256 hash on
     * @return the hashed input string
     */
    protected String sha3(String input) {
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
