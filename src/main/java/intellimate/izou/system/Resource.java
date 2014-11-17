package intellimate.izou.system;

/**
 * An Resource is an object which is used to pass data from one part of the application to another.
 */
public class Resource <T> {
    private Identifiable resourceProvider;
    private Identifiable resourceConsumer;
    private final String resourceID;
    private T resource;

    /**
     * creates a new Resource.
     * @param resourceID
     */
    public Resource(String resourceID) {
        this.resourceID = resourceID;
    }


    public Identifiable getResourceProvider() {
        return resourceProvider;
    }

    public void setResourceProvider(Identifiable resourceProvider) {
        this.resourceProvider = resourceProvider;
    }

    public void setResourceProvider(String id) {
        this.resourceProvider = () -> id;
    }

    public Identifiable getResourceConsumer() {
        return resourceConsumer;
    }

    public void setResourceConsumer(Identifiable resourceConsumer) {
        this.resourceConsumer = resourceConsumer;
    }

    public void setResourceConsumer(String id) {
        this.resourceConsumer = () -> id;
    }

    public T getResource() {
        return resource;
    }

    public void setResource(T resource) {
        this.resource = resource;
    }

    public String getResourceID() {
        return resourceID;
    }
}
