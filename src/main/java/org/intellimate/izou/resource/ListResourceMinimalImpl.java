package org.intellimate.izou.resource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * a minimal implementation of the ListResourceProviderInterface. Do not use this outside izou!
 * @author LeanderK
 * @version 1.0
 */
public class ListResourceMinimalImpl implements ListResourceProvider {
    final List<ResourceModel> resources = new ArrayList<>();

    /**
     * adds a Resource to the Container
     * @param resource an instance of the resource to add
     */
    @Override
    public void addResource(ResourceModel resource) {
        resources.add(resource);
    }

    /**
     * adds a List of Resources to the Container
     * @param resources a List of resources to add
     */
    @Override
    public void addResource(List<ResourceModel> resources) {
        this.resources.addAll(resources);
    }

    /**
     * checks whether it can provide the resource
     *
     * @param resource the resource to provide
     * @return true if the container can provide the resource
     */
    @Override
    public boolean providesResource(ResourceModel resource) {
        return resources.stream()
                .map(ResourceModel::getResourceID)
                .anyMatch(resourceS -> resourceS.equals(resource.getResourceID()));
    }

    /**
     * checks whether there are any resources registered from the source
     *
     * @param sourceID the ID of the source
     * @return true if the container has resources from the source
     */
    @Override
    public boolean containsResourcesFromSource(String sourceID) {
        return resources.stream()
                .map(ResourceModel::getResourceID)
                .anyMatch(source -> source.equals(sourceID));
    }

    /**
     * checks whether the ResourceContainer can provide at least ONE resource
     *
     * @param resourcesIDs a list containing sources
     * @return true if the ResourceContainer can provide at least one resource
     */
    @Override
    public boolean providesResource(List<String> resourcesIDs) {
        return resources.stream()
                .map(ResourceModel::getResourceID)
                .anyMatch(resourcesIDs::contains);
    }

    /**
     * returns all EXISTING resources for the ID.
     * If there are no resources for the ID the ID will get skipped
     *
     * @param resourceIDs an Array containing the resources
     * @return a list of resources found
     */
    @Override
    public List<ResourceModel> provideResource(String[] resourceIDs) {
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
    public List<ResourceModel> provideResource(String resourceID) {
        return resources.stream()
                .filter(resource -> resource.getResourceID().equals(resourceID))
                .collect(Collectors.toList());
    }

    /**
     * returns the resource (if existing) from the source
     *
     * @param sourceID the ID of the source
     * @return a list containing all the found resources
     */
    @Override
    public List<ResourceModel> provideResourceFromSource(String sourceID) {
        return resources.stream()
                .filter(resource -> resource.getProvider().getID().equals(sourceID))
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "ListResourceProviderImpl{" +
                "resources=" + resources +
                '}';
    }
}
