package org.intellimate.izou.security.replaced;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;

import javax.sound.sampled.Line;

/**
 * @author LeanderK
 * @version 1.0
 */
@Aspect
public class MixerAspect {
    @Around("execution(Line javax.sound.sampled.Mixer.getLine(Line.Info info))")
    public Object getLineAdvice(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("AspectJ");
        pjp.getArgs();
        Line ret = (Line) pjp.proceed();
        return ret;
    }

    @Around("execution(Line[] javax.sound.sampled.Mixer.getSourceLines())")
    public Object getLinesAdvice(ProceedingJoinPoint pjp) throws Throwable {
        System.out.println("AspectJ");
        Line ret = (Line) pjp.proceed();
        return ret;
    }
}
