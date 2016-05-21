package org.intellimate.izou.server;

import java.util.List;
import java.util.Map;

/**
 * @author LeanderK
 * @version 1.0
 */
public class ResponseImpl implements Response {
    private final int status;
    private final Map<String, List<String>> headers;
    private final String contentType;
    private final byte[] data;

    public ResponseImpl(int status, Map<String, List<String>> headers, String contentType, byte[] data) {
        this.status = status;
        this.headers = headers;
        this.contentType = contentType;
        this.data = data;
    }

    @Override
    public int getStatus() {
        return status;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
