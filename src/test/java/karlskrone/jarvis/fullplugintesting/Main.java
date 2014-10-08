package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.contentgenerator.ContentGeneratorManager;
import karlskrone.jarvis.events.EventManager;
import karlskrone.jarvis.output.OutputManager;
import org.junit.Test;

import java.util.Scanner;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class Main {

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
    }
}
