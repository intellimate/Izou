package karlskrone.jarvis.output;

import java.util.concurrent.Callable;

/**
 * Created by julianbrendl on 9/27/14.
 */
public class OutputExtension implements Callable{
    private String id;

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
