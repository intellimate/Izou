package intellimate.izou.fullplugintesting;

import intellimate.izou.main.Main;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestAll {

    public static boolean isWorking = false;

    @Test
    public void testPlugin() throws Exception {
        Main main = new Main(null, true);

        TestAct testAct = new TestAct();
        main.getActivatorManager().addActivator(testAct);

        TestCG testCG = new TestCG("test_ID");
        main.getResourceManager().registerResourceBuilder(testCG);

        TestOP testOP = new TestOP("1");
        TestOE testOE = new TestOE("1");
        testOE.addResourceIdToWishList("test_ID");
        main.getOutputManager().addOutputPlugin(testOP);
        main.getOutputManager().addOutputExtension(testOE,"1");

        Thread.sleep(10);
        testAct.setStart(true);

        int count = 0;
        int limit = 150;
        while(!isWorking && (count < limit)) {
            Thread.sleep(20);
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
