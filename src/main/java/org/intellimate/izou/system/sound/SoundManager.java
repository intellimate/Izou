package org.intellimate.izou.system.sound;

import org.intellimate.izou.IzouModule;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.List;

/**
 * @author LeanderK
 * @version 1.0
 */
public class SoundManager extends IzouModule {
    private HashMap<AddOnModel, List<WeakReference<IzouSoundLine>>> soundLines = new HashMap<>();
    public SoundManager(Main main) {
        super(main);
    }


}
