package intellimate.izou.output;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.Event;
import intellimate.izou.fullplugintesting.TestAddOn;
import intellimate.izou.main.Main;
import intellimate.izou.resource.Resource;
import intellimate.izou.testHelper.IzouTest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OutputPluginTest extends IzouTest{

    public OutputPluginTest() {
        super(true, OutputManagerTest.class.getCanonicalName());
    }

    @Test
    public void testDistributeContentData() throws Exception {
        Optional<Event> event = getNextEvent();
        if(!event.isPresent()) fail();
        List<Resource> resources = Arrays.asList(new Resource<String>("1"),
                new Resource<String>("2"),
                new Resource<String>("3"));
        event.get().getListResourceContainer().addResource(resources);

        OutputPlugin outputPlugin = new OutputPlugin("abcd", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension ext1 = new OutputExtension("789", getContext()) {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(Event event) {
                System.out.println("test");
                return null;
            }
        };
        OutputExtension ext2 = new OutputExtension("10", getContext()) {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(Event event) {
                System.out.println("test");
                return null;
            }
        };
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);

        ext1.addResourceIdToWishList(resources.get(0).getResourceID());
        ext2.addResourceIdToWishList(resources.get(1).getResourceID());
        ext2.addResourceIdToWishList(resources.get(2).getResourceID());

        outputPlugin.distributeEvent(event.get());
        assertTrue((ext2.getEvents().size() == 1) && (ext1.getEvents().size() == 1));
    }

    @Test
    public void testAddOutputExtension() throws Exception {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);

        OutputPlugin outputPlugin = new OutputPlugin("abcd", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension ext1 = new OutputExtension("789", getContext()) {
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
        OutputExtension ext2 = new OutputExtension("10", getContext()) {
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
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);
        assertTrue(outputPlugin.getOutputExtensionList().size() == 2);
    }

    @Test
    public void testRemoveOutputExtension() throws Exception {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);

        OutputPlugin outputPlugin = new OutputPlugin("abcd", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension ext1 = new OutputExtension("789", getContext()) {
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
        OutputExtension ext2 = new OutputExtension("10", getContext()) {
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
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);
        outputPlugin.removeOutputExtension(ext1.getID());
        assertTrue(outputPlugin.getOutputExtensionList().size() == 1 && outputPlugin.getOutputExtensionList().get(0).equals(ext2));
    }

    @Test
    public void testOutputPluginParameters() {
        Optional<Event> event = getEvent(id + 1);
        if(!event.isPresent()) fail();
        List<Resource> resources = Arrays.asList(new Resource<String>("1"),
                new Resource<String>("2"),
                new Resource<String>("3"));
        event.get().getListResourceContainer().addResource(resources);

        OutputPlugin outputPlugin = new OutputPlugin("abcd", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtension ext1 = new OutputExtension("789", getContext()) {
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
        OutputExtension ext2 = new OutputExtension("10", getContext()) {
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
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);

        ext1.addResourceIdToWishList(resources.get(0).getResourceID());
        ext2.addResourceIdToWishList(resources.get(1).getResourceID());
        ext2.addResourceIdToWishList(resources.get(2).getResourceID());

        outputPlugin.distributeEvent(event.get());

        boolean t1, t2, t3;
        t1 = ext1.canRun();
        t2 = ext2.canRun();
        t3 = outputPlugin.canRun();

        assertTrue(t1 && t2 && t3);
    }
}