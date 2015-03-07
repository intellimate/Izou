package intellimate.izou.fullplugintesting;

import intellimate.izou.output.OutputPluginImpl;

import java.util.List;

/**
 * Created by julianbrendl on 10/7/14.
 */
@SuppressWarnings("SameParameterValue")
public class TestOP extends OutputPluginImpl<TestOD> {

    public TestOP(String id, intellimate.izou.system.context.Context context) {
        super(id, context);
    }

    @Override
    public void renderFinalOutput() {
        System.out.println("4");
        List<TestOD> testODList = this.getTDoneList();
        String finalOutput = "";
        for(TestOD testOD: testODList)
            finalOutput += testOD.getData() + " ";
        System.out.println(finalOutput);
        TestAll.isWorking = true;
    }
}
