package intellimate.izou.threadpool;

/**
 * classes who implement this interface get notified when the thread submitted to the ThreadPool crashes.
 * @author LeanderK
 * @version 1.0
 */
public interface ExceptionCallback {
    /**
     * this method gets called when the task submitted to the ThreadPool crashes
     * @param e the exception catched
     */
    public void exceptionThrown(Exception e);
}
