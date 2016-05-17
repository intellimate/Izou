package org.intellimate.izou.threadpool;

import ro.fortsoft.pf4j.AddonAccessible;

/**
 * classes who implement this interface get notified when the thread submitted to the ThreadPool crashes.
 * @author LeanderK
 * @version 1.0
 */
@AddonAccessible
public interface ExceptionCallback {
    /**
     * this method gets called when the task submitted to the ThreadPool crashes
     * @param e the exception catched
     */
    void exceptionThrown(Exception e);
}
