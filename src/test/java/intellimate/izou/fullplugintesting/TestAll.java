package intellimate.izou.fullplugintesting;

import intellimate.izou.main.Main;
import intellimate.izou.system.Context;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestAll {
    public static boolean isWorking = false;

    @Test
    public void testPlugin(Context context) throws Exception {
        Main main = new Main(null, true);

        TestAct testAct = new TestAct(context);
        main.getActivatorManager().addActivator(testAct);

        TestCG testCG = new TestCG("test_ID", context);
        main.getResourceManager().registerResourceBuilder(testCG);

        TestOP testOP = new TestOP("1", context);
        TestOE testOE = new TestOE("1", context);
        testOE.addResourceIdToWishList("test_ID");
        main.getOutputManager().addOutputPlugin(testOP);
        main.getOutputManager().addOutputExtension(testOE,"1");

        Thread.sleep(10);
        testAct.setStart(true);

        int count = 0;
        int limit = 150;
        while(!isWorking && (count < limit)) {
            Thread.sleep(50);
            count++;
        }
        //should print out:
        //1
        //2
        //3
        //4
        //It Works
        assertTrue(isWorking);
    }
}
