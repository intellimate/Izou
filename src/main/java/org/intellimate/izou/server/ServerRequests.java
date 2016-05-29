package org.intellimate.izou.server;

import com.google.common.io.ByteStreams;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.AddonThreadPoolUser;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.App;
import org.intellimate.server.proto.Izou;
import org.intellimate.server.proto.SocketConnection;
import org.intellimate.server.proto.SocketConnectionResponse;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.Charset;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * used for all the http-requests to the server
 * @author LeanderK
 * @version 1.0
 */

//TODO: os via System.getProperty("os.arch") (arm on arm)
public class ServerRequests extends IzouModule implements AddonThreadPoolUser {
    private final String izouServerURL;
    private final String izouSocketUrl;
    private final String refreshToken;
    private final Client client;
    private final boolean ssl;
    private String authToken = null;
    private LocalDateTime authTokenCreation = null;
    private final JsonFormat.Parser parser = JsonFormat.parser();
    private boolean run = true;
    private int izouId = -1;
    private String izouRoute = null;

    public ServerRequests(String izouServerURL, String izouSocketUrl, boolean ssl, String refreshToken, Main main) {
        super(main);
        if (ssl) {
            this.izouServerURL = "https://"+izouServerURL;
        } else {
            this.izouServerURL = "http://"+izouServerURL;
        }
        this.izouSocketUrl = izouSocketUrl;
        this.ssl = ssl;
        this.refreshToken = refreshToken;
        this.client = Client.create();
    }

    void refreshToken() throws ClientHandlerException {
        WebResource.Builder builder = client.resource(izouServerURL + "/authentication/refreshIzou/izou")
                .header("Authorization", "Bearer " + refreshToken)
                .accept("application/json");

        ClientResponse post = builder.post(ClientResponse.class);

        if (post.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + post.getStatus());
        } else {
            authToken = post.getEntity(String.class);
        }
        authTokenCreation = LocalDateTime.now();
    }

    /**
     * processes the requests from the
     * @param callback
     */
    void requests(Function<Request, Response> callback) {
        assureInit();
        SocketFactory socketFactory;
        if (ssl) {
            socketFactory = SSLSocketFactory.getDefault();
        } else {
            socketFactory = SocketFactory.getDefault();
        }
        Socket socket = null;
        try {
            socket = socketFactory.createSocket(InetAddress.getByName(izouSocketUrl), 4000);
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();
            SocketConnection.newBuilder()
                    .setToken(getAuthToken())
                    .build()
                    .writeDelimitedTo(outputStream);
            outputStream.flush();
            SocketConnectionResponse connectionResponse = SocketConnectionResponse.parseDelimitedFrom(socket.getInputStream());
            this.izouId = connectionResponse.getId();
            this.izouRoute = connectionResponse.getRoute();
            boolean loop = true;
            while (run && loop) {
                org.intellimate.server.proto.HttpRequest httpRequest = org.intellimate.server.proto.HttpRequest.parseDelimitedFrom(inputStream);
                if (httpRequest == null) {
                    loop = false;
                    continue;
                }
                int bodySize = (int) httpRequest.getBodySize();
                InputStream stream = ByteStreams.limit(socket.getInputStream(), bodySize);
                InputStream streamWithoutClose = new DelegatingInputstream(stream);
                RequestImpl request = new RequestImpl(httpRequest, streamWithoutClose, bodySize);
                Response response;
                try {
                    response = callback.apply(request);
                } catch (Exception e) {
                    error("unable to apply callback", e);
                    String returnText = "an internal error occured: " + e.getMessage();
                    response = new ResponseImpl(500, new HashMap<>(), "text", returnText.getBytes(Charset.forName("UTF-8")));
                }
                int result = streamWithoutClose.read();
                while (result != -1) {
                    result = streamWithoutClose.read();
                }
                org.intellimate.server.proto.HttpResponse.newBuilder()
                        .setContentType(response.getContentType())
                        .setStatus(response.getStatus())
                        .setBodySize(response.getDataSize())
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
                Response finalResponse = response;
                submit(() -> {
                    try {
                        if (finalResponse.getData() != null) {
                            long dataWritten = 0;
                            try {
                                dataWritten = ByteStreams.copy(finalResponse.getData(), outputStream);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            if (dataWritten != finalResponse.getDataSize()) {
                                throw new IllegalStateException("response body is larger than advertised");
                            }
                        }
                    } finally {
                        if (finalResponse.getData() != null) {
                            try {
                                finalResponse.getData().close();
                            } catch (IOException e) {
                                debug("unable to close inputStream from app", e);
                            }
                        }
                    }
                }).join();
                outputStream.flush();
            }
        } catch (EOFException e) {
            error("body size did not match advertised size", e);
        } catch (IOException e) {
            error("there was a problem with the server connection", e);
        } catch (IllegalStateException e) {
            error(e.getMessage(), e);
        } catch (CompletionException e) {
            debug("there was a problem copying the response into the body", e);
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

    public Izou getNewestVersion() throws ClientHandlerException, InvalidProtocolBufferException {
        assureInit();
        ClientResponse jsonResponse = doGet("/izou");
        if (jsonResponse.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + jsonResponse.getStatus());
        }
        Izou.Builder builder = Izou.newBuilder();
        parser.merge(jsonResponse.getEntity(String.class), builder);
        return builder.build();
    }

    public InputStream download(String url) throws ClientHandlerException {
        assureInit();

        WebResource.Builder builder = client.resource(url)
                .header("Authorization", "Bearer " + getAuthToken());

        ClientResponse response = builder.get(ClientResponse.class);

        if (response.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + response.getStatus());
        }
        return response.getEntityInputStream();
    }

    public Optional<App> getAddonAndDependencies(int id) throws ClientHandlerException {
        assureInit();
        ClientResponse app = doGet("/apps/" + id);
        if (app.getStatus() == 404) {
            debug("unable to retrieve addon"+id);
            return Optional.empty();
        }
        App.Builder appBuilder = App.newBuilder();
        try {
            parser.merge(app.getEntity(String.class), appBuilder);
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
                        ClientResponse response =
                                doGet("apps/" + id + "/" + parsed.getMajor() + "/" + parsed.getMinor() + "/" + parsed.getPatch());
                        App.AppVersion.Builder builder = App.AppVersion.newBuilder();
                        try {
                            parser.merge(response.getEntity(String.class), builder);
                            return Optional.of(builder.build());
                        } catch (InvalidProtocolBufferException e) {
                            debug("parse result for addon" + id);
                            return Optional.empty();
                        }
                    } catch (ClientHandlerException e) {
                        return Optional.empty();
                    }
                })
                .map(appVersion -> {
                    appBuilder.clearVersions();
                    appBuilder.addVersions(appVersion);
                    return appBuilder.build();
                });

    }

    private ClientResponse doGet(String route) throws ClientHandlerException {
        assureInit();

        WebResource.Builder builder = client.resource(izouServerURL + "/authentication/refreshIzou/izou")
                .header("Authorization", "Bearer " + getAuthToken())
                .accept("application/json");

        return builder.get(ClientResponse.class);
    }

    private void assureInit() {
        if (authToken == null) {
            throw new IllegalStateException("not initialized");
        }
    }

    private String getAuthToken() {
        if (authToken == null) {
            refreshToken();
        }
        if (LocalDateTime.now().plusDays(1).isAfter(authTokenCreation)) {
            refreshToken();
        }
        return authToken;
    }

    /**
     * returns the IzouId on the Server, if fetched
     * @return the id or empty
     */
    public Optional<Integer> getIzouId() {
        if (izouId == -1) {
            return Optional.empty();
        } else {
            return Optional.of(izouId);
        }
    }

    /**
     * returns the route Izou is reachable at, if fetched
     * @return the route or empty if not fetched
     */
    public Optional<String> getIzouRoute() {
        return Optional.ofNullable(izouRoute);
    }
}
