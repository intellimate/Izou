package intellimate.izou.fullplugintesting;

import intellimate.izou.addon.AddOn;
import intellimate.izou.main.Main;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by julianbrendl on 10/7/14.
 */
public class TestAll {
    public static boolean isWorking = false;

    @Test
    public void testPlugin() throws Exception {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList, true);
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
