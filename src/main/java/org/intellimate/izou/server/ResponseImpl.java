package org.intellimate.izou.server;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * @author LeanderK
 * @version 1.0
 */
class ResponseImpl implements Response {
    private final int status;
    private final Map<String, List<String>> headers;
    private final String contentType;
    private final long dataSize;
    private final InputStream stream;

    public ResponseImpl(int status, Map<String, List<String>> headers, String contentType, byte[] data) {
        this.status = status;
        this.headers = headers;
        this.contentType = contentType;
        this.dataSize = data.length;
        this.stream = new ByteArrayInputStream(data);
    }

    public ResponseImpl(int status, Map<String, List<String>> headers, String contentType, long dataSize, InputStream stream) {
        this.status = status;
        this.headers = headers;
        this.contentType = contentType;
        this.dataSize = dataSize;
        this.stream = stream;
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
    public long getDataSize() {
        return dataSize;
    }

    @Override
    public InputStream getData() {
        return stream;
    }
}
