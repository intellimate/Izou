package intellimate.izou.output;

import intellimate.izou.addon.AddOn;
import intellimate.izou.contentgenerator.ContentData;
import intellimate.izou.fullplugintesting.TestAddOn;
import intellimate.izou.main.Main;
import intellimate.izou.system.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class OutputManagerTest {

    @Test
    public void testAddOutputExtension() throws Exception {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);
        Context context = new Context(testAddOn, main, "1", "debug");

        OutputManager outputManager = new OutputManager();
        OutputPlugin outputPlugin = new OutputPlugin("1234", context) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension outputExtension = new OutputExtension("abcd", context) {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        outputManager.addOutputPlugin(outputPlugin);
        outputManager.addOutputExtension(outputExtension, outputPlugin.getId());
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().contains(outputExtension));
    }

    @Test
    public void testRemoveOutputExtension() throws Exception {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);
        Context context = new Context(testAddOn, main, "1", "debug");

        OutputManager outputManager = new OutputManager();
        OutputPlugin outputPlugin = new OutputPlugin("1234", context) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension outputExtension = new OutputExtension("abcd", context) {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        outputManager.addOutputPlugin(outputPlugin);
        outputManager.addOutputExtension(outputExtension, outputPlugin.getId());
        outputManager.removeOutputExtension(outputPlugin.getId(), outputExtension.getId());
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().isEmpty());
    }

    @Test
    public void testPassDataToOutputPlugin() throws Exception {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);
        Context context = new Context(testAddOn, main, "1", "debug");

        List<ContentData> list = new ArrayList<>();
        OutputManager outputManager = new OutputManager();
        OutputPlugin outputPlugin = new OutputPlugin("1234", context) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension outputExtension = new OutputExtension("abcd", context) {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };

        ContentData cD1 = new ContentData("1");
        ContentData cD2 = new ContentData("2");
        ContentData cD3 = new ContentData("3");
        list.add(cD1);
        list.add(cD2);
        list.add(cD3);

        outputManager.addOutputPlugin(outputPlugin);
        outputManager.addOutputExtension(outputExtension, outputPlugin.getId());
        outputExtension.addContentDataToWishList("2");
        outputManager.passDataToOutputPlugins(list);
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().size() == 1);
    }

    @Test
    public void testAddOutputExtensionLater() throws Exception {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);
        Context context = new Context(testAddOn, main, "1", "debug");

        OutputManager outputManager = new OutputManager();
        OutputPlugin outputPlugin = new OutputPlugin("1234", context) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension outputExtension = new OutputExtension("abcd", context) {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        outputManager.addOutputExtension(outputExtension, outputPlugin.getId());
        outputManager.addOutputPlugin(outputPlugin);
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().contains(outputExtension));
    }
}