package org.intellimate.izou.system.context;

import java.util.Optional;

/**
 * this class holds information related to the server
 * @author LeanderK
 * @version 1.0
 */
public interface ServerInformation {
    /**
     * returns the URL of the server this is instance is communicating with
     * @return the
     */
    Optional<String> getIzouServerURL();

    /**
     * returns the id of izou, or empty if not yet fetched
     * @return the id or empty
     */
    Optional<Integer> getIzouId();

    /**
     * returns the route izou is reachable at, concatenated with the server-url it constructs the whole url
     * @return the route or empty
     */
    Optional<String> getIzouRoute();
}
