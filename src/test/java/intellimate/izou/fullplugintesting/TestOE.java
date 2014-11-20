package intellimate.izou.fullplugintesting;

import intellimate.izou.contentgenerator.ContentData;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.system.Context;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestOE extends OutputExtension<TestOD> {

    public TestOE(String id, Context context) {
        super(id, context);
        addContentDataToWishList("Test-CG");
        setPluginId("test-OP");
    }

    @Override
    public TestOD call() throws Exception {
        System.out.println("3");
        String finalOutput = "";
        for(ContentData cD: this.getContentDataList())
            finalOutput += cD.getData();
        return new TestOD(finalOutput);
    }
}

