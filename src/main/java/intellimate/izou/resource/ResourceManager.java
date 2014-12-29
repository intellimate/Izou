package intellimate.izou.resource;

import intellimate.izou.events.Event;
import intellimate.izou.main.Main;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * this class manages all the ResourceBuilders.
 */
public class ResourceManager {
    //holds the threads
    private final ExecutorService executor;
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
    private final Logger fileLogger = LogManager.getLogger(this.getClass());

    public ResourceManager(Main main) {
        executor = main.getThreadPoolManager().getAddOnsThreadPool();
    }

    /**
     * generates all the resources for an event
     * @param event the Event to generate the resources for
     * @return a List containing all the generated resources
     */
    public List<Resource> generateResources(Event event) {
        if(!event.getAllIformations().stream()
                .anyMatch(eventSubscribers::containsKey)) return new LinkedList<>();

        List<Future<List<Resource>>> futures = event.getAllIformations().stream()
                .map(eventSubscribers::get)
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .map(resource -> new ResourceBuilderCallableWrapper(resource, resource.announceResources(), Optional.of(event)))
                .map(executor::submit)
                .collect(Collectors.toList());

        futures = timeOut(futures);

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        fileLogger.debug(e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * generates the resources for a list of predefined Resources.
     * This method uses all the information stored in the Resources instances to build them.
     * @param requestResources a list containing resources instances.
     * @return a List containing all the generated resources
     */
    public List<Resource> generateResources(List<Resource> requestResources) {
        if(Objects.isNull(requestResources)) return new LinkedList<>();

        List<ResourceBuilder> resourceBuilders = requestResources.stream()
                .map(resource -> {
                    List<ResourceBuilder> tempList = resourceIDs.get(resource.getResourceID());

                    if(resource.hasProvider()) {
                        tempList = tempList.stream()
                                .filter(resourceBuilder -> resourceBuilder.isOwner(resource.getProvider()))
                                .collect(Collectors.toList());
                    }

                    return tempList;
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .distinct()
                .collect(Collectors.toList());

        return generateResource(resourceBuilders);
    }

    /**
     * generates a resources
     * @param resource the resource to request
     * @param consumer the callback when the ResourceBuilder finishes
     */
    public void generatedResource(Resource resource, Consumer<List<Resource>> consumer) {
        if(resourceIDs.get(resource.getResourceID()) == null) return;
        Optional<ResourceBuilder> resourceBuilder = resourceIDs.get(resource.getResourceID()).stream()
                //return true if resource has no provider, if not check provider
                .filter(resourceS -> !resource.hasProvider() || resourceS.isOwner(resource.getProvider()))
                .findFirst();
        if(!resourceBuilder.isPresent()) consumer.accept(new LinkedList<>());
        CompletableFuture
                .supplyAsync(() -> resourceBuilder.get().provideResource(Arrays.<Resource>asList(resource),
                        Optional.<Event>empty()), executor)
                .thenAccept(consumer);
    }

    /**
     * generates all the resources for the specified ResourceBuilders
     * @param resourceBuilders a List containing all the ResourceBuilders who should generate
     * @return a List of generated resources
     */
    private List<Resource> generateResource(List<ResourceBuilder> resourceBuilders) {
        if(resourceBuilders == null) return new LinkedList<>();
        List<Future<List<Resource>>> futures = resourceBuilders.stream()
                .map(resource -> new ResourceBuilderCallableWrapper(resource, resource.announceResources(), Optional.empty()))
                .map(executor::submit)
                .collect(Collectors.toList());

        futures = timeOut(futures);

        return futures.stream()
                .map(future -> {
                    try {
                        return future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        fileLogger.warn(e);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    /**
     * creates a 1 sec. timeout for the resource-generation
     * @param futures a List of futures running
     * @return list with all elements removed, who aren't finished after 1 sec
     */
    public List<Future<List<Resource>>> timeOut(List<Future<List<Resource>>> futures) {
        //Timeout
        int start = 0;
        boolean notFinished = true;
        while ( (start < 100) && notFinished) {
            notFinished = futures.stream()
                    .anyMatch(future -> !future.isDone());
            start++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fileLogger.warn(e);
            }
        }
        //cancel all running tasks
        if(notFinished) {
            futures.stream()
                    .filter(future -> !future.isDone())
                    .peek(future -> fileLogger.error(future.toString()+ " timed out"))
                    .forEach(future -> future.cancel(true));
        }
        return futures.stream()
                .filter(Future::isDone)
                .collect(Collectors.toList());
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
                resourceIDs.get(resource.getResourceID()).add(resourceBuilder);
            } else {
                LinkedList<ResourceBuilder> tempList = new LinkedList<>();
                tempList.add(resourceBuilder);
                resourceIDs.put(resource.getResourceID(), tempList);
            }
        }
    }

    /**
     * registers the events for the ResourceBuilder
     * @param resourceBuilder an instance of ResourceBuilder
     */
    private void registerEventsForResourceBuilder(ResourceBuilder resourceBuilder) {
        List<String> events = resourceBuilder.announceEvents();
        if(events == null) return;
        for(String event : events) {
            if(eventSubscribers.containsKey(event)) {
                eventSubscribers.get(event).add(resourceBuilder);
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
    }

    /**
     * unregisters all ResourceIDs for the ResourceBuilders
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
        resourceBuilder.announceEvents().stream()
                .map(eventSubscribers::get)
                .filter(Objects::nonNull)
                .forEach(list -> list.remove(resourceBuilder));
    }

    private class ResourceBuilderCallableWrapper implements Callable<List<Resource>> {
        private ResourceBuilder resourceBuilder;
        private List<Resource> resources;
        private Optional<Event> event;

        public ResourceBuilderCallableWrapper(ResourceBuilder resourceBuilder, List<Resource> resources,
                                              Optional<Event> event) {
            this.resourceBuilder = resourceBuilder;
            this.resources = resources;
            this.event = event;
        }

        /**
         * Computes a result, or throws an exception if unable to do so.
         * @return computed result
         * @throws Exception if unable to compute a result
         */
        @Override
        public List<Resource> call() throws Exception {
            return resourceBuilder.provideResource(resources, event);
        }
    }
}
