package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.output.OutputPlugin;

import java.util.List;

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
