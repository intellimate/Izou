package org.intellimate.izou.server;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

/**
 * @author LeanderK
 * @version 1.0
 */
public interface Request {
    String getUrl();
    Map<String, List<String>> getParams();
    String getMethod();
    String getContentType();
    byte[] getData();
    default String getDataAsUTF8() {
        return new String(getData(), Charset.forName("UTF-8"));
    }
}
