package intellimate.izou.fullplugintesting;

import intellimate.izou.contentgenerator.ContentData;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventManager;
import intellimate.izou.system.Context;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestCG extends ContentGenerator<String> {

    public TestCG(String contentGeneratorID, Context context) {
        super(contentGeneratorID, context);
        this.registerEvent(EventManager.FULL_WELCOME_EVENT);
    }

    @Override
    public ContentData<String> generate(String eventID) throws Exception {
        System.out.println("2");
        ContentData<String> cd = new ContentData<>("Test-CG");
        cd.setData("IT WORKS!!!!!!!!!!!");
        return cd;
    }

    @Override
    public void handleError(Exception e) {

    }
}
