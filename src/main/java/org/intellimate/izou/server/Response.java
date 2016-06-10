package org.intellimate.izou.server;

import ro.fortsoft.pf4j.AddonAccessible;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * the interface for the response to the user
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
public interface Response {
    /**
     * the http-status of the response, e.g. {@code 200}
     * @return the status
     */
    int getStatus();

    /**
     * returns the headers for the response
     * @return a map containing the headers
     */
    Map<String, List<String>> getHeaders();

    /**
     * the content-type of the response, e.g. {@code text/html}
     * @return a String containing the content-type
     */
    String getContentType();

    /**
     * the length of the data to send (there should always be data!)
     * @return the length of the data to send
     */
    long getDataSize();

    /**
     * the data to send (there should always be data!)
     * @return an inputStream containing the data to send
     */
    InputStream getData();
}
