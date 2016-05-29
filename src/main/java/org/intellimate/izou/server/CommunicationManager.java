package org.intellimate.izou.server;

import com.sun.jersey.api.client.ClientHandlerException;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;

import java.util.Optional;

/**
 * @author LeanderK
 * @version 1.0
 */
public class CommunicationManager extends IzouModule {
    private final ServerRequests serverRequests;
    private final boolean disabledLib;
    private final RequestHandler requestHandler;
    private final Thread connectionThread;
    private boolean run = true;
    private final String izouServerURL;

    public CommunicationManager(Main main, String izouServerURL, String izouSocketUrl, boolean ssl, boolean disabledLib, String refreshToken) throws IllegalStateException {
        super(main);
        if (ssl) {
            this.izouServerURL = "https://"+izouServerURL;
        } else {
            this.izouServerURL = "http://"+izouServerURL;
        }

        this.serverRequests = new ServerRequests(izouServerURL, izouSocketUrl, ssl, refreshToken, main);
        requestHandler = new RequestHandler(main);
        try {
            serverRequests.refreshToken();
        } catch (ClientHandlerException e) {
            error("unable to refreshToken Connection to server", e);
            throw new IllegalStateException("not able to refreshToken server-request package");
        }
        connectionThread = new Thread(() -> {
            while (run) {
                try {
                    serverRequests.requests(requestHandler::handleRequests);
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        debug("interrupted", e);
                    }
                } catch (Exception e) {
                    error("an uncatched Exception occurred", e);
                }
            }
        });
        connectionThread.start();
        this.disabledLib = disabledLib;
    }

    /**
     * the active IzouServerURL
     * @return the URL
     */
    public String getIzouServerURL() {
        return izouServerURL;
    }

    /**
     * returns the id of izou, or empty if not yet fetched
     * @return the id or empty
     */
    public Optional<Integer> getIzouId() {
        return serverRequests.getIzouId();
    }

    /**
     * returns the route izou is reachable at, concatenated with the server-url it constructs the whole url
     * @return the route or empty
     */
    public Optional<String> getIzouRoute() {
        return serverRequests.getIzouRoute();
    }

    /**
     * returns the Server-Requests which contains methods for interacting with the server
     * @return an instance of {@link ServerRequests}
     */
    public ServerRequests getServerRequests() {
        return serverRequests;
    }
}
