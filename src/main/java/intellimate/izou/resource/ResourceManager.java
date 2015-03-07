package intellimate.izou.resource;

import intellimate.izou.AddonThreadPoolUser;
import intellimate.izou.IzouModule;
import intellimate.izou.events.Event;
import intellimate.izou.main.Main;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * this class manages all the ResourceBuilders.
 */
public class ResourceManager extends IzouModule implements AddonThreadPoolUser {
    /**
     * this object maps all the eventIDs to ResourceBuilders
     * the key is the registered event (or noEvent)
     */
    private HashMap<String, LinkedList<ResourceBuilder>> eventSubscribers = new HashMap<>();
    /**
     * this object maps all the resourceID to ResourceBuilders
     * the key is the registered event (or noEvent)
     * the List contains all the ResourceBuilders registered
     */
    private HashMap<String, LinkedList<ResourceBuilder>> resourceIDs= new HashMap<>();

    public ResourceManager(Main main) {
        super(main);
    }

    /**
     * generates all the resources for an event
     * @param event the Event to generate the resources for
     * @return a List containing all the generated resources
     */
    public List<Resource> generateResources(Event event) {
        if(!event.getAllIformations().stream()
                .anyMatch(eventSubscribers::containsKey)) return new LinkedList<>();

        List<ResourceBuilder> resourceBuilders = event.getAllIformations().stream()
                .map(eventSubscribers::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());
        
        return generateResources(resourceBuilders, event);
    }

    /**
     * generates the resources with a 1 sec. timeout for each ResourceBuilder 
     * @param resourceBuilders the ResourceBuilders
     * @param event the event or null if not present
     * @return a List of generated resources
     */
    private List<Resource> generateResources(List<ResourceBuilder> resourceBuilders, Event event) {
        Optional<Event> parameter = event != null ? Optional.of(event) : Optional.empty();
        List<CompletableFuture<List<Resource>>> futures = resourceBuilders.stream()
                .map(resourceB -> submit(() -> resourceB.provideResource(resourceB.announceResources(), parameter)))
                .collect(Collectors.toList());

        try {
            futures = timeOut(futures, 1000);
        } catch (InterruptedException e) {
            debug("interrupted while doing an time-out", e);
        }

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        debug("exception while trying to get the result from the future", e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * generates a resources
     * <p>
     * It will use the first matching resource! So if you really want to be sure, set the provider
     * Identification
     * </p>
     * @param resource the resource to request
     * @param consumer the callback when the ResourceBuilder finishes
     */
    @Deprecated
    public void generatedResource(Resource resource, Consumer<List<Resource>> consumer) {
        generateResource(resource)
                .ifPresent(completableFuture -> completableFuture.thenAccept(consumer));
    }

    /**
     * generates a resources
     * <p>
     * It will use the first matching resource! So if you really want to be sure, set the provider
     * Identification
     * </p>
     * @param resource the resource to request
     * @return an optional of an CompletableFuture
     */
    public Optional<CompletableFuture<List<Resource>>> generateResource (Resource resource) {
        if(resourceIDs.get(resource.getResourceID()) == null) return Optional.empty();
        return resourceIDs.get(resource.getResourceID()).stream()
                //return true if resource has no provider, if not check provider
                .filter(resourceS -> !resource.hasProvider() || resourceS.isOwner(resource.getProvider()))
                .findFirst()
                .map(resourceB -> submit(() -> resourceB.provideResource(Arrays.asList(resource), Optional.empty())));
    }

    /**
     * registers a ResourceBuilder.
     * <p>
     * this method registers all the events, resourcesID etc.
     * @param resourceBuilder an instance of the ResourceBuilder
     */
    public void registerResourceBuilder(ResourceBuilder resourceBuilder) {
        registerResourceIDsForResourceBuilder(resourceBuilder);
        registerEventsForResourceBuilder(resourceBuilder);
    }

    /**
     * registers all ResourceIDs for the ResourceBuilders
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void registerResourceIDsForResourceBuilder(ResourceBuilder resourceBuilder) {
        List<Resource> resources = resourceBuilder.announceResources();
        if(resources == null) return;
        for(Resource resource : resources) {
            if(resourceIDs.containsKey(resource.getResourceID())) {
                LinkedList<ResourceBuilder> resourceBuilders = resourceIDs.get(resource.getResourceID());
                if (!resourceBuilders.contains(resourceBuilder))
                    resourceBuilders.add(resourceBuilder);
            } else {
                LinkedList<ResourceBuilder> tempList = new LinkedList<>();
                tempList.add(resourceBuilder);
                resourceIDs.put(resource.getResourceID(), tempList);
            }
        }
    }

    /**
     * Registers the events for the ResourceBuilder
     *
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void registerEventsForResourceBuilder(ResourceBuilder resourceBuilder) {
        List<String> events = resourceBuilder.announceEvents();
        if(events == null) return;
        for (String event : events) {
            //TODO: @Julian here!
            if(eventSubscribers.containsKey(event)) {
                LinkedList<ResourceBuilder> resourceBuilders = eventSubscribers.get(event);
                if (!resourceBuilders.contains(resourceBuilder))
                    resourceBuilders.add(resourceBuilder);
            } else {
                LinkedList<ResourceBuilder> tempList = new LinkedList<>();
                tempList.add(resourceBuilder);
                eventSubscribers.put(event, tempList);
            }
        }
    }

    /**
     * unregister a ResourceBuilder.
     * <p>
     * this method unregisters all the events, resourcesID etc.
     * @param resourceBuilder an instance of the ResourceBuilder
     */
    public void unregisterResourceBuilder(ResourceBuilder resourceBuilder) {
        unregisterResourceIDForResourceBuilder(resourceBuilder);
        unregisterEventsForResourceBuilder(resourceBuilder);
    }

    /**
     * Unregisters all ResourceIDs for the ResourceBuilders
     *
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void unregisterResourceIDForResourceBuilder(ResourceBuilder resourceBuilder) {
        List<Resource> resources = resourceBuilder.announceResources();
        resources.stream().map(resource -> resourceIDs.get(resource.getResourceID()))
                .filter(Objects::nonNull)
                .forEach(list -> list.remove(resourceBuilder));
    }

    /**
     * unregisters the events for the ResourceBuilder
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void unregisterEventsForResourceBuilder(ResourceBuilder resourceBuilder) {
        //TODO @Julian intercept here for removal!
        resourceBuilder.announceEvents().stream()
                .map(eventSubscribers::get)
                .filter(Objects::nonNull)
                .forEach(list -> list.remove(resourceBuilder));
    }
}
