package org.intellimate.izou.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * The PermissionManager manages conflicts between addOns. For example if two AddOns want to play music, then the
 * PermissionManager will sort out this conflict. It manages intra-izou problems, that means it does not worry about
 * general system security. That is what the {@link IzouSecurityManager} does.
 */
public abstract class IzouPermissionManager {

    /**
     * Applies SHA-256 hash function on the {@code input} string
     *
     * @param input the string to apply to SHA-256 hash on
     * @return the hashed input string
     * @throws NoSuchAlgorithmException thrown if SHA-256 is not found as an algorithm
     */
    protected String sha256(String input) throws NoSuchAlgorithmException {
        MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
        byte[] result = mDigest.digest(input.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte aResult : result) {
            sb.append(Integer.toString((aResult & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString().toLowerCase();
    }
}
