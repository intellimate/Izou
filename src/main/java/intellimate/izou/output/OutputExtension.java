package intellimate.izou.output;

import intellimate.izou.contentgenerator.ContentData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * OutputExtension's purpose is to take contentData and convert it into another data format so that it can be rendered correctly
 * by the output-plugin. These objects are represented in the form of future objects that are stored in tDoneList
 */
public abstract class OutputExtension<T> implements Callable<T> {

    /**
     * the id of the outputExtension, same as the id of its contentData
     */
    private final String id;

    /**
     * the id of the outputPlugin the outputExtension belongs to
     */
    private String pluginId;

    /**
     * the contentDatas which it will convert to the desired Data type
     */
    private List<ContentData> contentDataList;

    /**
     * a list of all the contentData's which the outputExtension would like to receive theoretically to work with
     */
    private List<String> contentDataWishList;


    /**
     * creates a new outputExtension with a new id
     * @param id the id to be set to the id of outputExtension
     */
    public OutputExtension(String id) {
        this.id = id;
        contentDataList = new ArrayList<>();
        contentDataWishList = new ArrayList<>();
    }

    /**
     * returns its contentDataList
     * @return the contentDataList to return
     */
    public List<ContentData> getContentDataList() {
        return contentDataList;
    }

    /**
     * returns its contentDataWishList
     * @return the contentDataWishList to return
     */
    public List<String> getContentDataWishList() {
        return contentDataWishList;
    }

    /**
     * adds contentData to the contentDataList
     *
     * @param contentData the content-data to be added
     */
    public void addContentData(ContentData contentData) {
        contentDataList.add(contentData);
    }

    /**
     * checks if the outputExtension can execute with the current content-Datas
     *
     * @return the state of whether the outputExtension can execute with the current content-Datas
     */
    public boolean canRun() {
        return !contentDataList.isEmpty();
    }

    /**
     * removes content-data with id: id from the contentDataList
     *
     * @param id the id of the content-data to be removed
     */
    public void removeContentData(String id) {
        for(ContentData cD: contentDataList) {
            if(cD.getId().equals(id))
                contentDataList.remove(cD);
        }
    }

    /**
     * adds id of content-Datas to the wish list
     *
     * @param id the id of the content-data to be added to the wish list
     */
    public void addContentDataToWishList(String id) {
        contentDataWishList.add(id);
    }

    /**
     * removes content-data with id: id from the contentDataWishList
     *
     * @param id the id of the content-data to be removed
     */
    public void removeContentDataFromWishList(String id) {
        for(String str: contentDataWishList) {
            if(str.equals(id))
                contentDataWishList.remove(str);
        }
    }

    /**
     * returns the id of the outputExtension
     *
     * @return id of outputExtension to be returned
     */
    public String getId() {
        return id;
    }

    /**
     * gets the id of the output-plugin the outputExtension belongs to
     * @return id of the output-plugin the outputExtension belongs to
     */
    public String getPluginId() {
        return pluginId;
    }

    /**
     * sets the id of the output-plugin the outputExtension belongs to
     * @param pluginId the id of the output-plugin the outputExtension belongs to that is to be set
     */
    public void setPluginId(String pluginId) {
        this.pluginId = pluginId;
    }

    @Override
    /**
     * the main method of the outputExtension, it converts the contentData into the necessary data format and returns it
     * to the outputPlugin
     */
    public abstract T call() throws Exception;
}

