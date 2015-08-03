package org.intellimate.izou.internal.system.sound;

import org.intellimate.izou.system.sound.IzouSoundLine;
import org.intellimate.izou.internal.util.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.internal.main.Main;

import javax.sound.sampled.*;
import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * the base class for every IzouSoundLine, provides basic implementation of the Methods defined int IzouSoundLine and
 * delegates to Line, AutoCloseable etc.
 * @author LeanderK
 * @version 1.0
 */
public class IzouSoundLineBaseClass extends IzouModule implements Line, AutoCloseable, IzouSoundLine {
    protected final Line line;
    private Future<?> closingThread;
    private boolean isPermanent;
    protected final SoundManager soundManager;
    private final AddOnModel addOnModel;
    protected final boolean isMutable;
    protected boolean isMutedFromSystem = false;
    private boolean isMutedFromUser = false;
    private Consumer<Void> closeCallback = null;
    private boolean muteIfNonPermanent = true;
    private Consumer<Void> muteCallback = null;
    private Identification responsibleID;

    public IzouSoundLineBaseClass(Line line, Main main, boolean isPermanent, AddOnModel addOnModel) {
        super(main, false);
        this.line = line;
        this.isPermanent = isPermanent;
        this.addOnModel = addOnModel;
        if (!isPermanent) {
            closingThread = getClosingThread(line, main, addOnModel);
        } else {
            closingThread = null;
        }
        soundManager = null;
        boolean mutable;
        try {
            line.getControl(BooleanControl.Type.MUTE);
            mutable = true;
        } catch (IllegalArgumentException e) {
            mutable = false;
        }
        isMutable = mutable;
    }

    private Future<?> getClosingThread(Line line, Main main, AddOnModel addOnModel) {
        return main.getThreadPoolManager().getIzouThreadPool().submit(() -> {
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                error("interrupted while sleeping, canceling");
                return;
            }
            if (line.isOpen()) {
                debug("closing line " + line + "for Addon " + addOnModel);
                line.close();
            }
        });
    }

    /**
     * Obtains the <code>Line.Info</code> object describing this
     * line.
     * @return description of the line
     */
    @Override
    public Info getLineInfo() {
        return line.getLineInfo();
    }

    /**
     * Opens the line, indicating that it should acquire any required
     * system resources and become operational.
     * If this operation
     * succeeds, the line is marked as open, and an <code>OPEN</code> event is dispatched
     * to the line's listeners.
     * <p>
     * Note that some lines, once closed, cannot be reopened.  Attempts
     * to reopen such a line will always result in an <code>LineUnavailableException</code>.
     * <p>
     * Some types of lines have configurable properties that may affect
     * resource allocation.   For example, a <code>DataLine</code> must
     * be opened with a particular format and buffer size.  Such lines
     * should provide a mechanism for configuring these properties, such
     * as an additional <code>open</code> method or methods which allow
     * an application to specify the desired settings.
     * <p>
     * This method takes no arguments, and opens the line with the current
     * settings.  For <code>SourceDataLine</code> and
     * <code>TargetDataLine</code> objects, this means that the line is
     * opened with default settings.  For a <code>Clip</code>, however,
     * the buffer size is determined when data is loaded.  Since this method does not
     * allow the application to specify any data to load, an IllegalArgumentException
     * is thrown. Therefore, you should instead use one of the <code>open</code> methods
     * provided in the <code>Clip</code> interface to load data into the <code>Clip</code>.
     * <p>
     * For <code>DataLine</code>'s, if the <code>DataLine.Info</code>
     * object which was used to retrieve the line, specifies at least
     * one fully qualified audio format, the last one will be used
     * as the default format.
     *
     * @throws IllegalArgumentException if this method is called on a Clip instance.
     * @throws LineUnavailableException if the line cannot be
     * opened due to resource restrictions.
     * @throws SecurityException if the line cannot be
     * opened due to security restrictions.
     *
     * @see #close
     * @see #isOpen
     */
    @Override
    public void open() throws LineUnavailableException {
        opening();
        line.open();
    }

    protected void opening() {
        System.out.println("opening for " + addOnModel);
        if (!line.isOpen() && !isPermanent && muteCallback != null)
            muteCallback.accept(null);
    }

    /**
     * Closes the line, indicating that any system resources
     * in use by the line can be released.  If this operation
     * succeeds, the line is marked closed and a <code>CLOSE</code> event is dispatched
     * to the line's listeners.
     * @throws SecurityException if the line cannot be
     * closed due to security restrictions.
     *
     * @see #open
     * @see #isOpen
     */
    @Override
    public void close() {
        System.out.println("closing for " + addOnModel);
        closeCallback.accept(null);
        line.close();
    }

    /**
     * Indicates whether the line is open, meaning that it has reserved
     * system resources and is operational, although it might not currently be
     * playing or capturing sound.
     * @return <code>true</code> if the line is open, otherwise <code>false</code>
     *
     * @see #open()
     * @see #close()
     */
    @Override
    public boolean isOpen() {
        return line.isOpen();
    }

    /**
     * Obtains the set of controls associated with this line. Some controls may only be available when the line is open.
     * If there are no controls, this method returns an array of length 0.
     * The mute-control operation may be overridden by the System.
     * @return the array of controls
     * @see #isMutedFromSystem()
     */
    @Override
    public Control[] getControls() {
        Control[] controls = line.getControls();
        for (int i = 0; i < controls.length; i++) {
            Control control = controls[i];
            if (control.getType().toString().equals(BooleanControl.Type.MUTE.toString())) {
                controls[i] = new FakeMuteControl();
            }
        }
        return controls;
    }

    /**
     * Indicates whether the line supports a control of the specified type. Some controls may only be available when the line is open.
     * @param control the type of the control for which support is queried
     * @return true if at least one control of the specified type is supported, otherwise false.
     */
    @Override
    public boolean isControlSupported(Control.Type control) {
        return line.isControlSupported(control);
    }

    /**
     * Obtains a control of the specified type, if there is any.
     * Some controls may only be available when the line is open.
     * The mute-control operation may be overridden by the System.
     * @param control the type of the requested control
     * @return a control of the specified type
     * @throws IllegalArgumentException - if a control of the specified type is not supported
     * @see #isMutedFromSystem()
     */
    @Override
    public Control getControl(Control.Type control) throws IllegalArgumentException {
        if (control.toString().equals(BooleanControl.Type.MUTE.toString())) {
            return new FakeMuteControl();
        } else {
            return line.getControl(control);
        }
    }

    /**
     * follows no predictable behaviour, can be seen as not implemented.
     * @param listener the listener
     */
    @Override
    public void addLineListener(LineListener listener) {
        line.addLineListener(listener);
    }

    /**
     * follows no predictable behaviour, can be seen as not implemented.
     * @param listener the listener
     */
    @Override
    public void removeLineListener(LineListener listener) {
        line.removeLineListener(listener);
    }

    /**
     * returns whether the line is permanently-available.
     * If a line is not permanently available, it will close after max. 10 minutes
     * @return true if permanent.
     */
    @Override
    public boolean isPermanent() {
        return isPermanent;
    }

    void setToPermanent() {
        if (isPermanent)
            return;
        closingThread.cancel(true);
        closingThread = null;
        isPermanent = true;
    }

    void setToNonPermanent() {
        if (!isPermanent)
            return;
        closingThread = getClosingThread(line, main, addOnModel);
        isPermanent = false;
    }

    /**
     * gets the associated AddonModel
     * @return the AddonModel
     */
    @Override
    @SuppressWarnings("unused")
    public AddOnModel getAddOnModel() {
        return addOnModel;
    }

    /**
     * gets the ID responsible
     *
     * @return the the ID
     */
    @Override
    public Identification getResponsibleID() {
        return responsibleID;
    }

    void setResponsibleID(Identification responsibleID) {
        this.responsibleID = responsibleID;
    }

    /**
     * returns whether the Line is muted
     * @return true if muted.
     */
    @Override
    @SuppressWarnings("unused")
    public boolean isMutedFromSystem() {
        return isMutedFromSystem;
    }

    /**
     * sets whether other Addons audio-inputs should be muted while this line is open (only works for non-permanent lines).
     * The standard is true.
     * @param muteIfNonPermanent true if muted, false if not
     */
    @Override
    @SuppressWarnings("unused")
    public void setMuteIfNonPermanent(boolean muteIfNonPermanent) {
        this.muteIfNonPermanent = muteIfNonPermanent;
    }

    /**
     * retruns whether other Addons audio-inputs should be muted while this line is open (only works for non-permanent lines).
     * @return true if muted, false if not
     */
    @Override
    public boolean isMuteIfNonPermanent() {
        return muteIfNonPermanent;
    }

    void setMutedFromSystem(boolean isMuted) {
        if (isMutable) {
            BooleanControl bc = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
            if (bc != null) {
                if (isMuted) {
                    bc.setValue(true); // true to mute the line, false to unmute
                } else {
                    bc.setValue(isMutedFromUser); // true to mute the line, false to unmute
                }
            }
        }
        this.isMutedFromSystem = isMuted;
    }

    void registerCloseCallback(Consumer<Void> consumer) {
        this.closeCallback = consumer;
    }

    void registerMuteCallback(Consumer<Void> consumer) {
        this.muteCallback = consumer;
    }

    private class FakeMuteControl extends BooleanControl {
        private final BooleanControl control;

        /**
         * Constructs a new boolean control object with the given parameters.
         * The labels for the <code>true</code> and <code>false</code> states
         * default to "true" and "false."
         */
        protected FakeMuteControl() {
            super(BooleanControl.Type.MUTE, isMutedFromUser);
            this.control = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
        }

        /**
         * Sets the current value for the control.  The default
         * implementation simply sets the value as indicated.
         * Some controls require that their line be open before they can be affected
         * by setting a value.
         *
         * @param value desired new value.
         */
        @Override
        public void setValue(boolean value) {
            isMutedFromUser = value;
            if (!isMutedFromSystem) {
                control.setValue(isMutedFromUser);
            }
        }

        /**
         * Obtains this control's current value.
         *
         * @return current value.
         */
        @Override
        public boolean getValue() {
            return isMutedFromUser;
        }

        /**
         * Obtains the label for the specified state.
         *
         * @param state the state whose label will be returned
         * @return the label for the specified state, such as "true" or "on"
         * for <code>true</code>, or "false" or "off" for <code>false</code>.
         */
        @Override
        public String getStateLabel(boolean state) {
            if (state) {
                return "true";
            } else {
                return "false";
            }
        }
    }
}
