package org.intellimate.izou.resource;

import ro.fortsoft.pf4j.AddonAccessible;

import java.util.List;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface ListResourceProvider extends ResourceProvider {
    /**
     * adds a Resource to the Container
     * @param resource an instance of the resource to add
     */
    void addResource(ResourceModel resource);

    /**
     * adds a List of Resources to the Container
     * @param resources a List of resources to add
     */
    void addResource(List<ResourceModel> resources);
}
