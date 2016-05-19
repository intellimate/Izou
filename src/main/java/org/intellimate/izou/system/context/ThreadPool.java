package org.intellimate.izou.system.context;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import org.intellimate.izou.identification.IllegalIDException;
import ro.fortsoft.pf4j.AddonAccessible;

import java.util.concurrent.ExecutorService;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface ThreadPool {
    /**
     * returns an ThreadPool where all the IzouPlugins are running
     * @param identifiable the Identifiable to set each created Task as the Source
     * @return an instance of ExecutorService
     * @throws IllegalIDException not implemented yet
     */
    ExecutorService getThreadPool(Identifiable identifiable) throws IllegalIDException;

    /**
     * tries everything to log the exception
     * @param throwable the Throwable
     * @param target an instance of the thing which has thrown the Exception
     */
    void handleThrowable(Throwable throwable, Object target);

    /**
     * returns the ID of the Manager
     * @return an instance of Identification
     */
    Identification getManagerIdentification();
}
