package karlskrone.jarvis.contentgenerator;

/**
 * Created by Leander on 27.09.2014.
 */
public class ContentData<T> {
    private String id;
    private T t;
    public ContentData (String id) {
        this.id = id;
    }

    public String getId () {
        return id;
    }

    public void setData (T t) {
        synchronized (t) {
            this.t = t;
        }
    }

    public T getData () {
        synchronized (t) {
            return t;
        }
    }
}
