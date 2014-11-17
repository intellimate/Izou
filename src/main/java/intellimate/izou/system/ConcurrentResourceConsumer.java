package intellimate.izou.system;

import java.util.List;

/**
 * Use this interface to access Resources from other parts of the application
 */
@Deprecated
public interface ConcurrentResourceConsumer {
    /**
     * this method gets called, when the resources get delivered.
     * The Implementation shouldn't contain much Logic, it should return quickly.
     * @param resources a List of resources which contain the data
     */
    abstract void resourceDeliveredCallBack(List<Resource> resources);
}
