package intellimate.izou.fullplugintesting;

import intellimate.izou.contentgenerator.ContentData;
import intellimate.izou.output.OutputExtension;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestOE extends OutputExtension<TestOD> {

    public TestOE(String id) {
        super(id);
    }

    @Override
    public TestOD call() throws Exception {
        System.out.println("3");
        String finalOutput = "";
        for(ContentData cD: this.getEvent())
            finalOutput += cD.getData();
        return new TestOD(finalOutput);
    }
}

