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
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.App;
import org.intellimate.server.proto.Izou;
import org.intellimate.server.proto.SocketConnection;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * used for all the http-requests to the server
 * @author LeanderK
 * @version 1.0
 */
//TODO update the update method (respect toInstall/toDelete etc)
public class ServerRequests extends IzouModule {
    private final String izouServerURL;
    private final String izouSocketUrl;
    private final String refreshToken;
    private final Client client;
    private final boolean ssl;
    private String authToken = null;
    private final JsonFormat.Parser parser = JsonFormat.parser();
    private boolean run = true;

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

    public void init() throws ClientHandlerException {
        if (authToken != null) {
            return;
        }
        WebResource.Builder builder = client.resource(izouServerURL + "/authentication/refreshIzou/izou")
                .header("Authorization", "Bearer " + refreshToken)
                .accept("application/json");

        ClientResponse post = builder.post(ClientResponse.class);

        if (post.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + post.getStatus());
        } else {
            authToken = post.getEntity(String.class);
        }
    }

    public void requests(Function<Request, Response> callback) {
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
                    .setToken(authToken)
                    .build()
                    .writeDelimitedTo(outputStream);
            outputStream.flush();
            boolean loop = true;
            while (run && loop) {
                org.intellimate.server.proto.HttpRequest httpRequest = org.intellimate.server.proto.HttpRequest.parseDelimitedFrom(inputStream);
                if (httpRequest == null) {
                    loop = false;
                    continue;
                }
                int bodySize = (int) httpRequest.getBodySize();
                if (bodySize < httpRequest.getBodySize()) {
                    throw new IllegalStateException("Body to large, requested body: "+httpRequest.getBodySize());
                }
                byte[] bytes = new byte[0];
                if (bodySize > 0) {
                    bytes = new byte[bodySize];
                    ByteStreams.readFully(inputStream, bytes);
                }
                RequestImpl request = new RequestImpl(httpRequest, bytes);
                Response response = callback.apply(request);
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
                long dataWritten = ByteStreams.copy(response.getData(), outputStream);
                if (dataWritten != response.getDataSize()) {
                    throw new IllegalStateException("response body is larger than advertised");
                }
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

    public String getNewestVersion() throws ClientHandlerException, InvalidProtocolBufferException {
        assureInit();
        ClientResponse jsonResponse = doGet("/izou");
        if (jsonResponse.getStatus() != 200) {
            throw new IllegalStateException("server answered with " + jsonResponse.getStatus());
        }
        Izou.Builder builder = Izou.newBuilder();
        parser.merge(jsonResponse.getEntity(String.class), builder);
        return builder.getVersion();
    }

    public InputStream download(String url) throws ClientHandlerException {
        assureInit();

        WebResource.Builder builder = client.resource(url)
                .header("Authorization", "Bearer " + authToken);

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
                .header("Authorization", "Bearer " + authToken)
                .accept("application/json");

        return builder.get(ClientResponse.class);
    }

    private void assureInit() {
        if (authToken == null) {
            throw new IllegalStateException("not initialized");
        }
    }
}
