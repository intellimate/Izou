package org.intellimate.izou.server;

import org.intellimate.server.proto.HttpRequest;
import ro.fortsoft.pf4j.AddonAccessible;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
class RequestImpl implements Request {
    private final HttpRequest request;
    private final Map<String, List<String>> params;
    private final byte[] data;

    public RequestImpl(HttpRequest request, byte[] data) {
        this.request = request;
        params = request.getParamsList().stream()
                .collect(Collectors.toMap(HttpRequest.Param::getKey, param -> (List<String>) param.getValueList(), (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }));
        this.data = data;
    }

    public HttpRequest getRequest() {
        return request;
    }

    @Override
    public String getUrl() {
        return request.getUrl();
    }

    @Override
    public Map<String, List<String>> getParams() {
        return params;
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
