package intellimate.izou.system.context;

import intellimate.izou.resource.Resource;
import intellimate.izou.resource.ResourceBuilder;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface Resources {
    /**
     * registers a ResourceBuilder.
     * <p>
     *  this method registers all the events, resourcesID etc.
     * </p>
     * @param resourceBuilder an instance of the ResourceBuilder
     */
    void registerResourceBuilder(ResourceBuilder resourceBuilder);

    /**
     * unregister a ResourceBuilder.
     * <p>
     * this method unregisters all the events, resourcesID etc.
     * @param resourceBuilder an instance of the ResourceBuilder
     */
    void unregisterResourceBuilder(ResourceBuilder resourceBuilder);

    /**
     * generates a resources
     * <p>
     * @param resource the resource to request
     * @param consumer the callback when the ResourceBuilder finishes
     */
    @Deprecated
    void generateResource(Resource resource, Consumer<List<Resource>> consumer);

    /**
     * generates a resources
     * <p>
     * It will use the first matching resource! So if you really want to be sure, set the provider
     * Identification
     * </p>
     * @param resource the resource to request
     * @return an optional of an CompletableFuture
     */
    Optional<CompletableFuture<List<Resource>>> generateResource(Resource resource);
}
