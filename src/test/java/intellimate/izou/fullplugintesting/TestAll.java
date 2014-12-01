package intellimate.izou.fullplugintesting;

import intellimate.izou.main.Main;
import intellimate.izou.activator.Activator;
import intellimate.izou.activator.ActivatorManager;
import intellimate.izou.addon.AddOn;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.contentgenerator.ContentGeneratorManager;
import intellimate.izou.events.EventController;
import intellimate.izou.events.EventManager;
import intellimate.izou.main.Main;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputManager;
import intellimate.izou.output.OutputPlugin;
import intellimate.izou.system.Context;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

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
