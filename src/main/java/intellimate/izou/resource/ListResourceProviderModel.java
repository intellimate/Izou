package intellimate.izou.resource;

import java.util.List;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface ListResourceProviderModel extends ResourceProviderModel {
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
