package org.intellimate.izou.system.sound;

import java.util.Arrays;
import java.util.List;

/**
 * this class holds various descriptors used by the SoundManager to communicate with the Addons.
 * @author LeanderK
 * @version 1.0
 */
public class SoundIDs {

    public static class StartRequest {
        public static final String descriptor = "izou.music.events.startrequest";
        //add this descriptor to the Event if you are using non-java code to play music
        public static final String isUsingNonJava = "izou.sound.events.start.nonjava";
    }

    public static class StartEvent {
        public static final String descriptor = "izou.sound.events.start";
        //add this descriptor to the Event if you are using non-java code to play music
        public static final String isUsingNonJava = "izou.sound.events.start.nonjava";
    }

    public static class EndedEvent {
        public static final String type = "response";
        public static final List<String> descriptors = Arrays.asList("notinterrupt", "izou.sound.events.ended");
        public static final String descriptor = "izou.sound.events.ended";
    }

    public static class MuteEvent {
        public static final String type = "response";
        public static final List<String> descriptors = Arrays.asList("notinterrupt", "izou.sound.events.mute");
        public static final String descriptor = "izou.sound.events.mute";
        public static final String resourceSelector = "izou.common.resource.selector";
    }

    public static class UnMuteEvent {
        public static final String type = "response";
        public static final List<String> descriptors = Arrays.asList("notinterrupt", "izou.sound.events.unmute");
        public static final String descriptor = "izou.sound.events.unmute";
    }

    public static class StopEvent {
        public static final String type = "response";
        public static final List<String> descriptors = Arrays.asList("notinterrupt", "izou.sound.events.stop");
        public static final String resourceSelector = "izou.common.resource.selector";
    }
}
