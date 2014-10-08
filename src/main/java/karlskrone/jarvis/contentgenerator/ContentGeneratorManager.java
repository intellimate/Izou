package karlskrone.jarvis.contentgenerator;

import karlskrone.jarvis.events.EventManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * The ContentGeneratorManager holds all the ContentGenerator-instances and runs them parallel in Threads.
 */
public class ContentGeneratorManager {
    //holds the threads
    private final ExecutorService executor = Executors.newCachedThreadPool();
     //holds the contentGenerators
    private final List<ContentGenerator> contentGeneratorList = new ArrayList<>();
    private final EventManager eventManager;

    public ContentGeneratorManager(EventManager eventManager) {
        this.eventManager = eventManager;
    }

    /**
     * Adds an ContentGenerator.
     *
     * Adds an ContentGenerator, but does not run them.
     *
     * @param contentGenerator an instance of the ContentGenerator
     */
    public void addContentGenerator (ContentGenerator contentGenerator) {
        contentGenerator.registerAllNeededDependencies(this, eventManager);
        contentGeneratorList.add(contentGenerator);
    }

    /**
     * returns all the ContentGenerators registered in ContentGeneratorManager
     *
     * @return a List containing all the ContentGenerators
     */
    public List<ContentGenerator> getContentGeneratorList() {
        return contentGeneratorList;
    }

    /**
     * Removes an ContentGenerator.
     *
     * Removes an ContentGenerator from its internal List.
     *
     * @param contentGenerator an instance of the ContentGenerator
     */
    public void removeContentGenerators (ContentGenerator contentGenerator) {
        contentGeneratorList.remove(contentGenerator);
    }

    /**
     * Removes an ContentGenerator and unregisters all his listeners
     *
     * @param contentGenerator an instance of the ContentGenerator
     */
    public void deleteContentGenerator (ContentGenerator contentGenerator) {
        contentGenerator.unregisterAllEvents();
        contentGeneratorList.remove(contentGenerator);
    }

    /**
     * Runs the ContentGenerator in a ThreadPool.
     *
     * @param contentGenerator the ContentGenerator to run.
     * @return a Future representing pending completion of the task.
     */
    @SuppressWarnings("unchecked")
    public Future<ContentData> runContentGenerator (ContentGenerator contentGenerator) {
        return executor.submit(contentGenerator);
    }
}
