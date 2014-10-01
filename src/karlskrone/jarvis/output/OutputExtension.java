package karlskrone.jarvis.output;

import karlskrone.jarvis.contentgenerator.ContentData;

import java.util.concurrent.Callable;

/**
 * OutputExtension's purpose is to take contentData and convert it into another data format so that it can be rendered correctly
 * by the output-plugin
 *
 * Created by julianbrendl on 9/27/14.
 */
public class OutputExtension implements Callable{
    /**
     * the id of the outputExtension, same as the id of its contentData
     */
    private final String id;
    /**
     * the contentData which it will convert to the desired Data type
     */
    private ContentData contentData;


    /**
     * creates a new outputExtension with a new id
     * @param id the id to be set to the id of outputExtension
     */
    public OutputExtension(String id) {
        this.id = id;
    }

    /**
     * returns its contentData
     * @return the contentData to return
     */
    public ContentData getContentData() {
        return contentData;
    }

    /**
     * sets the contentData so that it can be further used
     *
     * @param contentData the content-data to be set
     */
    public void setContentData(ContentData contentData) {
        this.contentData = contentData;
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
    public Object call() throws Exception {
        return null;
    }
}

