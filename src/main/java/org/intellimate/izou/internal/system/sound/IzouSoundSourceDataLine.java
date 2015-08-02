package org.intellimate.izou.internal.system.sound;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.internal.main.Main;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * the delegation to SourceDataLine.
 * @author LeanderK
 * @version 1.0
 */
public class IzouSoundSourceDataLine extends IzouSoundDataLine implements SourceDataLine {
    private final SourceDataLine sourceDataLine;

    public IzouSoundSourceDataLine(SourceDataLine dataLine, Main main, boolean isPermanent, AddOnModel addOnModel) {
        super(dataLine, main, isPermanent, addOnModel);
        this.sourceDataLine = dataLine;
    }

    /**
     * Opens the line with the specified format and suggested buffer size,
     * causing the line to acquire any required
     * system resources and become operational.
     * <p>
     * The buffer size is specified in bytes, but must represent an integral
     * number of sample frames.  Invoking this method with a requested buffer
     * size that does not meet this requirement may result in an
     * IllegalArgumentException.  The actual buffer size for the open line may
     * differ from the requested buffer size.  The value actually set may be
     * queried by subsequently calling <code>DataLine#getBufferSize</code>.
     * <p>
     * If this operation succeeds, the line is marked as open, and an
     * <code>LineEvent.Type#OPEN OPEN</code> event is dispatched to the
     * line's listeners.
     * <p>
     * Invoking this method on a line which is already open is illegal
     * and may result in an <code>IllegalStateException</code>.
     * <p>
     * Note that some lines, once closed, cannot be reopened.  Attempts
     * to reopen such a line will always result in a
     * <code>LineUnavailableException</code>.
     *
     * @param format the desired audio format
     * @param bufferSize the desired buffer size
     * @throws LineUnavailableException if the line cannot be
     * opened due to resource restrictions
     * @throws IllegalArgumentException if the buffer size does not represent
     * an integral number of sample frames,
     * or if <code>format</code> is not fully specified or invalid
     * @throws IllegalStateException if the line is already open
     * @throws SecurityException if the line cannot be
     * opened due to security restrictions
     *
     * @see #open(AudioFormat)
     */
    @Override
    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
        opening();
        sourceDataLine.open(format, bufferSize);
    }

    /**
     * Opens the line with the specified format, causing the line to acquire any
     * required system resources and become operational.
     *
     * <p>
     * The implementation chooses a buffer size, which is measured in bytes but
     * which encompasses an integral number of sample frames.  The buffer size
     * that the system has chosen may be queried by subsequently calling
     * <code>DataLine#getBufferSize</code>.
     * <p>
     * If this operation succeeds, the line is marked as open, and an
     * <code>LineEvent.Type#OPEN OPEN</code> event is dispatched to the
     * line's listeners.
     * <p>
     * Invoking this method on a line which is already open is illegal
     * and may result in an <code>IllegalStateException</code>.
     * <p>
     * Note that some lines, once closed, cannot be reopened.  Attempts
     * to reopen such a line will always result in a
     * <code>LineUnavailableException</code>.
     *
     * @param format the desired audio format
     * @throws LineUnavailableException if the line cannot be
     * opened due to resource restrictions
     * @throws IllegalArgumentException if <code>format</code>
     * is not fully specified or invalid
     * @throws IllegalStateException if the line is already open
     * @throws SecurityException if the line cannot be
     * opened due to security restrictions
     *
     * @see #open(AudioFormat, int)
     */
    @Override
    public void open(AudioFormat format) throws LineUnavailableException {
        opening();
        sourceDataLine.open(format);
    }

    /**
     * Writes audio data to the mixer via this source data line.  The requested
     * number of bytes of data are read from the specified array,
     * starting at the given offset into the array, and written to the data
     * line's buffer.  If the caller attempts to write more data than can
     * currently be written (see <code>DataLine#available available</code>),
     * this method blocks until the requested amount of data has been written.
     * This applies even if the requested amount of data to write is greater
     * than the data line's buffer size.  However, if the data line is closed,
     * stopped, or flushed before the requested amount has been written,
     * the method no longer blocks, but returns the number of bytes
     * written thus far.
     * <p>
     * The number of bytes that can be written without blocking can be ascertained
     * using the <code>DataLine#available available</code> method of the
     * <code>DataLine</code> interface.  (While it is guaranteed that
     * this number of bytes can be written without blocking, there is no guarantee
     * that attempts to write additional data will block.)
     * <p>
     * The number of bytes to write must represent an integral number of
     * sample frames, such that:
     * <br>
     * <center><code>[ bytes written ] % [frame size in bytes ] == 0</code></center>
     * <br>
     * The return value will always meet this requirement.  A request to write a
     * number of bytes representing a non-integral number of sample frames cannot
     * be fulfilled and may result in an <code>IllegalArgumentException</code>.
     *
     * @param b a byte array containing data to be written to the data line
     * @param off the offset from the beginning of the array, in bytes
     * @param len the length, in bytes, of the valid data in the array
     * (in other words, the requested amount of data to write, in bytes)
     * @return the number of bytes actually written
     * @throws IllegalArgumentException if the requested number of bytes does
     * not represent an integral number of sample frames,
     * or if <code>len</code> is negative
     * @throws ArrayIndexOutOfBoundsException if <code>off</code> is negative,
     * or <code>off+len</code> is greater than the length of the array
     * <code>b</code>.
     */
    @Override
    public int write(byte[] b, int off, int len) {
        if (isMutable) {
            return sourceDataLine.write(b, off, len);
        } else {
            if (isMutedFromSystem) {
                byte[] newArr = new byte[b.length];
                return sourceDataLine.write(newArr, off, len);
            } else {
                return sourceDataLine.write(b, off, len);
            }
        }
    }
}
