package org.intellimate.izou.system.sound;

import org.intellimate.izou.addon.AddOnModel;

/**
 * use this interface to communicate with Izou. Every Instance of Line you obtain via Mixer.getLine() implements this
 * interface.
 * @author LeanderK
 * @version 1.0
 */
public interface IzouSoundLine {
    /**
     * returns whether the line is permanently-available.
     * If a line is not permanently available, it will close after max. 10 minutes
     * @return true if permanent.
     */
    boolean isPermanent();

    /**
     * gets the associated AddonModel
     * @return the AddonModel
     */
    @SuppressWarnings("unused")
    AddOnModel getAddOnModel();

    /**
     * returns whether the Line is muted
     * @return true if muted.
     */
    @SuppressWarnings("unused")
    boolean isMuted();

    /**
     * sets whether other Addons audio-inputs should be muted while this line is open (only works for non-permanent lines).
     * The standard is true.
     * @param muteIfNonPermanent true if muted, false if not
     */
    @SuppressWarnings("unused")
    void setMuteIfNonPermanent(boolean muteIfNonPermanent);

    /**
     * retruns whether other Addons audio-inputs should be muted while this line is open (only works for non-permanent lines).
     * @return true if muted, false if not
     */
    @SuppressWarnings("unused")
    boolean isMuteIfNonPermanent();
}
