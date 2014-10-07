package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.contentgenerator.ContentData;
import karlskrone.jarvis.output.OutputManager;
import karlskrone.jarvis.output.OutputPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestOP extends OutputPlugin<TestOD> {

    public TestOP(String id) {
        super(id);
    }

    @Override
    public void finalOutput() {
        List<TestOD> testODList = this.getTDoneList();
        String finalOutput = "";
        for(TestOD testOD: testODList)
            finalOutput += testOD.getData() + " ";
        System.out.println(finalOutput);
    }
}
