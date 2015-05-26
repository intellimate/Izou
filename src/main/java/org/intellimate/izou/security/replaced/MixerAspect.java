package org.intellimate.izou.security.replaced;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.intellimate.izou.main.Main;

import javax.sound.sampled.Line;

/**
 * @author LeanderK
 * @version 1.0
 */
@Aspect
public class MixerAspect {
    static Main main;

    @Around("execution(* javax.sound.sampled.AudioSystem.getLine(javax.sound.sampled.Line.Info))")
    public Object getLineAdvice(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("unbelievable!");
        pjp.getArgs();
        Line ret = (Line) pjp.proceed();
        System.out.println(MixerAspectInitializer.main);
        return ret;
    }
}
