package org.intellimate.izou.resource;

import org.intellimate.izou.util.AddonThreadPoolUser;
import org.intellimate.izou.util.IzouModule;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.identification.IllegalIDException;
import org.intellimate.izou.main.Main;

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
    private HashMap<String, LinkedList<ResourceBuilderModel>> eventSubscribers = new HashMap<>();
    /**
     * this object maps all the resourceID to ResourceBuilders
     * the key is the registered event (or noEvent)
     * the List contains all the ResourceBuilders registered
     */
    private HashMap<String, LinkedList<ResourceBuilderModel>> resourceIDs= new HashMap<>();

    public ResourceManager(Main main) {
        super(main);
    }

    /**
     * generates all the resources for an event
     * @param event the Event to generate the resources for
     * @return a List containing all the generated resources
     */
    public List<ResourceModel> generateResources(EventModel<?> event) {
        if(!event.getAllInformations().stream()
                .anyMatch(eventSubscribers::containsKey)) return new LinkedList<>();

        List<ResourceBuilderModel> resourceBuilders = event.getAllInformations().stream()
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
    private List<ResourceModel> generateResources(List<ResourceBuilderModel> resourceBuilders, EventModel event) {
        Optional<EventModel> parameter = event != null ? Optional.of(event) : Optional.empty();
        List<CompletableFuture<List<ResourceModel>>> futures = resourceBuilders.stream()
                .map(resourceB -> submit(() -> resourceB.provideResource(resourceB.announceResources(), parameter)))
                .collect(Collectors.toList());

        try {
            futures = timeOut(futures, 3000);
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
     * @throws IllegalIDException not yet implemented
     */
    @Deprecated
    public void generatedResource(ResourceModel resource, Consumer<List<ResourceModel>> consumer) throws IllegalIDException {
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
     * @throws IllegalIDException not yet implemented
     */
    public Optional<CompletableFuture<List<ResourceModel>>> generateResource (ResourceModel resource) throws IllegalIDException {
        if(resourceIDs.get(resource.getResourceID()) == null) return Optional.empty();
        return resourceIDs.get(resource.getResourceID()).stream()
                //return true if resource has no provider, if not check provider
                .filter(resourceS -> !resource.hasProvider() || resourceS.isOwner(resource.getProvider()))
                .findFirst()
                .map(resourceB -> submit(() -> resourceB.provideResource(Collections.singletonList(resource), Optional.empty())));
    }

    /**
     * registers a ResourceBuilder.
     * <p>
     * this method registers all the events, resourcesID etc.
     * @param resourceBuilder an instance of the ResourceBuilder
     * @throws IllegalIDException not yet implemented
     */
    public void registerResourceBuilder(ResourceBuilderModel resourceBuilder) throws IllegalIDException {
        registerResourceIDsForResourceBuilder(resourceBuilder);
        registerEventsForResourceBuilder(resourceBuilder);
    }

    /**
     * registers all ResourceIDs for the ResourceBuilders
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void registerResourceIDsForResourceBuilder(ResourceBuilderModel resourceBuilder) {
        List<? extends ResourceModel> resources = resourceBuilder.announceResources();
        if(resources == null) return;
        resources.stream()
                .map(this::getRegisteredListForResource)
                .forEach(list -> list.add(resourceBuilder));
    }

    /**
     * returns the list with all the ResourceBuilders listening to the Resource
     * @param resource the resource to listen to
     * @return a List of ResourceBuilders
     */
    private List<ResourceBuilderModel> getRegisteredListForResource(ResourceModel resource) {
        if(resourceIDs.containsKey(resource.getResourceID())) {
            return resourceIDs.get(resource.getResourceID());
        } else {
            LinkedList<ResourceBuilderModel> tempList = new LinkedList<>();
            resourceIDs.put(resource.getResourceID(), tempList);
            return tempList;
        }
    }

    /**
     * Registers the events for the ResourceBuilder
     *
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void registerEventsForResourceBuilder(ResourceBuilderModel resourceBuilder) {
        List<? extends EventModel<?>> events = resourceBuilder.announceEvents();
        if(events == null) return;
        events.stream()
                .filter(event -> event.getAllInformations() != null)
                .flatMap(event -> event.getAllInformations().stream())
                .map(this::getRegisteredListForEvent)
                .forEach(list -> list.add(resourceBuilder));
    }

    /**
     * returns a list of all the ResourceBuilders listening to the Event-ID 
     * @param event the eventID
     * @return a List of ResourceBuilders
     */
    private List<ResourceBuilderModel> getRegisteredListForEvent(String event) {
        if(eventSubscribers.containsKey(event)) {
            return eventSubscribers.get(event);
        } else {
            LinkedList<ResourceBuilderModel> tempList = new LinkedList<>();
            eventSubscribers.put(event, tempList);
            return tempList;
        }
    }

    /**
     * unregister a ResourceBuilder.
     * <p>
     * this method unregisters all the events, resourcesID etc.
     * @param resourceBuilder an instance of the ResourceBuilder
     */
    public void unregisterResourceBuilder(ResourceBuilderModel resourceBuilder) {
        unregisterResourceIDForResourceBuilder(resourceBuilder);
        unregisterEventsForResourceBuilder(resourceBuilder);
    }

    /**
     * Unregisters all ResourceIDs for the ResourceBuilders
     *
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void unregisterResourceIDForResourceBuilder(ResourceBuilderModel resourceBuilder) {
        List<? extends ResourceModel> resources = resourceBuilder.announceResources();
        resources.stream().map(resource -> resourceIDs.get(resource.getResourceID()))
                .filter(Objects::nonNull)
                .forEach(list -> list.remove(resourceBuilder));
    }

    /**
     * unregisters the events for the ResourceBuilder
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void unregisterEventsForResourceBuilder(ResourceBuilderModel resourceBuilder) {
        resourceBuilder.announceEvents().stream()
                .map(eventSubscribers::get)
                .filter(Objects::nonNull)
                .forEach(list -> list.remove(resourceBuilder));
    }
}
