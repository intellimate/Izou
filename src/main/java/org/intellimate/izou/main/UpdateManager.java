package org.intellimate.izou.main;

import com.sun.jersey.api.client.ClientHandlerException;
import org.intellimate.izou.config.Version;
import org.intellimate.izou.server.CommunicationManager;
import org.intellimate.izou.util.IzouModule;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * this class is responsible for the updates
 * @author LeanderK
 * @version 1.0
 */
public class UpdateManager extends IzouModule {
    private final boolean disabledUpdate;
    private final IzouSynchronization izouSynchronization;
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final Timer timer;
    private boolean disabledLib;
    private final Lock lock = new ReentrantLock();
    private boolean isLocked = false;

    public UpdateManager(Main main, boolean disabledUpdate, Version currentIzouVersion, CommunicationManager communicationManager, boolean disabledLib) {
        super(main);
        this.disabledUpdate = disabledUpdate;
        this.disabledLib = disabledLib;
        if (!disabledUpdate) {
            timer = init();
        } else {
            timer = null;
        }
        izouSynchronization = new IzouSynchronization(main, currentIzouVersion, communicationManager.getServerRequests());
    }

    private Timer init() {
        LocalDateTime firstInterval = LocalDateTime.now().withHour(2);
        if (firstInterval.plusMinutes(10).isBefore(LocalDateTime.now())) {
            firstInterval = firstInterval.minusDays(1);
        }
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                boolean shouldRestart = false;
                //TODO maybe a more sophisticated algorithm?
                try {
                    shouldRestart = checkForUpdates();
                } catch (IOException e) {
                    error("unable to search for updates", e);
                    try {
                        Thread.sleep(300000);
                        shouldRestart = checkForUpdates();
                    } catch (IOException e1) {
                        error("unable to search for updates", e);
                    } catch (InterruptedException e1) {
                        error("interrupted", e);
                    }
                }
                if (shouldRestart) {
                    debug("restarting to apply updates");
                    System.exit(0);
                }
            }
        }, Date.from(firstInterval.atZone(ZoneId.systemDefault()).toInstant()), 8640000);
        return timer;
    }

    /**
     * checks for updates, this method has no effects if updates are disabled
     * @return true if there are changes and the need for an restart
     * @throws IOException may happen during the synchronization process, the method is not atomic,
     * but can be restarted at any time
     */
    public boolean checkForUpdates() throws IOException {
        if (disabledUpdate) {
            return false;
        }
        lock.lock();
        isLocked = true;
        try {
            boolean mustRestart = false;
            try {
                mustRestart = izouSynchronization.updateIzou();
            } catch (ClientHandlerException e) {
                error("unable to update izou", e);
            }
            if (mustRestart) {
                return true;
            }
            if (!disabledLib) {
                return getMain().getAddOnManager().synchronizeApps();
            }
            return false;
        } finally {
            lock.unlock();
            isLocked = false;
        }
    }

    /**
     * returns true when the UpdatesScheduler is already updating
     * @return true if updating
     */
    public boolean isUpdating() {
        return isLocked;
    }

    /**
     * retruns true if updates are disabled
     * @return true if disabled
     */
    public boolean isUpdateDisabled() {
        return disabledUpdate;
    }
}
