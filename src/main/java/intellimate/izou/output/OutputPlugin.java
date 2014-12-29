package intellimate.izou.output;

import intellimate.izou.events.Event;
import intellimate.izou.resource.Resource;
import intellimate.izou.system.Identifiable;
import intellimate.izou.system.IdentificationManager;
import intellimate.izou.system.Context;
import intellimate.izou.threadpool.ExceptionCallback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * The OutputPlugin class gets Event and then starts threads filled with output-extension tasks to create the final
 * output and then render it on its own medium
 */
@SuppressWarnings("UnusedDeclaration")
public abstract class OutputPlugin<T> implements Runnable, Identifiable, ExceptionCallback {
    /**
     * id of the of OutputPlugin, it is primarily used by OutputManager to communicate with specific output plugins
     */
    private final String id;

    /**
     * a List containing all the output-extensions the plugin requires to successfully create its final output
     */
    private List<OutputExtension<T>> outputExtensionList;

    /**
     * responsible for running output-extensions in different threads
     */
    private ExecutorService executor;

    /**
     * list that contains the future objects each output-extension returns
     */
    private LinkedList<Future<T>> futureList;

    /**
     * list of future objects that now finished processing and were added to this list
     */
    private List<T> tDoneList;

    /**
     * BlockingQueue that stores event-requests that are to be executed
     */
    private BlockingQueue<Event> eventBlockingQueue;

    /**
     * the context of the addOn
     */
    private Context context;

    /**
     * creates a new output-plugin with a new id
     *
     * @param id the id of the new output-plugin
     * @param context context and sh*t, you know
     */
    public OutputPlugin(String id, Context context) {
        this.id = id;
        this.context = context;
        outputExtensionList = new ArrayList<>();
        futureList = new LinkedList<>();
        tDoneList = new ArrayList<>();
        executor = context.threadPool.getThreadPool();
        eventBlockingQueue = new LinkedBlockingQueue<>();
        IdentificationManager identificationManager = IdentificationManager.getInstance();
        identificationManager.registerIdentification(this);

    }

    /**
     * gets list of finished future objects
     * @return list of finished future objects
     */
    public List<T> getTDoneList() {
        return tDoneList;
    }

    /**
     * get the outputExtensionList
     *
     * @return gets the list of output-extensions in the output-plugin
     */
    public List<OutputExtension<T>> getOutputExtensionList() {
        return outputExtensionList;
    }

    /**
     * gets the futureList of the output-Plugin, which contains the processed data of the output-extensions
     * @return the futureList to be return
     */
    public LinkedList<Future<T>> getFutureList() {
        return futureList;
    }

    /**
     * adds an event to blockingQueue
     *
     * @param event the event to add
     * @throws IllegalStateException raised if problems adding an event to blockingQueue
     */
    public void addToEventList(Event event) throws IllegalStateException{
        eventBlockingQueue.add(event);
    }

    /**
     * sets the executor of the OutputManager for efficiency reasons
     * @param executor the executor to be set
     */
    public void setExecutor(ExecutorService executor) {
        this.executor = executor;
    }

    /**
     * Gets the id of the outputPlugin
     *
     * @return id of the outputPlugin
     */
    @Override
    public String getID() {
        return this.id;

    }

    /**
     * gets the blocking-queue that stores the backlog of Events
     *
     * @return blocking-queue that stores Events
     */
    public BlockingQueue<Event> getEventBlockingQueue() {
        return eventBlockingQueue;
    }

    /**
     * checks if the outputPlugin has any output-extensions it can run with the current Event
     *
     * @return state of whether the outputPlugin has any output-extensions it can run with the current Event
     */
    public boolean canRun() {
        boolean canRun = false;
        for(OutputExtension oE: outputExtensionList) {
            if(oE.canRun())
                canRun = true;
        }
        return canRun;
    }

    /**
     * distributes the Event in the eventDataList to the output-extensions that will need them.
     *
     * all OutputExtensions have a wishList, where Ids of resources are stored. This method compares the wishList with
     * resources in provided from the event.
     *
     * @param event the Event to distribute
     */
    public void distributeEvent(Event event) {
        outputExtensionList.parallelStream()
                .filter(ext -> event.getListResourceContainer().providesResource(ext.getResourceIdWishList()))
                .forEach(ext -> ext.setEvent(event));
    }

    /**
     * add outputExtension to outputExtensionList
     *
     * @param outputExtension the output-extension to be added to outputExtensionList
     */
    public void addOutputExtension(OutputExtension<T> outputExtension) {
        outputExtensionList.add(outputExtension);
        outputExtension.setPluginId(this.getID());
        outputExtensionWasAdded(outputExtension);
    }

    /**
     * removes output-extensions from outputExtensionList
     *
     * removes output-extensions from outputExtensionList, outputExtensionList is an ArrayList that stores all
     * outputExtensions of all OutputPlugins
     *
     * @param id the id of the output extension to be removed
     */
    public void removeOutputExtension(String id) {
        for(OutputExtension ext: outputExtensionList) {
            if(ext.getID().equals(id)) {
                ext.setPluginId(null);
                outputExtensionList.remove(ext);
                break;
            }
        }
    }

    /**
     * returns the Context of the AddOn.
     *
     * Context provides some general Communications.
     *
     * @return an instance of Context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * event is called when an output-extension is added to this output-plugin
     *
     * @param outputExtension the outputExtension that was added to the outputPlugin
     */
    public void outputExtensionWasAdded(OutputExtension<T> outputExtension) {

    }

    /**
     * method that can be overwritten in order to "do stuff" before an outputExtension is started
     *
     * @param outputExtension the outputExtension which is about to be started
     */
    public void prepareDistribution(OutputExtension<T> outputExtension) {}

    /**
     * method that uses tDoneList to generate a final output that will then be rendered.
     * The processed data is found in tDoneList
     */
    public abstract void renderFinalOutput();

    /**
     * The event raised when an outputData has finished processing and is about to be added to the tDoneList.
     * It is NOT raised immediately raised after it is done, instead it is raised exactly when it is about to be
     * added to the tDoneList, by which time all other outputExtensions will also have finished. However it is an
     * opportunity to work with a finished future object (or get the outputData inside) before it is thrown back
     * into the heap.
     *
     * @param tFuture future object that contains the processed outputExtension data
     */
    public void outputDataIsDone(Future<T> tFuture) {}

    /**
     * Default implementation waits until a new Event has been received and then processes it.
     *
     * This method is made to be overwritten as seen fit by the developer
     *
     * @throws java.lang.InterruptedException if interrupted while waiting
     * @return the recently added Event-instance to be processed by the outputPlugin
     */
    public Event blockingQueueHandling() throws InterruptedException {
        return eventBlockingQueue.take();
    }

    /**
     *@param event the current processed Event
     */
    public void isDone(Event event) {
        Optional<Resource> resource = event.getListResourceContainer().provideResource(getID()).stream()
                .filter(resourceS -> resourceS.getProvider().getID().equals(OutputManager.ID))
                .findFirst();
        if(!resource.isPresent()) return;
        if(resource.get().getResource() instanceof Consumer) {
            Consumer consumer = (Consumer) resource.get().getResource();
            consumer.accept(null);
        }
    }

    /**
     * this method gets called when the task submitted to the ThreadPool crashes
     *
     * @param e the exception catched
     */
    @Override
    public void exceptionThrown(Exception e) {
        context.logger.getLogger().fatal("OutputPlugin " + getID() + " crashed", e);
    }

    /**
     * main method for outputPlugin, runs the data-conversion and output-renderer
     *
     * when the outputExtensions are done processing the Event object, they add their finished objects into tDoneList,
     * from where they will have to be gotten to render them in renderFinalOutput
     */
    @Override
    public void run() {
        while (true) {
            Event event;
            try {
                event = blockingQueueHandling();  //gets the new Event if one was added to the blockingQueue
            } catch (InterruptedException e) {
                context.logger.getLogger().warn(e);
                continue;
            }

            distributeEvent(event); //distributes the Event among all outputExtensions
            if(canRun()) {  //checks if there are any outputExtensions that can run at all
                for (OutputExtension<T> ext : outputExtensionList) {
                    prepareDistribution(ext);
                    if (ext.canRun()) //if the specific outputExtension can run, then it does
                        futureList.add(executor.submit(ext));
                }

                //waits until all the outputExtensions have finished processing
                boolean isWorking;
                do {
                    isWorking = false;
                    for (Future<T> cDF : futureList) {
                        if(!cDF.isDone())
                            isWorking = true;
                    }
                } while (isWorking);

                //copies the finished future objects into a tDoneList
                for (Future<T> tF : futureList) {
                    try {
                        outputDataIsDone(tF);
                        tDoneList.add(tF.get());
                    } catch (InterruptedException | ExecutionException e) {
                        context.logger.getLogger().warn(e);
                    }
                }

                //render final output
                renderFinalOutput();
            }

            //notifies output-manager when done processing
            isDone(event);
        }
    }
}
