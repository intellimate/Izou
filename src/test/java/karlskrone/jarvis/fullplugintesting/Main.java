package karlskrone.jarvis.fullplugintesting;

import karlskrone.jarvis.activator.ActivatorManager;
import karlskrone.jarvis.contentgenerator.ContentGeneratorManager;
import karlskrone.jarvis.events.EventManager;
import karlskrone.jarvis.output.OutputManager;

import java.util.Scanner;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class Main {

    public static void main(String[] args) throws EventManager.MultipleEventsException, InterruptedException {
        ActivatorManager activatorManager = new ActivatorManager();
        EventManager eventManager = new EventManager();
        ContentGeneratorManager contentGeneratorManager = new ContentGeneratorManager();
        Thread thread = new Thread(eventManager);
        thread.start();
        TestAct testAct = new TestAct(eventManager);
        OutputManager outputManager = new OutputManager();

        TestCG testCG = new TestCG(eventManager);
        testCG.setContentGeneratorManager(contentGeneratorManager);

        TestOP testOP = new TestOP("1");
        TestOE testOE = new TestOE("1");
        testOE.addContentDataToWishList("TestGC-id");
        outputManager.addOutputPlugin(testOP);
        outputManager.addOutputExtension(testOE,"1");

        Scanner scanner = new Scanner(System.in);

        int input;
        do {
            System.out.println("To start, enter 1");
            input = scanner.nextInt();

            testAct.setStart(input==1);
            testAct.activatorStarts();
        } while(input != 1);
    }
}
