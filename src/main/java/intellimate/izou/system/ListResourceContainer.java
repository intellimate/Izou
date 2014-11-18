package intellimate.izou.system;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * A ResourceContainer which holds all the Resources in an List internally
 */
public class ListResourceContainer implements ResourceContainer{
    LinkedList<Resource> resources = new LinkedList<>();
    /**
     * checks whether it can provide the resource
     *
     * @param resource the resource to provide
     * @return true if the container can provide the resource
     */
    @Override
    public boolean providesResource(Resource resource) {
        Long found = resources.stream()
                .filter(resource1 -> resource1.getResourceID().equals(resource.getResourceID()))
                .limit(1)
                .count();
        return found > 0;
    }

    /**
     * checks whether there are any resources registered from the source
     *
     * @param sourceID the ID of the source
     * @return true if the container has resources from the source
     */
    @Override
    public boolean containsResourcesFromSource(String sourceID) {
        long found = resources.stream()
                .filter(resource -> resource.getProvider().getID().equals(sourceID))
                .limit(1)
                .count();
        return found > 0;
    }

    /**
     * checks whether the ResourceContainer can provide at least ONE resource
     *
     * @param resourcesIDs a list containing sources
     * @return true if the ResourceContainer can provide at least one resource
     */
    @Override
    public boolean providesResource(List<String> resourcesIDs) {
        long found = resources.stream()
                .filter(resource -> resourcesIDs.contains(resource.getResourceID()))
                .limit(1)
                .count();
        return found > 0;
    }

    /**
     * returns all EXISTING resources for the ID.
     * If there are no resources for the ID the ID will get skipped
     *
     * @param resourceIDs an Array containing the resources
     * @return a list of resources found
     */
    @Override
    public List<Resource> provideResource(String[] resourceIDs) {
        return resources.stream()
                .filter(resource -> Arrays.stream(resourceIDs)
                        .anyMatch(resourceID -> resourceID.equals(resource.getResourceID())))
                .collect(Collectors.toList());
    }

    /**
     * returns the FIRST resource (if existing)
     *
     * @param resourceID the ID of the resource
     * @return a list of resources found
     */
    @Override
    public Resource provideResource(String resourceID) {
        return resources.stream()
                .filter(resource -> resource.getResourceID().equals(resourceID))
                .findFirst()
                .get();
    }

    /**
     * returns the resource (if existing) from the source
     *
     * @param sourceID the ID of the source
     * @return a list containing all the found resources
     */
    public List<Resource> provideResourceFromSource(String sourceID) {
        return resources.stream()
                .filter(resource -> resource.getProvider().getID().equals(sourceID))
                .collect(Collectors.toList());
    }
}
