package org.intellimate.izou.system.sound;

import javax.sound.sampled.BooleanControl;

/**
 * @author LeanderK
 * @version 1.0
 */
class FakeMuteControl extends BooleanControl {
    private IzouSoundLineBaseClass izouSoundLineBaseClass;
    private final BooleanControl control;

    /**
     * Constructs a new boolean control object with the given parameters.
     * The labels for the <code>true</code> and <code>false</code> states
     * default to "true" and "false."
     */
    protected FakeMuteControl(IzouSoundLineBaseClass izouSoundLineBaseClass) {
        super(Type.MUTE, izouSoundLineBaseClass.isMutedFromUser);
        this.izouSoundLineBaseClass = izouSoundLineBaseClass;
        if (izouSoundLineBaseClass.line != null) {
            this.control = (BooleanControl) izouSoundLineBaseClass.line.getControl(Type.MUTE);
        } else {
            control = null;
        }
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
        izouSoundLineBaseClass.isMutedFromUser = value;
        if (izouSoundLineBaseClass.isMutedFromSystem && !izouSoundLineBaseClass.notDisabled) {
            if (control != null)
                control.setValue(izouSoundLineBaseClass.isMutedFromUser);
        }
    }

    /**
     * Obtains this control's current value.
     *
     * @return current value.
     */
    @Override
    public boolean getValue() {
        return izouSoundLineBaseClass.isMutedFromUser;
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
