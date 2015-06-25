package org.intellimate.izou.system.sound;

import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;

/**
 * @author LeanderK
 * @version 1.0
 */
class FakeGainControl extends FloatControl {
    private IzouSoundLineBaseClass izouSoundLineBaseClass;
    private final FloatControl floatControl;

    protected FakeGainControl(IzouSoundLineBaseClass izouSoundLineBaseClass, FloatControl floatControl) {
        super((Type) floatControl.getType(), floatControl.getMinimum(), floatControl.getMaximum(),
                floatControl.getPrecision(), floatControl.getUpdatePeriod(), floatControl.getValue(),
                floatControl.getUnits(), floatControl.getMinLabel(), floatControl.getMidLabel(),
                floatControl.getMaxLabel());
        this.izouSoundLineBaseClass = izouSoundLineBaseClass;
        this.floatControl = floatControl;
    }


    /**
     * Sets the current value for the control.  The default implementation
     * simply sets the value as indicated.  If the value indicated is greater
     * than the maximum value, or smaller than the minimum value, an
     * IllegalArgumentException is thrown.
     * Some controls require that their line be open before they can be affected
     * by setting a value.
     *
     * @param newValue desired new value
     * @throws IllegalArgumentException if the value indicated does not fall
     *                                  within the allowable range
     */
    @Override
    public void setValue(float newValue) {
        if (newValue > getMaximum() || newValue < getMinimum())
            throw new IllegalArgumentException(newValue + " does not fall within the allowed range");
        if (izouSoundLineBaseClass.notDisabled)
            floatControl.setValue(newValue);
        izouSoundLineBaseClass.gain = newValue;
    }

    /**
     * Obtains this control's current value.
     *
     * @return the current value
     */
    @Override
    public float getValue() {
        return izouSoundLineBaseClass.gain;
    }

    /**
     * Obtains the maximum value permitted.
     *
     * @return the maximum allowable value
     */
    @Override
    public float getMaximum() {
        return floatControl.getMaximum();
    }

    /**
     * Obtains the minimum value permitted.
     *
     * @return the minimum allowable value
     */
    @Override
    public float getMinimum() {
        return floatControl.getMinimum();
    }

    /**
     * Obtains the label for the units in which the control's values are expressed,
     * such as "dB" or "frames per second."
     *
     * @return the units label, or a zero-length string if no label
     */
    @Override
    public String getUnits() {
        return floatControl.getUnits();
    }

    /**
     * Obtains the label for the minimum value, such as "Left" or "Off."
     *
     * @return the minimum value label, or a zero-length string if no label      * has been set
     */
    @Override
    public String getMinLabel() {
        return floatControl.getMinLabel();
    }

    /**
     * Obtains the label for the mid-point value, such as "Center" or "Default."
     *
     * @return the mid-point value label, or a zero-length string if no label    * has been set
     */
    @Override
    public String getMidLabel() {
        return floatControl.getMidLabel();
    }

    /**
     * Obtains the label for the maximum value, such as "Right" or "Full."
     *
     * @return the maximum value label, or a zero-length string if no label      * has been set
     */
    @Override
    public String getMaxLabel() {
        return floatControl.getMaxLabel();
    }

    /**
     * Obtains the resolution or granularity of the control, in the units
     * that the control measures.
     * The precision is the size of the increment between discrete valid values
     * for this control, over the set of supported floating-point values.
     *
     * @return the control's precision
     */
    @Override
    public float getPrecision() {
        return floatControl.getPrecision();
    }

    /**
     * Obtains the smallest time interval, in microseconds, over which the control's value can
     * change during a shift.  The update period is the inverse of the frequency with which
     * the control updates its value during a shift.  If the implementation does not support value shifting over
     * time, it should set the control's value to the final value immediately
     * and return -1 from this method.
     *
     * @return update period in microseconds, or -1 if shifting over time is unsupported
     * @see #shift
     */
    @Override
    public int getUpdatePeriod() {
        return floatControl.getUpdatePeriod();
    }

    /**
     * Changes the control value from the initial value to the final
     * value linearly over the specified time period, specified in microseconds.
     * This method returns without blocking; it does not wait for the shift
     * to complete.  An implementation should complete the operation within the time
     * specified.  The default implementation simply changes the value
     * to the final value immediately.
     *
     * @param from         initial value at the beginning of the shift
     * @param to           final value after the shift
     * @param microseconds maximum duration of the shift in microseconds
     * @throws IllegalArgumentException if either {@code from} or {@code to}
     *                                  value does not fall within the allowable range
     * @see #getUpdatePeriod
     */
    @Override
    public void shift(float from, float to, int microseconds) {
        floatControl.shift(from, to, microseconds);
    }

    /**
     * Provides a string representation of the control
     *
     * @return a string description
     */
    @Override
    public String toString() {
        return floatControl.toString();
    }

    /**
     * Obtains the control's type.
     *
     * @return the control's type.
     */
    @Override
    public Control.Type getType() {
        return floatControl.getType();
    }
}
