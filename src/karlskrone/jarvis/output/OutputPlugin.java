package karlskrone.jarvis.output;

import karlskrone.jarvis.contentgenerator.ContentData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The OutputPlugin class gets contentData and then starts threads filled with output-extension tasks to create the final
 * output and then render it on its own medium
 *
 * Created by julianbrendl on 9/27/14.
 */
public class OutputPlugin implements Runnable{
    /**
     * id of the of OutputPlugin, it is primarily used by OutputManager to communicate with specific output plugins
     */
    private final String id;
    /**
     * a List containing all the output-extensions the plugin requires to successfully create its final output
     */
    private List<OutputExtension> outputExtensionList;


    /**
     * list contains all the content-data objects that will be distributed to outputExtension in distributeContentData
     */
    private List<ContentData> contentDataList;

    /**
     * responsible for running output-extensions in different threads
     */
    private final ExecutorService executor;


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
    }

    /**
     * get contentDataList from outputPlugin
     */
    public List<ContentData> getContentDataList() {
        return contentDataList;
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
    public void distributContentData() {
        for(ContentData cD: contentDataList) {
            for(OutputExtension ext: outputExtensionList) {
                if(cD.getId().equals(ext.getId())) {
                    ext.setContentData(cD);
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
    public void addOutputExtension(OutputExtension outputExtension) {
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
     * main method for outputPlugin, runs the data-conversion and output-renderer
     */
    @Override
    public void run() {
        for(OutputExtension ext: outputExtensionList)
            executor.submit(ext);
    }
}
