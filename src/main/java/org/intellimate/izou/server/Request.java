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
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
public interface Request {
    String getUrl();
    Map<String, List<String>> getParams();
    String getMethod();
    @SuppressWarnings("unused")
    String getContentType();
    int getContentLength();
    InputStream getData();
    default Optional<String> getDataAsUTF8String() throws IOException {
        byte[] bytes = ByteStreams.toByteArray(getData());
        if (bytes.length != getContentLength()) {
            return Optional.empty();
        } else {
            return Optional.of(new String(bytes, Charset.forName("UTF-8")));
        }
    }
}
