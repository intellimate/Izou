package intellimate.izou.output;

import intellimate.izou.contentgenerator.ContentData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.*;

/**
 * The OutputPlugin class gets contentData and then starts threads filled with output-extension tasks to create the final
 * output and then render it on its own medium
 *
 * Created by julianbrendl on 9/27/14.
 */
public abstract class OutputPlugin<T> implements Runnable{
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
    private BlockingQueue<List<ContentData>> contentDataBlockingQueue;


    /**
     * creates a new output-plugin with a new id
     *
     * @param id the id of the new output-plugin
     */
    public OutputPlugin(String id) {
        this.id = id;
        outputExtensionList = new ArrayList<>();
        futureList = new LinkedList<>();
        tDoneList = new ArrayList<>();
        executor = null;
        contentDataBlockingQueue = new LinkedBlockingQueue<>();
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
     * @return the futureList to be returnt
     */
    public LinkedList<Future<T>> getFutureList() {
        return futureList;
    }

    /**
     * adds a content-data list to blockingQueue
     *
     * @param contentDataList the content-data list to be added to blockingQueue
     * @throws IllegalStateException raised if problems adding a content-data list to blockingQueue
     */
    public void addContentDataList(List<ContentData> contentDataList) throws IllegalStateException {
        contentDataBlockingQueue.add(contentDataList);
    }

    /**
     * sets the executor of the eventManager for efficiency reasons
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
    public String getId() {
        return this.id;

    }

    /**
     * gets the blocking-queue that stores contentDataLists sent by events
     *
     * @return blocking-queue that stores contentDataLists sent by events
     */
    public BlockingQueue<List<ContentData>> getContentDataBlockingQueue() {
        return contentDataBlockingQueue;
    }

    /**
     * checks if the outputPlugin has any output-extensions it can run with the current content-datas
     *
     * @return state of whether the outputPlugin has any output-extensions it can run with the current content-datas
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
     * distributes the content-Data elements in the contentDataList to the output-extensions that will need them
     *
     * it uses the id of the contentData which is the same as the id of the outputExtension to identify which output-extension
     * it should send the content-data to
     *
     * @param contentDataList the Data to distribute
     */
    public void distributeContentData(List<ContentData> contentDataList) {
        for(OutputExtension ext: outputExtensionList) {
            List<String> contentDataWishList = ext.getContentDataWishList();
            for(String strWish: contentDataWishList) {
                for(ContentData cD: contentDataList) {
                    if (strWish.equals(cD.getId())) {
                        ext.addContentData(cD);
                    }
                }
            }
        }
    }

    /**
     * add outputExtension to outputExtensionList
     *
     * @param outputExtension the output-extension to be added to outputExtensionList
     */
    public void addOutputExtension(OutputExtension<T> outputExtension) {
        outputExtensionList.add(outputExtension);
        outputExtension.setPluginId(this.getId());
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
            if(ext.getId().equals(id)) {
                ext.setPluginId(null);
                outputExtensionList.remove(ext);
                break;
            }
        }
    }

    /**
     * method that can be overwritten in order to "do stuff" before the outputExtensions are started
     */
    public void prepareDistribution() {

    }

    /**
     * method that uses tDoneList to generate a final output that will then be rendered.
     * The processed content-data objects are found in tDoneProcessed
     */
    public abstract void renderFinalOutput();

    /**
     * Default implementation waits until a new list of content-datas has been received and then processes it.
     *
     * This method is made to be overwritten as seen fit by the developer
     *
     * @throws java.lang.InterruptedException if interrupted while waiting
     * @return the list of content-datas to be processed by the outputPlugin
     */
    public List<ContentData> blockingQueueHandling() throws InterruptedException {
        List<ContentData> contentDataList = contentDataBlockingQueue.take();
        return contentDataList;
    }

    /**
     * main method for outputPlugin, runs the data-conversion and output-renderer
     *
     * when the outputExtensions are done processing the ContentData objects, they add their finished objects into tDoneList,
     * from where they will have to be gotten to render them in renderFinalOutput
     */
    @Override
    public void run() {
        while (true) {
            List<ContentData> contentDataList;
            try {
                contentDataList = blockingQueueHandling();  //gets the new contentDataList if one was added to the blockingQueue
            } catch (InterruptedException e) {
                e.printStackTrace();
                //TODO: implement exception handling
                break;
            }

            prepareDistribution();
            distributeContentData(contentDataList); //distributes the contentDatas among all outputExtensions
            if(canRun()) {  //checks if there are any outputExtensions that can run at all
                for (OutputExtension<T> ext : outputExtensionList) {
                    if (ext.canRun()) //if the specific outputExtension can run, then it does
                        futureList.add(executor.submit(ext));
                }

                //waits until all the outputExtensions have finished processing
                boolean isWorking;
                do {
                    isWorking = true;
                    for (Future<T> cDF : futureList) {
                        if (cDF.isDone())
                            isWorking = false;
                    }
                } while (isWorking);

                //copies the finished future objects into a tDoneList
                for (Future<T> tF : futureList) {
                    try {
                        tDoneList.add(tF.get());
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        //TODO: implement exception handling
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                        //TODO: implement exception handling
                    }
                }

                //render final output
                renderFinalOutput();
            }
        }
    }
}
