package org.intellimate.izou.system.sound.replaced;

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
        System.out.println("request to add line");
        pjp.getArgs();
        Line line = (Line) pjp.proceed();
        return MixerAspectInitializer.getAndRegisterLine(line);
    }
}
