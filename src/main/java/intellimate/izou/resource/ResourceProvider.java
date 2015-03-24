package intellimate.izou.resource;

import java.util.List;

/**
 * This interface is used to provide resources
 */
public interface ResourceProvider {

    /**
     * checks whether it can provide the resource
     * beware that the implementation may vary. It can time out etc.
     * @param resource the resource to provide
     * @return true if the container can provide the resource
     */
    abstract boolean providesResource(ResourceModel resource);

    /**
     * checks whether there are any resources registered from the source
     * beware that the implementation may vary. It can time out etc.
     * @param sourceID the ID of the source
     * @return true if the container has resources from the source
     */
    abstract boolean containsResourcesFromSource(String sourceID);

    /**
     * checks whether the ResourceContainer can provide at least ONE resource
     * beware that the implementation may vary. It can time out etc.
     * @param resourcesID a list containing sources
     * @return true if the ResourceContainer can provide at least one resource
     */
    abstract boolean providesResource(List<String> resourcesID);

    /**
     * returns all EXISTING resources for the ID.
     * If there are no resources for the ID the ID will get skipped
     * beware that the implementation may vary. It can time out etc.
     * @param resourceIDs an Array containing the resources
     * @return a list of resources found
     */
    abstract List<ResourceModel> provideResource(String[] resourceIDs);

    /**
     * returns the resource (if existing)
     * beware that the implementation may vary. It can time out etc.
     * @param resourceID the ID of the resource
     * @return a list of resources found
     */
    abstract List<ResourceModel> provideResource(String resourceID);

    /**
     * returns the resource (if existing) from the source
     *
     * @param sourceID the ID of the source
     * @return a list containing all the found resources
     */
    public List<ResourceModel> provideResourceFromSource(String sourceID);
}
