package intellimate.izou.system;

import java.util.List;

/**
 * This interface is used to provide resources to other parts of the application.
 */
public interface ResourceProvider {
    /**
     * this method is called to register what resources the object provides.
     * just pass a List of Resources without Data in it.
     * @return a List containing the resources the object provides
     */
    abstract List<Resource> announceResources();

    /**
     * this method is called when an object wants to get a Resource.
     * it has as an argument resource instances without data, which just need to get populated.
     * @param resources a list of resources without data
     * @return a list of resources with data
     */
    abstract List<Resource> provideResource(List<Resource> resources);
}