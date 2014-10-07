package karlskrone.jarvis.output;

import karlskrone.jarvis.contentgenerator.ContentData;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * OutputExtension's purpose is to take contentData and convert it into another data format so that it can be rendered correctly
 * by the output-plugin
 *
 * Created by julianbrendl on 9/27/14.
 */
public abstract class OutputExtension<T> implements Callable<T> {
    /**
     * the id of the outputExtension, same as the id of its contentData
     */
    private final String id;
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

    @Override
    /**
     * the main method of the outputExtension, it converts the contentData into the necessary data format and returns it
     * to the outputPlugin
     */
    public abstract T call() throws Exception;
}

