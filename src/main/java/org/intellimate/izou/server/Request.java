package org.intellimate.izou.server;

import com.google.common.io.ByteStreams;
import ro.fortsoft.pf4j.AddonAccessible;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * the interface for an http-request from an user
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
public interface Request {
    /**
     * this method returns the relevant url.
     * <p>
     * The relevant url is the path of the url relevant to izou, e.g. when an user calls:
     * {@code http://api.izou.info/users/1/izou/1/instance/this/example/path} the relevant url is: {@code /this/example/path}.
     * @return a String containing the urls
     */
    String getUrl();

    /**
     * this method returns the query parameters passed
     * @return a map with the query parameters or empty if none passed
     */
    Map<String, List<String>> getParams();

    /**
     * returns the http-method used for the request, e.g. {@code GET}.
     * @return a String containing the http-method
     */
    String getMethod();

    /**
     * returns the content-type of the request, e.g. {@code text/html}
     * @return the content-type
     */
    @SuppressWarnings("unused")
    String getContentType();

    /**
     * returns the content-length of the body of the request, or -1 if there is no body for the request
     * @return the content-length or -1
     */
    int getContentLength();

    /**
     * returns the input-stream for the content of the body of the request
     * @return an instance of inputStream, may be empty if there is not body
     */
    InputStream getData();

    /**
     * returns the body as an UTF-8 String
     * @return the Body as an UTF-8 String or empty if the content-length does not match the advertised lenght
     * @throws IOException if an exception occurred while reading the input-stream
     */
    default Optional<String> getDataAsUTF8String() throws IOException {
        byte[] bytes = ByteStreams.toByteArray(getData());
        if (bytes.length != getContentLength()) {
            return Optional.empty();
        } else {
            return Optional.of(new String(bytes, Charset.forName("UTF-8")));
        }
    }
}
