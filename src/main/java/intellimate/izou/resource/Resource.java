package intellimate.izou.resource;

import intellimate.izou.system.Identification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

/**
 * An Resource is an object which is used to pass data from one part of the application to another.
 */
public class Resource <T> {
    private final Lock resourceIDLock = new ReentrantLock();
    private final String resourceID;
    private final Lock providerLock = new ReentrantLock();
    private Identification provider = null;
    private final Lock consumerLock = new ReentrantLock();
    private Identification consumer = null;
    private final Lock resourceLock = new ReentrantLock();
    private T resource;
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    /**
     * creates a new Resource.
     * This method is thread-safe.
     * @param resourceID the ID of the Resource
     */
    public Resource(String resourceID) {
        this.resourceID = resourceID;
    }

    /**
     * creates a new Resource.
     * This method is thread-safe.
     * @param resourceID the ID of the Resource
     * @param provider the Provider of the Resource
     */
    public Resource(String resourceID, Identification provider) {
        this.resourceID = resourceID;
        this.provider = provider;
    }

    /**
     * creates a new Resource.
     * This method is thread-safe.
     * @param resourceID the ID of the Resource
     * @param provider the Provider of the Resource
     */
    public Resource(String resourceID, Identification provider, T t) {
        this.resourceID = resourceID;
        this.provider = provider;
        this.resource = t;
    }

    /**
     * returns the associated Resource data if set.
     * This method is thread-safe.
     * @return null or resource data
     */
    public T getResource() {
        resourceLock.lock();
        try {
            return resource;
        } finally {
            resourceLock.unlock();
        }
    }

    /**
     * sets the Resource data.
     * This method is thread-safe.
     * @param resource the data to set
     * @return the Resource
     */
    public Resource<T> setResource(T resource) {
        resourceLock.lock();
        try {
            this.resource = resource;
        } finally {
            resourceLock.unlock();
        }
        return this;
    }

    /**
     * returns the ID of the Resource.
     * This method is thread-safe.
     * @return a String containing the ID of the resource
     */
    public String getResourceID() {
        resourceIDLock.lock();
        try {
            return resourceID;
        } finally {
            resourceIDLock.unlock();
        }
    }

    /**
     * returns the provider of the Resource.
     * This method is thread-safe.
     * @return an Identification describing the provider of the Resource or null if not set
     */
    public Identification getProvider() {
        providerLock.lock();
        try {
            return provider;
        } finally {
            providerLock.unlock();
        }
    }

    /**
     * returns whether a provider is set
     * @return true if this resource has an provider, false if not
     */
    public boolean hasProvider() {
        return provider != null;
    }

    /**
     * sets who should or has provided the Resource Object.
     * This method is thread-safe.
     * @param provider an Identification describing the provider of the Resource
     * @return the Resource
     */
    public Resource<T> setProvider(Identification provider) {
        providerLock.lock();
        try {
            this.provider = provider;
        } finally {
            providerLock.unlock();
        }
        return this;
    }

    /**
     * returns the consumer of the object (if set).
     * This method is thread-safe.
     * @return null or an Identification describing the consumer of the Resource
     */
    public Identification getConsumer() {
        consumerLock.lock();
        try {
            return consumer;
        } finally {
            consumerLock.unlock();
        }
    }

    /**
     * sets who should or has consumed the Resource Object.
     * This method is thread-safe.
     * @param consumer an Identification describing the consumer of the Resource
     * @return the Resource
     */
    public Resource<T> setConsumer(Identification consumer) {
        consumerLock.lock();
        try {
            this.consumer = consumer;
        } finally {
            consumerLock.unlock();
        }
        return this;
    }

    /**
     * maps this resource to another type
     * @param function the mapping function
     * @param <R> the return type
     * @return R
     */
    public <R> R map (Function<Resource<T>, R> function) {
        return function.apply(this);
    }
}
