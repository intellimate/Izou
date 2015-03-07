package intellimate.izou.resource;

import intellimate.izou.identification.Identifiable;
import intellimate.izou.identification.Identification;

import java.util.List;
import java.util.function.Function;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
public interface Resource<T, X extends Resource<T, X>> extends Identifiable {
    /**
     * returns the associated Resource data if set.
     * This method is thread-safe.
     * @return null or resource data
     */
    T getResource();

    /**
     * returns the ID of the Resource.
     * This method is thread-safe.
     * @return a String containing the ID of the resource
     */
    String getResourceID();

    /**
     * returns the provider of the Resource.
     * This method is thread-safe.
     * @return an Identification describing the provider of the Resource or null if not set
     */
    Identification getProvider();

    /**
     * returns whether a provider is set
     * @return true if this resource has an provider, false if not
     */
    boolean hasProvider();

    /**
     * returns the consumer of the object (if set).
     * @return null or an Identification describing the consumer of the Resource
     */
    Identification getConsumer();

    /**
     * maps this resource to another type
     * @param function the mapping function
     * @param <R> the return type
     * @return R
     */
    <R> R map (Function<X, R> function);

    /**
     * creates a list with this Element in it.
     * @return a list
     */
    List<X> toList();
}
