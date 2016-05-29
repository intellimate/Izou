package org.intellimate.izou.main;

import com.google.common.io.ByteStreams;
import com.sun.jersey.api.client.ClientHandlerException;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.server.ServerRequests;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.server.proto.Izou;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * checks for new Izou-Versions
 * @author LeanderK
 * @version 1.0
 */
class IzouSynchronization extends IzouModule {
    private final Version currentVersion;
    private final ServerRequests serverRequests;

    IzouSynchronization(Main main, Version currentVersion, ServerRequests serverRequests) {
        super(main);
        this.currentVersion = currentVersion;
        this.serverRequests = serverRequests;
    }

    boolean updateIzou() throws IOException, ClientHandlerException {
        Izou currentIzou = serverRequests.getNewestVersion();
        File izouFile = main.getFileSystemManager().getIzouJarLocation();
        if (new Version(currentIzou.getVersion()).compareTo(currentVersion) != 0) {
            InputStream input = null;
            FileOutputStream fileOutputStream = null;
            try {
                input = serverRequests.download(currentIzou.getDownloadLink());
                File tempFile = new File(izouFile.getAbsolutePath() + ".new");
                if (!tempFile.exists()) {
                    tempFile.createNewFile();
                }
                fileOutputStream = new FileOutputStream(tempFile);
                ByteStreams.copy(input, fileOutputStream);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException ignored) {
                    }
                }

                if (input != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            return true;
        } else {
            return false;
        }
    }
}
