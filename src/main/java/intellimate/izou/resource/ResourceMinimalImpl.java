package intellimate.izou.resource;

import intellimate.izou.identification.Identification;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * An Resource is an object which is used to pass data from one part of the application to another.
 * <p>
 * This is a minimal internal implementation! It should stay so to ensure that this implementations doesn't constrain
 * the implementation.
 * </p> 
 * Note! This Object is immutable!
 */
public class ResourceMinimalImpl<T> implements ResourceModel<T, ResourceMinimalImpl<T>> {
    private final String resourceID;
    private final Identification provider;
    private final Identification consumer;
    private final T resource;

    /**
     * creates a new Resource.
     * This method is thread-safe.
     * @param resourceID the ID of the Resource
     * @param provider the Provider of the Resource
     * @param t the resource
     * @param consumer the ID of the Consumer
     */
    public ResourceMinimalImpl(String resourceID, Identification provider, T t, Identification consumer) {
        this.resourceID = resourceID;
        this.provider = provider;
        this.resource = t;
        this.consumer = consumer;
    }

    /**
     * returns the associated Resource data if set.
     * This method is thread-safe.
     * @return null or resource data
     */
    @Override
    public T getResource() {
        return resource;
    }

    /**
     * sets the Resource data.
     * <p>
     * Note! this Object is immutable!
     * </p>
     * @param resource the data to set
     * @return the Resource
     */
    public ResourceMinimalImpl<T> setResource(T resource) {
        return new ResourceMinimalImpl<>(resourceID, provider, resource, consumer);
    }

    /**
     * returns the ID of the Resource.
     * This method is thread-safe.
     * @return a String containing the ID of the resource
     */
    @Override
    public String getResourceID() {
        return resourceID;
    }

    /**
     * returns the provider of the Resource.
     * This method is thread-safe.
     * @return an Identification describing the provider of the Resource or null if not set
     */
    @Override
    public Identification getProvider() {
        return provider;
    }

    /**
     * returns whether a provider is set
     * @return true if this resource has an provider, false if not
     */
    @Override
    public boolean hasProvider() {
        return provider != null;
    }

    /**
     * returns the consumer of the object (if set).
     * @return null or an Identification describing the consumer of the Resource
     */
    @Override
    public Identification getConsumer() {
        return consumer;
    }

    /**
     * maps this resource to another type
     * @param function the mapping function
     * @param <R> the return type
     * @return R
     */
    @Override
    public <R> R map(Function<ResourceMinimalImpl<T>, R> function) {
        return function.apply(this);
    }

    /**
     * creates a list with this Element in it.
     * @return a list
     */
    @Override
    public List<ResourceMinimalImpl<T>> toList() {
        return Arrays.asList(this);
    }

    /**
     * An ID must always be unique.
     * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
     * If you have to implement this interface multiple times, just concatenate unique Strings to
     * .class.getCanonicalName()
     *
     * @return A String containing an ID
     */
    @Override
    public String getID() {
        return resourceID;
    }
}
