package org.intellimate.izou.system.sound;

import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;

import javax.sound.sampled.*;
import java.io.IOException;

/**
 * the delegation to Clip and SourceDataLine.
 * @author LeanderK
 * @version 1.0
 */
public class IzouSoundLineClipAndSDLine extends IzouSoundDataLine implements Clip, SourceDataLine {
    private final Clip clip;
    private final SourceDataLine sourceDataLine;

    public IzouSoundLineClipAndSDLine(Clip clip, SourceDataLine sourceDataLine, Main main, boolean isPermanent, AddOnModel addOnModel) {
        super(clip, main, isPermanent, addOnModel);
        this.clip = clip;
        this.sourceDataLine = sourceDataLine;
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
     * queried by subsequently calling <code>{@link DataLine#getBufferSize}</code>.
     * <p>
     * If this operation succeeds, the line is marked as open, and an
     * <code>{@link LineEvent.Type#OPEN OPEN}</code> event is dispatched to the
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
     * @see Line#open
     * @see Line#close
     * @see Line#isOpen
     * @see LineEvent
     */
    @Override
    public void open(AudioFormat format, int bufferSize) throws LineUnavailableException {
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
     * <code>{@link DataLine#getBufferSize}</code>.
     * <p>
     * If this operation succeeds, the line is marked as open, and an
     * <code>{@link LineEvent.Type#OPEN OPEN}</code> event is dispatched to the
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
     * @see Line#open
     * @see Line#close
     * @see Line#isOpen
     * @see LineEvent
     */
    @Override
    public void open(AudioFormat format) throws LineUnavailableException {
        sourceDataLine.open(format);
    }

    /**
     * Writes audio data to the mixer via this source data line.  The requested
     * number of bytes of data are read from the specified array,
     * starting at the given offset into the array, and written to the data
     * line's buffer.  If the caller attempts to write more data than can
     * currently be written (see <code>{@link DataLine#available available}</code>),
     * this method blocks until the requested amount of data has been written.
     * This applies even if the requested amount of data to write is greater
     * than the data line's buffer size.  However, if the data line is closed,
     * stopped, or flushed before the requested amount has been written,
     * the method no longer blocks, but returns the number of bytes
     * written thus far.
     * <p>
     * The number of bytes that can be written without blocking can be ascertained
     * using the <code>{@link DataLine#available available}</code> method of the
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
     *
     * @see TargetDataLine#read
     * @see DataLine#available
     */
    @Override
    public int write(byte[] b, int off, int len) {
        return sourceDataLine.write(b, off, len);
    }

    /**
     * Opens the clip, meaning that it should acquire any required
     * system resources and become operational.  The clip is opened
     * with the format and audio data indicated.
     * If this operation succeeds, the line is marked as open and an
     * <code>{@link LineEvent.Type#OPEN OPEN}</code> event is dispatched
     * to the line's listeners.
     * <p>
     * Invoking this method on a line which is already open is illegal
     * and may result in an IllegalStateException.
     * <p>
     * Note that some lines, once closed, cannot be reopened.  Attempts
     * to reopen such a line will always result in a
     * <code>{@link LineUnavailableException}</code>.
     *
     * @param format the format of the supplied audio data
     * @param data a byte array containing audio data to load into the clip
     * @param offset the point at which to start copying, expressed in
     * <em>bytes</em> from the beginning of the array
     * @param bufferSize the number of <em>bytes</em>
     * of data to load into the clip from the array.
     * @throws LineUnavailableException if the line cannot be
     * opened due to resource restrictions
     * @throws IllegalArgumentException if the buffer size does not represent
     * an integral number of sample frames,
     * or if <code>format</code> is not fully specified or invalid
     * @throws IllegalStateException if the line is already open
     * @throws SecurityException if the line cannot be
     * opened due to security restrictions
     *
     * @see #close
     * @see #isOpen
     * @see LineListener
     */
    @Override
    public void open(AudioFormat format, byte[] data, int offset, int bufferSize) throws LineUnavailableException {
        clip.open(format, data, offset, bufferSize);
    }

    /**
     * Opens the clip with the format and audio data present in the provided audio
     * input stream.  Opening a clip means that it should acquire any required
     * system resources and become operational.  If this operation
     * input stream.  If this operation
     * succeeds, the line is marked open and an
     * <code>{@link LineEvent.Type#OPEN OPEN}</code> event is dispatched
     * to the line's listeners.
     * <p>
     * Invoking this method on a line which is already open is illegal
     * and may result in an IllegalStateException.
     * <p>
     * Note that some lines, once closed, cannot be reopened.  Attempts
     * to reopen such a line will always result in a
     * <code>{@link LineUnavailableException}</code>.
     *
     * @param stream an audio input stream from which audio data will be read into
     * the clip
     * @throws LineUnavailableException if the line cannot be
     * opened due to resource restrictions
     * @throws IOException if an I/O exception occurs during reading of
     * the stream
     * @throws IllegalArgumentException if the stream's audio format
     * is not fully specified or invalid
     * @throws IllegalStateException if the line is already open
     * @throws SecurityException if the line cannot be
     * opened due to security restrictions
     *
     * @see #close
     * @see #isOpen
     * @see LineListener
     */
    @Override
    public void open(AudioInputStream stream) throws LineUnavailableException, IOException {
        clip.open(stream);
    }

    /**
     * Obtains the media length in sample frames.
     * @return the media length, expressed in sample frames,
     * or <code>AudioSystem.NOT_SPECIFIED</code> if the line is not open.
     * @see AudioSystem#NOT_SPECIFIED
     */
    @Override
    public int getFrameLength() {
        return clip.getFrameLength();
    }

    /**
     * Obtains the media duration in microseconds
     * @return the media duration, expressed in microseconds,
     * or <code>AudioSystem.NOT_SPECIFIED</code> if the line is not open.
     * @see AudioSystem#NOT_SPECIFIED
     */
    @Override
    public long getMicrosecondLength() {
        return clip.getMicrosecondLength();
    }

    /**
     * Sets the media position in sample frames.  The position is zero-based;
     * the first frame is frame number zero.  When the clip begins playing the
     * next time, it will start by playing the frame at this position.
     * <p>
     * To obtain the current position in sample frames, use the
     * <code>{@link DataLine#getFramePosition getFramePosition}</code>
     * method of <code>DataLine</code>.
     *
     * @param frames the desired new media position, expressed in sample frames
     */
    @Override
    public void setFramePosition(int frames) {
        clip.setFramePosition(frames);
    }

    /**
     * Sets the media position in microseconds.  When the clip begins playing the
     * next time, it will start at this position.
     * The level of precision is not guaranteed.  For example, an implementation
     * might calculate the microsecond position from the current frame position
     * and the audio sample frame rate.  The precision in microseconds would
     * then be limited to the number of microseconds per sample frame.
     * <p>
     * To obtain the current position in microseconds, use the
     * <code>{@link DataLine#getMicrosecondPosition getMicrosecondPosition}</code>
     * method of <code>DataLine</code>.
     *
     * @param microseconds the desired new media position, expressed in microseconds
     */
    @Override
    public void setMicrosecondPosition(long microseconds) {
        clip.setMicrosecondPosition(microseconds);
    }

    /**
     * Sets the first and last sample frames that will be played in
     * the loop.  The ending point must be greater than
     * or equal to the starting point, and both must fall within the
     * the size of the loaded media.  A value of 0 for the starting
     * point means the beginning of the loaded media.  Similarly, a value of -1
     * for the ending point indicates the last frame of the media.
     * @param start the loop's starting position, in sample frames (zero-based)
     * @param end the loop's ending position, in sample frames (zero-based), or
     * -1 to indicate the final frame
     * @throws IllegalArgumentException if the requested
     * loop points cannot be set, usually because one or both falls outside
     * the media's duration or because the ending point is
     * before the starting point
     */
    @Override
    public void setLoopPoints(int start, int end) {
        clip.setLoopPoints(start, end);
    }

    /**
     * Starts looping playback from the current position.   Playback will
     * continue to the loop's end point, then loop back to the loop start point
     * <code>count</code> times, and finally continue playback to the end of
     * the clip.
     * <p>
     * If the current position when this method is invoked is greater than the
     * loop end point, playback simply continues to the
     * end of the clip without looping.
     * <p>
     * A <code>count</code> value of 0 indicates that any current looping should
     * cease and playback should continue to the end of the clip.  The behavior
     * is undefined when this method is invoked with any other value during a
     * loop operation.
     * <p>
     * If playback is stopped during looping, the current loop status is
     * cleared; the behavior of subsequent loop and start requests is not
     * affected by an interrupted loop operation.
     *
     * @param count the number of times playback should loop back from the
     * loop's end position to the loop's  start position, or
     * <code>{@link #LOOP_CONTINUOUSLY}</code> to indicate that looping should
     * continue until interrupted
     */
    @Override
    public void loop(int count) {
        clip.loop(count);
    }
}
