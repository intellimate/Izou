package org.intellimate.izou.system.sound;

import org.intellimate.izou.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;

import javax.sound.sampled.Control;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import java.util.function.Consumer;

/**
 * @author LeanderK
 * @version 1.0
 */
public class IzouSoundLine extends IzouModule implements Line, AutoCloseable {
    protected final Line line;
    private final boolean isPermanent;
    protected final SoundManager soundManager;
    private final AddOnModel addOnModel;
    private boolean isMuted;
    private Consumer<Void> closeCallback = null;

    public IzouSoundLine(Line line, Main main, boolean isPermanent, AddOnModel addOnModel) {
        super(main);
        this.line = line;
        this.isPermanent = isPermanent;
        this.addOnModel = addOnModel;
        if (!isPermanent)
            main.getThreadPoolManager().getIzouThreadPool().submit(() -> {
                try {
                    Thread.sleep(600000);
                } catch (InterruptedException e) {
                    error("interrupted while sleeping");
                }
                if (line.isOpen()) {
                    debug("closing line " + line + "for Addon " + addOnModel);
                    line.close();
                }
            });
        soundManager = null;
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
        line.open();
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
        line.close();
        if (closeCallback != null)
            closeCallback.accept(null);
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
     * currently not implemented, throws an UnsupportedOperationException.
     * @return an error
     */
    @Override
    public Control[] getControls() {
        throw new UnsupportedOperationException("method not available");
    }

    /**
     * currently not implemented, throws an UnsupportedOperationException.
     * @param control the control to check
     * @return an error
     */
    @Override
    public boolean isControlSupported(Control.Type control) {
        throw new UnsupportedOperationException("method not available");
    }

    /**
     * currently not implemented, throws an UnsupportedOperationException.
     * @param control the control
     * @return an error
     */
    @Override
    public Control getControl(Control.Type control) {
        throw new UnsupportedOperationException("method not available");
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
    public boolean isPermanent() {
        return isPermanent;
    }

    /**
     * gets the associated AddonModel
     * @return the AddonModel
     */
    public AddOnModel getAddOnModel() {
        return addOnModel;
    }

    /**
     * returns whether the Line is muted
     * @return true if muted.
     */
    public boolean isMuted() {
        return isMuted;
    }

    void setMuted(boolean isMuted) {
        this.isMuted = isMuted;
    }

    void registerCloseCallback(Consumer<Void> consumer) {
        this.closeCallback = consumer;
    }
}
