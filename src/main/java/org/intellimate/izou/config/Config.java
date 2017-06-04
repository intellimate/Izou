package org.intellimate.izou.config;

import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class Config {
    /**
     * the token used to communicate with the server
     */
    public String token;

    /**
     * the url of the server
     */
    public String url = "api.izou.info";

    /**
     * the url of the server for the socket connection
     */
    public String urlSocket = "api.izou.info";

    /**
     * true if ssl-connection is enabled, false if not
     */
    public String ssl = "true";
}
