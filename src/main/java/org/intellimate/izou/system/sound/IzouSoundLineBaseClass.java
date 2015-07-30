package org.intellimate.izou.system.sound;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.util.IzouModule;

import javax.sound.sampled.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * the base class for every IzouSoundLine, provides basic implementation of the Methods defined int IzouSoundLine and
 * delegates to Line, AutoCloseable etc.
 * @author LeanderK
 * @version 1.0
 */
public class IzouSoundLineBaseClass extends IzouModule implements Line, AutoCloseable, IzouSoundLine {
    //information about the line
    protected boolean notDisabled;
    protected Line line = null;
    protected final Line.Info info;
    protected float gain;
    protected boolean isMutedFromSystem = false;
    protected boolean isMutedFromUser = false;
    protected final boolean gainSupported;
    protected final FakeGainControl.ParameterHolder gainParameterHolder;
    protected final boolean muteSupported;
    protected boolean ended = false;
    protected boolean isOpen = false;


    private Future<?> closingThread;
    private boolean isPermanent;
    private final AddOnModel addOnModel;
    private Consumer<Void> closeCallback = null;
    private boolean muteIfNonPermanent = true;
    private Consumer<Void> muteCallback = null;
    private Identification responsibleID;

    //debugging, they will be used if they are not null
    Supplier<Line> debugGetLine = null;

    public IzouSoundLineBaseClass(Line line, Main main, boolean isPermanent, AddOnModel addOnModel) {
        super(main, false);
        this.line = line;
        this.info = line.getLineInfo();
        this.isPermanent = isPermanent;
        this.addOnModel = addOnModel;
        if (!isPermanent) {
            closingThread = getClosingThread(main, addOnModel);
        } else {
            closingThread = null;
        }
        boolean gainSupport = false;
        boolean muteSupport = false;
        FakeGainControl.ParameterHolder parameterHolder = null;
        Control[] controls = line.getControls();
        for (Control control : controls) {
            if (control.getType().toString().equals(BooleanControl.Type.MUTE.toString())) {
                muteSupport = true;
            } else if (control.getType().toString().equals(FloatControl.Type.MASTER_GAIN.toString())) {
                gainSupport = true;
                parameterHolder = new FakeGainControl.ParameterHolder((FloatControl) control);
            }
        }
        this.muteSupported = muteSupport;
        this.gainSupported = gainSupport;
        this.gainParameterHolder = parameterHolder;
    }

    public IzouSoundLineBaseClass(Info lineInfo, Main main, boolean isPermanent, AddOnModel addOnModel) {
        super(main, false);
        this.info = lineInfo;
        this.line = null;
        this.isPermanent = isPermanent;
        this.addOnModel = addOnModel;
        if (!isPermanent) {
            closingThread = getClosingThread(main, addOnModel);
        } else {
            closingThread = null;
        }
        this.muteSupported = false;
        this.gainSupported = false;
        this.gainParameterHolder = null;
    }

    private Future<?> getClosingThread(Main main, AddOnModel addOnModel) {
        return main.getThreadPoolManager().getIzouThreadPool().submit(() -> {
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                error("interrupted while sleeping, canceling");
                return;
            }
            if (line != null && line.isOpen()) {
                debug("closing line " + line + "for Addon " + addOnModel);
                line.close();
            }
            ended = true;
            isOpen = false;
        });
    }

    /**
     * Obtains the <code>Line.Info</code> object describing this
     * line.
     * @return description of the line
     */
    @Override
    public Info getLineInfo() {
        return info;
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
        if (!ended) {
            if (!isOpen) {
                opening();
            }
            if (notDisabled) {
                if (line != null) {
                    line.open();
                } else {
                    Line line;
                    if (debugGetLine == null) {
                        line = AudioSystem.getLine(info);
                    } else {
                        line = debugGetLine.get();
                    }
                    newLineInstance(line);
                    this.line.open();
                }
            }
            isOpen = true;
        }
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
        if (!ended) {
            if (isOpen) {
                System.out.println("closing for " + addOnModel);
                closeCallback.accept(null);
                if (notDisabled)
                    line.close();
                isOpen = false;
                ended = true;
            }
        }
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
        return isOpen;
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
        if (notDisabled) {
            List<Control> fakeControls = new ArrayList<>(2);
            if (muteSupported) {
                fakeControls.add(new FakeMuteControl(this));
            }
            if (gainSupported) {
                Control control = line.getControl(FloatControl.Type.MASTER_GAIN);
                fakeControls.add(new FakeGainControl(this, (FloatControl) control));
            }
            return fakeControls.toArray(new Control[fakeControls.size()]);
        } else {
            List<Control> fakeControls = new ArrayList<>(2);
            if (muteSupported) {
                fakeControls.add(new FakeMuteControl(this));
            }
            if (gainSupported) {
                fakeControls.add(new FakeGainControl(this, gainParameterHolder));
            }
            return fakeControls.toArray(new Control[fakeControls.size()]);
        }
    }

    /**
     * CURRENTLY NOT IMPLEMENTED!<br>
     * Indicates whether the line supports a control of the specified type. Some controls may only be available when the line is open.
     * @param control the type of the control for which support is queried
     * @return true if at least one control of the specified type is supported, otherwise false.
     */
    @Override
    public boolean isControlSupported(Control.Type control) {
        if(notDisabled) {
            return line.isControlSupported(control);
        }
        if (control.toString().equals(BooleanControl.Type.MUTE.toString())) {
            return muteSupported;
        }
        if (control.toString().equals(FloatControl.Type.MASTER_GAIN.toString())) {
            return gainSupported;
        }
        return false;
    }

    /**
     * Obtains a control of the specified type, if there is any.
     * Some controls may only be available when the line is open.
     * The mute-control operation may be overridden by the System.
     * @param type the type of the requested control
     * @return a control of the specified type
     * @throws IllegalArgumentException - if a control of the specified type is not supported
     * @see #isMutedFromSystem()
     */
    @Override
    public Control getControl(Control.Type type) throws IllegalArgumentException {
        if (type.toString().equals(BooleanControl.Type.MUTE.toString())) {
            return new FakeMuteControl(this);
        } else if (type.toString().equals(FloatControl.Type.MASTER_GAIN.toString())) {
            if (notDisabled) {
                Control control = line.getControl(type);
                return new FakeGainControl(this, (FloatControl) control);
            } else {
                return new FakeGainControl(this, gainParameterHolder);
            }
        }
        throw new IllegalArgumentException("control of the type " + type + " is not supported");
    }

    /**
     * CURRENTLY NOT IMPLEMENTED!<br>
     * follows no predictable behaviour, can be seen as not implemented.
     * @param listener the listener
     */
    @Override
    public void addLineListener(LineListener listener) {
        line.addLineListener(listener);
    }

    /**
     * CURRENTLY NOT IMPLEMENTED!<br>
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
        closingThread = getClosingThread(main, addOnModel);
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
        this.isMutedFromSystem = isMuted;
        if (ended)
            return;
        if (isMuted) {
            internEnd();
            line = null;
        } else {
            line = internRestoreGet();
            internRestoreLineState();
        }
    }

    protected void internEnd() {
        line.close();
        line = null;
    }

    protected void internRestoreLineState() {
        if (gainSupported) {
            try {
                FloatControl control = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
                control.setValue(gain);
            } catch (IllegalArgumentException e) {
                log.error(e);
            }
        }
        if (muteSupported) {
            try {
                BooleanControl control = (BooleanControl) line.getControl(BooleanControl.Type.MUTE);
                control.setValue(isMutedFromUser);
            } catch (IllegalArgumentException e) {
                log.error(e);
            }
        }
        if (isOpen) {
            try {
                internRestoreOpen();
            } catch (LineUnavailableException e) {
                log.error(e);
                ended = true;
                line = null;
            }
        }
    }

    protected void internRestoreOpen() throws LineUnavailableException {
        line.open();
    }

    protected Line internRestoreGet() {
        try {
            if (debugGetLine == null) {
                return AudioSystem.getLine(info);
            } else {
                return debugGetLine.get();
            }

        } catch (LineUnavailableException e) {
            ended = true;
            return null;
        }
    }

    void registerCloseCallback(Consumer<Void> consumer) {
        this.closeCallback = consumer;
    }

    void registerMuteCallback(Consumer<Void> consumer) {
        this.muteCallback = consumer;
    }

    protected void newLineInstance(Line line) {
        this.line = line;
    }
}
