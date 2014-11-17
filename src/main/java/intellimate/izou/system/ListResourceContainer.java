package intellimate.izou.system;

import java.util.LinkedList;
import java.util.List;

/**
 * A ResourceContainer which holds all the Resources in an List internally
 */
public class ListResourceContainer implements ResourceContainer{
    /**
     * checks whether it can provide the resource
     *
     * @param resource the resource to provide
     * @return true if the container can provide the resource
     */
    @Override
    public boolean providesResource(Resource resource) {
        return false;
    }

    /**
     * checks whether there are any resources registered from the source
     *
     * @param sourceID the ID of the source
     * @return true if the container has resources from the source
     */
    @Override
    public boolean containsResourcesFromSource(String sourceID) {
        return false;
    }

    /**
     * checks whether the ResourceContainer can provide at least ONE resource
     *
     * @param resourcesID a list containing sources
     * @return true if the ResourceContainer can provide at least one resource
     */
    @Override
    public boolean providesResource(List<String> resourcesID) {
        return false;
    }

    /**
     * returns all EXISTING resources for the ID.
     * If there are no resources for the ID the ID will get skipped
     *
     * @param resourceIDs an Array containing the resources
     * @return a list of resources found
     */
    @Override
    public LinkedList<Resource> provideResource(String[] resourceIDs) {
        return null;
    }

    /**
     * returns the resource (if existing)
     *
     * @param resourceID the ID of the resource
     * @return a list of resources found
     */
    @Override
    public Resource provideResource(String resourceID) {
        return null;
    }

    /**
     * returns the resource (if existing) from the source
     *
     * @param sourceID the ID of the source
     * @return a list containing all the found resources
     */
    public LinkedList<Resource> provideResourceFromSource(String sourceID) {
        return null;
    }
}
