package org.intellimate.izou.server;

import ro.fortsoft.pf4j.AddonAccessible;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
public interface Response {
    int getStatus();
    Map<String, List<String>> getHeaders();
    String getContentType();
    long getDataSize();
    InputStream getData();
}
