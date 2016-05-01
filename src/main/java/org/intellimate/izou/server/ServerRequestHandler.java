package org.intellimate.izou.server;

import org.intellimate.server.proto.HttpRequest;
import org.intellimate.server.proto.HttpResponse;

/**
 * @author LeanderK
 * @version 1.0
 */
public interface ServerRequestHandler {
    HttpResponse handle(HttpRequest httpRequest);
}
