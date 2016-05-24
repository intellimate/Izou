package org.intellimate.izou.server;

import com.sun.jersey.api.client.ClientHandlerException;
import org.intellimate.izou.config.AddOn;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;

import java.io.IOException;
import java.util.List;

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
    private final Synchronization synchronization;

    public CommunicationManager(Version currentVersion, Main main, String izouServerURL, String izouSocketUrl, boolean ssl, boolean disabledLib, String refreshToken, List<AddOn> selected) throws IllegalStateException {
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
        synchronization = new Synchronization(main, selected, disabledLib, serverRequests, currentVersion);
        this.disabledLib = disabledLib;
    }

    /**
     * checks for updates
     * @return true if there are changes and the need for an restart
     * @throws IOException may happen during the synchronization process, the method is not atomic,
     * but can be restarted at any time
     */
    public boolean checkForUpdates() throws IOException {
        boolean mustRestart = false;
        try {
            mustRestart = synchronization.updateIzou();
        } catch (ClientHandlerException e) {
            error("unable to update izou", e);
        }
        if (mustRestart) {
            return true;
        }
        if (!disabledLib) {
            return synchronization.synchronizeApps();
        }
        return false;
    }

    /**
     * returns whether izou is synchronizing
     * @return true if synchronizing
     */
    public boolean isSynchronizing() {
        return synchronization.isBusy();
    }

    /**
     * the active IzouServerURL
     * @return the URL
     */
    public String getIzouServerURL() {
        return izouServerURL;
    }
}
