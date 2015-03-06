package intellimate.izou.main;

import intellimate.izou.IzouModule;
import intellimate.izou.addon.AddOn;
import intellimate.izou.events.Event;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

/**
 * Use this class if you want to hook to a Lifecycle-Phase 
 * @author Leander Kurscheidt
 * @version 1.0
 */
public class LifeCycle extends IzouModule {
    private List<Consumer<AddOn>> addOnsInitializingHook = new ArrayList<>();
    private List<Consumer<AddOn>> addOnsAddedHook = new ArrayList<>();
    private List<Consumer<Event>> generatingEventHook = new ArrayList<>();
    private List<Consumer<Event>> processEventHook = new ArrayList<>();
    
    public LifeCycle(Main main) {
        super(main);
    }
    
    public void addAddOnsInitializingHook(Consumer<AddOn> addOnConsumer) {
        addOnsInitializingHook.add(addOnConsumer);
    }
    
    public void addAddOnsAddedHook(Consumer<AddOn> addOnConsumer) {
        addOnsAddedHook.add(addOnConsumer);
    }

    public void addGeneratingEventHook(Consumer<Event> eventConsumer) {
        generatingEventHook.add(eventConsumer);
    }
    
    public void addProcessEventHook(Consumer<Event> eventConsumer) {
        processEventHook.add(eventConsumer);
    }
    
    public void removeAddOnsInitializingHook(Consumer<AddOn> addOnConsumer) {
        addOnsInitializingHook.remove(addOnConsumer);
    }
    
    public void removeAddOnsAddedHook(Consumer<AddOn> addOnConsumer) {
        addOnsAddedHook.remove(addOnConsumer);
    }
    
    public void removeGeneratingEventHook(Consumer<Event> eventConsumer) {
        generatingEventHook.remove(eventConsumer);
    }
    
    public void removeProcessEventHook(Consumer<Event> eventConsumer) {
        processEventHook.remove(eventConsumer);
    }

    public void startAddOnsInitializingHook(List<AddOn> addOns) {
        addOns.forEach(addOn -> runBlockingOnEach(addOn, addOnsInitializingHook));
    }
    
    public void startAddOnsAddedHook(List<AddOn> addOns) {
        addOns.forEach(addOn -> runBlockingOnEach(addOn, addOnsAddedHook));
    }
    
    public void startGeneratingEventHook(Event event) {
        runBlockingOnEach(event, generatingEventHook);
    }
    
    public void startProcessingHook(Event event) {
        runBlockingOnEach(event, processEventHook);
    }
    
    private <V> void runBlockingOnEach(V v, List<Consumer<V>> consumers) {
        consumers.stream()
                .map(listener -> CompletableFuture.runAsync(() -> listener.accept(v),
                        main.getThreadPoolManager().getIzouThreadPool()))
                .forEach(future -> {
                    try {
                        future.get();
                    } catch (InterruptedException | ExecutionException e) {
                        log.error("Error while processing " + v.toString());
                    }
                });
    }
}
