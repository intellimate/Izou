package intellimate.izou.fullplugintesting;

import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.contentgenerator.ContentGeneratorManager;
import intellimate.izou.events.EventManager;
import intellimate.izou.output.OutputManager;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestAll {

    public static boolean isWorking = false;

    @Test
    public void testPlugin() throws Exception {
        OutputManager outputManager = new OutputManager();
        EventManager eventManager = new EventManager(outputManager);
        ActivatorManager activatorManager = new ActivatorManager(eventManager);

        ContentGeneratorManager contentGeneratorManager = new ContentGeneratorManager(eventManager);
        Thread thread = new Thread(eventManager);
        thread.start();
        TestAct testAct = new TestAct();
        activatorManager.addActivator(testAct);

        TestCG testCG = new TestCG("12");
        contentGeneratorManager.addContentGenerator(testCG);

        TestOP testOP = new TestOP("1");
        TestOE testOE = new TestOE("1");
        testOE.addContentDataToWishList("TestGC-id");
        outputManager.addOutputPlugin(testOP);
        outputManager.addOutputExtension(testOE,"1");

        Thread.sleep(10);
        testAct.setStart(true);

        int count = 0;
        int limit = 150;
        while(!isWorking && (count < limit)) {
            Thread.sleep(20);
            count++;
        }
        assertTrue(isWorking);
    }


}
