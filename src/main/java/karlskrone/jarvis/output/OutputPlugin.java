package karlskrone.jarvis.output;

import karlskrone.jarvis.contentgenerator.ContentData;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
     * list contains all the content-data objects that will be distributed to outputExtension in distributeContentData
     */
    private List<ContentData> contentDataList;

    /**
     * responsible for running output-extensions in different threads
     */
    private final ExecutorService executor;

    /**
     * list that contains the future objects each output-extension returns
     */
    private LinkedList<Future<T>> futureList;

    /**
     * list of future objects that now finished processing and were added to this list
     */
    private List<T> tDoneList;


    /**
     * creates a new output-plugin with a new id
     *
     * @param id the id of the new output-plugin
     */
    public OutputPlugin(String id) {
        this.id = id;
        outputExtensionList = new ArrayList<>();
        contentDataList = new ArrayList<>();
        executor = Executors.newCachedThreadPool();
        futureList = new LinkedList<>();
        tDoneList = new ArrayList<>();
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
     * get contentDataList from outputPlugin
     */
    public List<ContentData> getContentDataList() {
        return contentDataList;
    }

    /**
     * gets the futureList of the output-Plugin, which contains the processed data of the output-extensions
     * @return the futureList to be returnt
     */
    public LinkedList<Future<T>> getFutureList() {
        return futureList;
    }

    /**
     * set contentDataList equal to the contentDataList of the OutputPlugin
     * @param contentDataList the list to be set equal to the contentDataList pertaining to outputPlugin
     */
    public void setContentDataList(List<ContentData> contentDataList) {
        this.contentDataList = contentDataList;
    }

    /**
     * distributes the content-Data elements in the contentDataList to the output-extensions that will need them
     *
     * it uses the id of the contentData which is the same as the id of the outputExtension to identify which output-extension
     * it should send the content-data to
     */
    public void distributeContentData() {
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
     * Gets the id of the outputPlugin
     *
     * @return id of the outputPlugin
     */
    public String getId() {
        return this.id;

    }

    /**
     * add outputExtension to outputExtensionList
     *
     * @param outputExtension the output-extension to be added to outputExtensionList
     */
    public void addOutputExtension(OutputExtension<T> outputExtension) {
        outputExtensionList.add(outputExtension);
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
                outputExtensionList.remove(ext);
                break;
            }
        }
    }

    /**
     * method that uses tDoneList to generate a final output that wil then be rendered
     */
    public abstract void renderFinalOutput();

    /**
     * main method for outputPlugin, runs the data-conversion and output-renderer
     */
    @Override
    public void run() {
        for(OutputExtension<T> ext: outputExtensionList)
            futureList.add(executor.submit(ext));

        boolean isWorking;
        do {
            isWorking = true;
            for(Future<T> cDF: futureList) {
                if(cDF.isDone())
                    isWorking = false;
            }
        } while(isWorking);

        for(Future<T> tF: futureList) {
            try {
                tDoneList.add(tF.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        renderFinalOutput();
    }
}
