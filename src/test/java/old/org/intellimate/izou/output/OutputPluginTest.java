package old.org.intellimate.izou.output;

//import intellimate.izouSDK.resource.ResourceImpl;
import old.org.intellimate.izou.testHelper.IzouTest;
import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.resource.ResourceModel;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class OutputPluginTest extends IzouTest {
//
//    public OutputPluginTest() {
//        super(true, OutputManagerTest.class.getCanonicalName());
//    }
//
//    @Test
//    public void testDistributeContentData() throws Exception {
//        Optional<EventModel> event = getNextEvent();
//        if(!event.isPresent()) fail();
//        List<ResourceModel> resources = Arrays.asList(new ResourceImpl<String>("1"),
//                new ResourceImpl<String>("2"),
//                new ResourceImpl<String>("3"));
//        event.get().getListResourceContainer().addResource(resources);
//
//        OutputPluginImpl outputPlugin = new OutputPluginImpl("abcd", getContext()) {
//            @Override
//            public void renderFinalOutput() {
//
//            }
//        };
//        OutputExtensionimpl ext1 = new OutputExtensionimpl("789", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                System.out.println("test");
//                return null;
//            }
//        };
//        OutputExtensionimpl ext2 = new OutputExtensionimpl("10", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                System.out.println("test");
//                return null;
//            }
//        };
//        outputPlugin.addOutputExtension(ext1);
//        outputPlugin.addOutputExtension(ext2);
//
//        ext1.addResourceIdToWishList(resources.get(0).getResourceID());
//        ext2.addResourceIdToWishList(resources.get(1).getResourceID());
//        ext2.addResourceIdToWishList(resources.get(2).getResourceID());
//
//        outputPlugin.distributeEvent(event.get());
//        assertTrue((ext2.getEvents().size() == 1) && (ext1.getEvents().size() == 1));
//    }
//
//    @Test
//    public void testAddOutputExtension() throws Exception {
//        OutputPluginImpl outputPlugin = new OutputPluginImpl("abcd", getContext()) {
//            @Override
//            public void renderFinalOutput() {
//
//            }
//        };
//        OutputExtensionimpl ext1 = new OutputExtensionimpl("789", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                return null;
//            }
//        };
//        OutputExtensionimpl ext2 = new OutputExtensionimpl("10", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                return null;
//            }
//        };
//        outputPlugin.addOutputExtension(ext1);
//        outputPlugin.addOutputExtension(ext2);
//        assertTrue(outputPlugin.getOutputExtensionList().size() == 2);
//    }
//
//    @Test
//    public void testRemoveOutputExtension() throws Exception {
//        OutputPluginImpl outputPlugin = new OutputPluginImpl("abcd", getContext()) {
//            @Override
//            public void renderFinalOutput() {
//
//            }
//        };
//        OutputExtensionimpl ext1 = new OutputExtensionimpl("789", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                return null;
//            }
//        };
//        OutputExtensionimpl ext2 = new OutputExtensionimpl("10", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                return null;
//            }
//        };
//        outputPlugin.addOutputExtension(ext1);
//        outputPlugin.addOutputExtension(ext2);
//        outputPlugin.removeOutputExtension(ext1.getID());
//        assertTrue(outputPlugin.getOutputExtensionList().size() == 1 && outputPlugin.getOutputExtensionList().get(0).equals(ext2));
//    }
//
//    @Test
//    public void testOutputPluginParameters() {
//        Optional<EventModel> event = getEvent(id + 1);
//        if(!event.isPresent()) fail();
//        List<ResourceModel> resources = Arrays.asList(new ResourceImpl<String>("1"),
//                new ResourceImpl<String>("2"),
//                new ResourceImpl<String>("3"));
//        event.get().getListResourceContainer().addResource(resources);
//
//        OutputPluginImpl outputPlugin = new OutputPluginImpl("abcd", getContext()) {
//            @Override
//            public void renderFinalOutput() {
//
//            }
//        };
//        OutputExtensionimpl ext1 = new OutputExtensionimpl("789", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                return null;
//            }
//        };
//        OutputExtensionimpl ext2 = new OutputExtensionimpl("10", getContext()) {
//            /**
//             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
//             * to the outputPlugin
//             *
//             * @param event
//             */
//            @Override
//            public Object generate(EventModel event) {
//                return null;
//            }
//        };
//        outputPlugin.addOutputExtension(ext1);
//        outputPlugin.addOutputExtension(ext2);
//
//        ext1.addResourceIdToWishList(resources.get(0).getResourceID());
//        ext2.addResourceIdToWishList(resources.get(1).getResourceID());
//        ext2.addResourceIdToWishList(resources.get(2).getResourceID());
//
//        outputPlugin.distributeEvent(event.get());
//
//        boolean t1, t2, t3;
//        t1 = ext1.canRun();
//        t2 = ext2.canRun();
//        t3 = outputPlugin.canRun();
//
//        assertTrue(t1 && t2 && t3);
//    }
}