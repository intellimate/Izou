package org.intellimate.izou.system.sound;

import java.util.Arrays;
import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class SoundIDs {
    public static class StartEvent {
        public static final String descriptor = "izou.sound.events.start";
    }
    public static class StopEvent {
        public static final String type = "response";
        public static final List<String> descriptors = Arrays.asList("notinterrupt", "izou.sound.events.stop");
        public static final String descriptor = "izou.sound.events.stop";
    }
}
