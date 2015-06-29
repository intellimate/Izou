package org.intellimate.izou.system.sound.replaced;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.security.exceptions.IzouPermissionException;
import org.intellimate.izou.system.sound.*;

import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;

/**
 * @author LeanderK
 * @version 1.0
 */
@Aspect
public class MixerAspect {
    static Main main;
    private static final Logger logger= LogManager.getLogger(MixerAspect.class);

    public static synchronized void init(Main main) {
        if (MixerAspect.main == null)
            MixerAspect.main = main;
    }

    /**
     * creates the appropriate IzouSoundLine if the request originates from an AddOn.
     * @param line the line
     * @return an IzouSoundLine if an addon requested the line
     */
    static Line getAndRegisterLine(Line line) {
        AddOnModel addOnModel;
        try {
            addOnModel = main.getSecurityManager().getOrThrowAddOnModelForClassLoader();
        } catch (IzouPermissionException e) {
            logger.debug("the SoundManager will not manage this line, obtained by system");
            return line;
        }
        IzouSoundLineBaseClass izouSoundLine;
        if (line instanceof SourceDataLine) {
            if (line instanceof Clip) {
                izouSoundLine = new IzouSoundLineClipAndSDLine((Clip) line, (SourceDataLine) line, main, false, addOnModel);
            } else {
                izouSoundLine = new IzouSoundSourceDataLine((SourceDataLine) line, main, false, addOnModel);
            }
        } else if (line instanceof Clip) {
            izouSoundLine = new IzouSoundLineClip((Clip) line, main, false, addOnModel);
        } else if (line instanceof DataLine) {
            izouSoundLine = new IzouSoundDataLine((DataLine) line, main, false, addOnModel);
        } else {
            izouSoundLine = new IzouSoundLineBaseClass(line, main, false, addOnModel);
        }
        main.getSoundManager().addIzouSoundLine(addOnModel, izouSoundLine);
        return izouSoundLine;
    }

    @Around("execution(* javax.sound.sampled.AudioSystem.getLine(javax.sound.sampled.Line.Info))")
    public Object getLineAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Line line = (Line) pjp.proceed();
        if (line instanceof IzouSoundLineBaseClass) {
            return line;
        } else {
            return MixerAspect.getAndRegisterLine(line);
        }
    }
}
