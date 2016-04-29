package org.intellimate.izou.output;

import org.intellimate.izou.identification.Identifiable;

/**
 * <p>
 *     An OutputControllerModel is the most basic abstraction for an OutputController. The purpose of this
 *     OutputController is to control the external output device that is attached to Izou in correlation to the
 *     {@link OutputPluginModel} that uses the external output device.
 * </p>
 * <p>
 *     For example, a music addon might control what content is played on a TV for example, but it might not
 *     control the state of the TV, that is whether it is turned on or off, what mode the TV is in etc. So prior
 *     to the playback of music, the output plugin might call the output controller for the TV and tell it to turn
 *     itself on and to switch to the audio playback mode so that the music can actually be heard.
 * </p>
 * <p>
 *     So the OutputController controls the state of an external output device.
 * </p>
 *
 * @author Julian Brendl
 * @version 1.0
 */
public interface OutputControllerModel extends Identifiable {
    /**
     * Turns the external output device that this OutputControllerModel controls on.
     *
     * @return Returns true if the device was turned on successfully.
     */
    boolean turnOn();

    /**
     * Turns the external output device that this OutputControllerModel controls off
     *
     * @return Returns true if the device was turned off successfully.
     */
    boolean turnOff();
}
