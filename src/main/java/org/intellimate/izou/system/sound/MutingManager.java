package org.intellimate.izou.system.sound;

import org.intellimate.izou.addon.AddOnModel;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author LeanderK
 * @version 1.0
 */
public class MutingManager {
    private final AddOnModel muting;
    private final List<IzouSoundLine> mutingLines;
    private final SoundManager soundManager;
    private LocalTime limit = null;
    private Future limitFuture;

    public MutingManager(SoundManager soundManager, AddOnModel muting, IzouSoundLine line) {
        this.muting = muting;
        this.soundManager = soundManager;
        mutingLines = new ArrayList<>();
        mutingLines.add(line);
        soundManager.muteOthers(muting);
    }

    public AddOnModel getMuting() {
        return muting;
    }

    public synchronized void add(IzouSoundLine line) {
        if (limit != null) {
            limit = null;
            limitFuture.cancel(true);
        }
        mutingLines.add(line);
    }

    public synchronized void cancel() {
        soundManager.unmute();
    }

    public synchronized boolean isTimeOut() {
        return limit != null && LocalTime.now().isAfter(limit);
    }

    public synchronized MutingManager remove(IzouSoundLine line) {
        mutingLines.remove(line);
        if (mutingLines.isEmpty()) {
            limit = LocalTime.now().plus(2000L, ChronoUnit.MILLIS);
            limitFuture = soundManager.getMain().getThreadPoolManager().getAddOnsThreadPool().submit(() -> {
                try {
                    Thread.sleep(2000);
                    soundManager.unmute();
                } catch (InterruptedException ignored) {
                }
            });
            return this;
        }
        return this;
    }
}
