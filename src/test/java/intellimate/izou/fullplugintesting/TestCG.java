package intellimate.izou.fullplugintesting;

import intellimate.izou.contentgenerator.ContentData;
import intellimate.izou.contentgenerator.ContentGeneratorOld;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestCG extends ContentGeneratorOld<String> {

    public TestCG(String contentGeneratorID) {
        super(contentGeneratorID);
        this.registerEvent("1");
    }

    @Override
    public ContentData<String> generate(String eventID) throws Exception {
        System.out.println("2");
        ContentData<String> cd = new ContentData<>("test_ID");
        cd.setData("IT WORKS!!!!!!!!!!!");
        return cd;
    }

    @Override
    public void handleError(Exception e) {

    }
}
