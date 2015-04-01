package org.intellimate.izou.output;

import org.intellimate.izou.events.EventModel;
import org.intellimate.izou.resource.ResourceModel;
import intellimate.izouSDK.resource.ResourceImpl;
import org.intellimate.izou.testHelper.IzouTest;
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
        OutputPluginModel outputPlugin = new OutputPluginImpl("1234", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtensionimpl outputExtension = new OutputExtensionimpl("abcd", getContext()) {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(EventModel event) {
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
        OutputPluginModel outputPlugin = new OutputPluginImpl("1234", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtensionimpl outputExtension = new OutputExtensionimpl("abcd", getContext()) {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(EventModel event) {
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
        Optional<EventModel> event = getEvent(id + 1);
        if(!event.isPresent()) fail();

        OutputManager outputManager = main.getOutputManager();
        OutputPluginModel outputPlugin = new OutputPluginImpl("1234", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtensionimpl outputExtension = new OutputExtensionimpl("abcd", getContext()) {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(EventModel event) {
                return null;
            }
        };
        outputExtension.addResourceIdToWishList("1");
        outputExtension.addResourceIdToWishList("2");

        List<ResourceModel> resources = Arrays.asList(new ResourceImpl<String>("1"),
                                                new ResourceImpl<String>("2"),
                                                new ResourceImpl<String>("3"));
        event.get().getListResourceContainer().addResource(resources);

        outputManager.addOutputPlugin(outputPlugin);
        outputManager.addOutputExtension(outputExtension, outputPlugin.getID());
        outputExtension.addResourceIdToWishList("2");
        outputManager.passDataToOutputPlugins(event.get());
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().size() == 1);
    }

    @Test
    public void testAddOutputExtensionLater() throws Exception {
        OutputManager outputManager = new OutputManager(getMain());
        OutputPluginModel outputPlugin = new OutputPluginImpl("1234", getContext()) {
            @Override
            public void renderFinalOutput() {

            }
        };
        OutputExtensionimpl outputExtension = new OutputExtensionimpl("abcd", getContext()) {
            /**
             * the main method of the outputExtension, it converts the resources into the necessary data format and returns it
             * to the outputPlugin
             *
             * @param event
             */
            @Override
            public Object generate(EventModel event) {
                return null;
            }
        };
        outputManager.addOutputExtension(outputExtension, outputPlugin.getID());
        outputManager.addOutputPlugin(outputPlugin);
        assertTrue(outputManager.getOutputPluginsList().get(0).getOutputExtensionList().contains(outputExtension));
    }
}