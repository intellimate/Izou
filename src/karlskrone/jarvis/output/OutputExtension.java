package karlskrone.jarvis.output;

import karlskrone.jarvis.contentgenerator.ContentData;

import java.util.concurrent.Callable;

/**
 * Created by julianbrendl on 9/27/14.
 */
public class OutputExtension implements Callable{
    private String id;
    private ContentData contentData;

    public ContentData getContentData() {
        return contentData;
    }

    public void setContentData(ContentData contentData) {
        this.contentData = contentData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public Object call() throws Exception {
        return null;
    }
}
