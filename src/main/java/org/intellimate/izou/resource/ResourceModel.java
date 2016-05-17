package org.intellimate.izou.resource;

import org.intellimate.izou.identification.Identifiable;
import org.intellimate.izou.identification.Identification;
import ro.fortsoft.pf4j.AddonAccessible;

/**
 * @author Leander Kurscheidt
 * @version 1.0
 */
@AddonAccessible
public interface ResourceModel<T> extends Identifiable {
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
}
