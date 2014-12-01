package intellimate.izou.output;

import intellimate.izou.addon.AddOn;
import intellimate.izou.contentgenerator.ContentData;
import intellimate.izou.fullplugintesting.TestAddOn;
import intellimate.izou.main.Main;
import intellimate.izou.system.Context;
import intellimate.izou.events.Event;
import intellimate.izou.resource.Resource;
import intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OutputManagerTest extends IzouTest{

    public OutputManagerTest() {
        super(true, OutputManagerTest.class.getCanonicalName());
    }

    @Test
    public void testAddOutputExtension() throws Exception {
        OutputManager outputManager = main.getOutputManager();
        Context context = new Context(testAddOn, main, "1", "debug");
        OutputPlugin outputPlugin = new OutputPlugin("1234") {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension outputExtension = new OutputExtension("abcd") {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(Event event) {
                return null;
            }
        };
        outputManager.addOutputPlugin(outputPlugin);
        outputManager.addOutputExtension(outputExtension, outputPlugin.getID());
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().contains(outputExtension));
    }

    @Test
    public void testRemoveOutputExtension() throws Exception {
        OutputManager outputManager = main.getOutputManager();
        Context context = new Context(testAddOn, main, "1", "debug");
        OutputPlugin outputPlugin = new OutputPlugin("1234") {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension outputExtension = new OutputExtension("abcd") {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(Event event) {
                return null;
            }
        };
        outputManager.addOutputPlugin(outputPlugin);
        outputManager.addOutputExtension(outputExtension, outputPlugin.getID());
        outputManager.removeOutputExtension(outputPlugin.getID(), outputExtension.getID());
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().isEmpty());
    }

    @Test
    public void testPassDataToOutputPlugin() throws Exception {
        Optional<Event> event = getEvent(id + 1);
        Context context = new Context(testAddOn, main, "1", "debug");
        if(!event.isPresent()) fail();

        OutputManager outputManager = main.getOutputManager();
        OutputPlugin outputPlugin = new OutputPlugin("1234") {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension outputExtension = new OutputExtension("abcd") {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(Event event) {
                return null;
            }
        };
        outputExtension.addResourceIdToWishList("1");
        outputExtension.addResourceIdToWishList("2");

        List<Resource> resources = Arrays.asList(new Resource<String>("1"),
                                                new Resource<String>("2"),
                                                new Resource<String>("3"));
        event.get().getListResourceContainer().addResource(resources);

        outputManager.addOutputPlugin(outputPlugin);
        outputManager.addOutputExtension(outputExtension, outputPlugin.getID());
        outputExtension.addResourceIdToWishList("2");
        outputManager.passDataToOutputPlugins(event.get());
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
        OutputExtension outputExtension = new OutputExtension("abcd") {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(Event event) {
                return null;
            }
        };
        outputManager.addOutputExtension(outputExtension, outputPlugin.getID());
        outputManager.addOutputPlugin(outputPlugin);
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().contains(outputExtension));
    }
}