package org.intellimate.izou.security.replaced;

import org.intellimate.izou.main.Main;

/**
 * @author LeanderK
 * @version 1.0
 */
public class MixerAspectInitializer {
    static Main main = null;

    public static synchronized void init(Main main) {
        if (MixerAspectInitializer.main == null)
            MixerAspectInitializer.main = main;
    }
}
