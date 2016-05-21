package org.intellimate.izou.server;

import com.google.common.io.ByteStreams;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import com.mashape.unirest.request.HttpRequest;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.App;
import org.intellimate.server.proto.Izou;
import org.intellimate.server.proto.SocketConnection;
import sun.security.ssl.SSLSocketImpl;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * used for all the qhttp-requests to the server
 * @author LeanderK
 * @version 1.0
 */
public class ServerRequests extends IzouModule {
    private final String url;
    private final String refreshToken;
    private String authToken = null;
    private final JsonFormat.Parser parser = JsonFormat.parser();
    private boolean run = true;

    public ServerRequests(String url, String refreshToken, Main main) {
        super(main);
        this.url = url;
        this.refreshToken = refreshToken;
    }

    public void init() throws UnirestException {
        if (authToken != null) {
            return;
        }
        HttpResponse<String> response = Unirest.get(url + "/authentication/izou")
                .queryString("Authorization", "Bearer " + refreshToken)
                .asString();

        if (response.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + response.getStatus());
        } else {
            authToken = response.getBody();
        }
    }

    public void requests(Function<Request, Response> callback) {
        assureInit();
        SocketFactory socketFactory = SSLSocketFactory.getDefault();
        Socket socket = null;
        try {
            socket = socketFactory.createSocket(url, 4000);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            SocketConnection.newBuilder()
                    .setToken(authToken)
                    .build()
                    .writeDelimitedTo(outputStream);
            outputStream.flush();
            while (run) {
                org.intellimate.server.proto.HttpRequest httpRequest = org.intellimate.server.proto.HttpRequest.parseDelimitedFrom(inputStream);
                int bodySize = (int) httpRequest.getBodySize();
                if (bodySize < httpRequest.getBodySize()) {
                    throw new IllegalStateException("Body to large, requested body: "+httpRequest.getBodySize());
                }
                byte[] bytes = new byte[bodySize];
                ByteStreams.readFully(inputStream, bytes);
                RequestImpl request = new RequestImpl(httpRequest, bytes);
                Response response = callback.apply(request);
                org.intellimate.server.proto.HttpResponse.newBuilder()
                        .setContentType(response.getContentType())
                        .setStatus(response.getStatus())
                        .setBodySize(response.getData().length)
                        .addAllHeaders(
                                response.getHeaders().entrySet().stream()
                                        .map(entry ->
                                                org.intellimate.server.proto.HttpResponse.Header.newBuilder()
                                                        .setKey(entry.getKey())
                                                        .addAllValue(entry.getValue())
                                                        .build()
                                        )
                                        .collect(Collectors.toList())
                        )
                        .build()
                        .writeDelimitedTo(outputStream);
                outputStream.write(response.getData());
                outputStream.flush();
            }
        } catch (EOFException e) {
            error("body size did not match advertised size", e);
        } catch (IOException e) {
            error("there was a problem with the server connection", e);
        } catch (IllegalStateException e) {
            error(e.getMessage(), e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    error("unable to close the socket", e);
                }
            }
        }
    }

    public String getNewestVersion() throws UnirestException, InvalidProtocolBufferException {
        assureInit();
        HttpResponse<JsonNode> jsonResponse = doGet("/izou").asJson();
        if (jsonResponse.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + jsonResponse.getStatus());
        }
        Izou.Builder builder = Izou.newBuilder();
        parser.merge(jsonResponse.getBody().toString(), builder);
        return builder.getVersion();
    }

    public InputStream download(String url) throws UnirestException {
        assureInit();
        HttpResponse<InputStream> response = Unirest.get(url)
                .queryString("Authorization", "Bearer " + authToken)
                .asBinary();
        if (response.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + response.getStatus());
        }
        return response.getBody();
    }

    public Optional<App> getAddonAndDependencies(int id) throws UnirestException {
        assureInit();
        HttpResponse<JsonNode> app = doGet("/apps/" + id).asJson();
        if (app.getStatus() == 404) {
            debug("unable to retrieve addon"+id);
            return Optional.empty();
        }
        App.Builder appBuilder = App.newBuilder();
        try {
            parser.merge(app.getBody().toString(), appBuilder);
        } catch (InvalidProtocolBufferException e) {
            debug("parse result for addon"+id);
            return Optional.empty();
        }

        return appBuilder.getVersionsList().stream()
                .reduce((appVersion, appVersion2) -> {
                    Version version = new Version(appVersion.getVersion());
                    Version version2 = new Version(appVersion2.getVersion());
                    if (version.compareTo(version2) < 0) {
                        return appVersion2;
                    } else {
                        return appVersion;
                    }
                })
                .flatMap(version -> {
                    Version parsed = new Version(version.getVersion());
                    try {
                        HttpResponse<JsonNode> response =
                                doGet("apps/" + id + "/" + parsed.getMajor() + "/" + parsed.getMinor() + "/" + parsed.getPatch())
                                        .asJson();
                        App.AppVersion.Builder builder = App.AppVersion.newBuilder();
                        try {
                            parser.merge(response.getBody().toString(), builder);
                            return Optional.of(builder.build());
                        } catch (InvalidProtocolBufferException e) {
                            debug("parse result for addon" + id);
                            return Optional.empty();
                        }
                    } catch (UnirestException e) {
                        return Optional.empty();
                    }
                })
                .map(appVersion -> {
                    appBuilder.clearVersions();
                    appBuilder.addVersions(appVersion);
                    return appBuilder.build();
                });

    }

    private HttpRequest doGet(String route) {
        assureInit();
        return Unirest.get(url + route)
                .header("Accept", "application/json")
                .queryString("Authorization", "Bearer " + authToken);
    }

    private void assureInit() {
        if (authToken == null) {
            throw new IllegalStateException("not initialized");
        }
    }
}
