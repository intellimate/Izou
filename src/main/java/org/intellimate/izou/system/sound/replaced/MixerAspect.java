package org.intellimate.izou.system.sound.replaced;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.intellimate.izou.addon.AddOnModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.system.sound.*;

import javax.sound.sampled.*;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.SourceDataLine;
import java.util.Optional;

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
     * @param lineInfo the lineInfo
     * @param pjp the PointCut
     * @return an IzouSoundLine if an addon requested the lineInfo
     */
    static Line createAndRegisterLine(Line.Info lineInfo, ProceedingJoinPoint pjp) throws Throwable {
        AddOnModel addOnModel;
        Optional<AddOnModel> addOnModelForClassLoader = main.getSecurityManager().getAddOnModelForClassLoader();
        if (!addOnModelForClassLoader.isPresent()) {
            logger.debug("the SoundManager will not manage this line, obtained by system");
            return (Line) pjp.proceed();
        } else {
            addOnModel = addOnModelForClassLoader.get();
        }
        Class<?> lineClazz = lineInfo.getLineClass();
        IzouSoundLineBaseClass izouSoundLine;
        if (SourceDataLine.class.isAssignableFrom(lineClazz)) {
            if (Clip.class.isAssignableFrom(lineClazz)) {
                izouSoundLine = new IzouSoundLineClipAndSDLine(lineInfo, main, false, addOnModel);
            } else {
                izouSoundLine = new IzouSoundSourceDataLine(lineInfo, main, false, addOnModel);
            }
        } else if (Clip.class.isAssignableFrom(lineClazz)) {
            izouSoundLine = new IzouSoundLineClip(lineInfo, main, false, addOnModel);
        } else if (DataLine.class.isAssignableFrom(lineClazz)) {
            izouSoundLine = new IzouSoundDataLine(lineInfo, main, false, addOnModel);
        } else {
            izouSoundLine = new IzouSoundLineBaseClass(lineInfo, main, false, addOnModel);
        }
        main.getSoundManager().addIzouSoundLine(addOnModel, izouSoundLine);
        return izouSoundLine;
    }

    @Around("execution(* javax.sound.sampled.AudioSystem.getLine(javax.sound.sampled.Line.Info))")
    public Object getLineAdvice(ProceedingJoinPoint pjp) throws Throwable {
        Line.Info info = (Line.Info) pjp.getArgs()[0];
        if (AudioSystem.isLineSupported(info)) {
            return MixerAspect.createAndRegisterLine(info, pjp);
        } else {
            throw new IllegalArgumentException("line for info:" + info + "not supported");
        }
    }
}
