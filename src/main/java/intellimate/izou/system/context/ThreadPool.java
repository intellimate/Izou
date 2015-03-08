package intellimate.izou.system.context;

import intellimate.izou.identification.Identifiable;
import intellimate.izou.identification.Identification;
import intellimate.izou.identification.IllegalIDException;

import java.util.concurrent.ExecutorService;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface ThreadPool {
    /**
     * returns an ThreadPool where all the IzouPlugins are running
     * @param identifiable the Identifiable to set each created Task as the Source
     * @return an instance of ExecutorService
     * @throws IllegalIDException not implemented yet
     */
    ExecutorService getThreadPool(Identifiable identifiable) throws IllegalIDException;

    /**
     * returns the ID of the Manager
     */
    Identification getManagerIdentification();
}
