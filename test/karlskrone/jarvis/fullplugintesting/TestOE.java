package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.contentgenerator.ContentData;
import karlskrone.jarvis.output.OutputExtension;

import java.util.List;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestOE extends OutputExtension<TestOD> {

    public TestOE(String id) {
        super(id);
    }

    @Override
    public TestOD call() throws Exception {
        String finalOutput = "";
        for(ContentData cD: this.getContentDataList())
            finalOutput += cD.getData();
        return new TestOD(finalOutput);
    }
}

