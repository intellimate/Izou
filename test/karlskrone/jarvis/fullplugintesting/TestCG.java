package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.contentgenerator.ContentData;
import karlskrone.jarvis.contentgenerator.ContentGenerator;
import karlskrone.jarvis.events.EventManager;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestCG extends ContentGenerator<String> {

    public TestCG(EventManager eventManager) {
        super(eventManager);
        this.registerEvent("1");
    }

    @Override
    public ContentData<String> generate(String eventID) throws Exception {
        ContentData<String> cd = new ContentData<>("TestGC-id");
        cd.setData("IT WORKS!!!!!!!!!!!");
        return cd;
    }

    @Override
    public void handleError(Exception e) {

    }
}
