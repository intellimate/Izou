package org.intellimate.izou.server;

import org.intellimate.server.proto.HttpRequest;
import ro.fortsoft.pf4j.AddonAccessible;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * the implementation for {@link Request}
 * @author LeanderK
 * @version 1.0
 */
class RequestImpl implements Request {
    private final HttpRequest request;
    private final Map<String, List<String>> params;
    private final InputStream in;
    private final int contentLenght;

    RequestImpl(HttpRequest request, InputStream in, int contentLenght) {
        this.request = request;
        params = request.getParamsList().stream()
                .collect(Collectors.toMap(HttpRequest.Param::getKey, param -> (List<String>) param.getValueList(), (l1, l2) -> {
                    l1.addAll(l2);
                    return l1;
                }));
        this.in = in;
        this.contentLenght = contentLenght;
    }

    private RequestImpl(HttpRequest request, Map<String, List<String>> params, InputStream in, int contentLenght) {
        this.request = request;
        this.params = params;
        this.in = in;
        this.contentLenght = contentLenght;
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

    public Request changeParams(Map<String, List<String>> params) {
        return new RequestImpl(request, params, in, contentLenght);
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
    public int getContentLength() {
        return contentLenght;
    }

    @Override
    public InputStream getData() {
        return in;
    }
}
