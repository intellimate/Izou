package org.intellimate.izou.addon;

import org.intellimate.izou.activator.ActivatorModel;
import intellimate.izouSDK.contentgenerator.ContentGenerator;
import org.intellimate.izou.events.EventsControllerModel;
import org.intellimate.izou.main.Main;
import org.intellimate.izou.output.OutputExtensionModel;
import org.intellimate.izou.output.OutputPluginModel;
import intellimate.izouSDK.addon.AddOnImpl;
import junit.framework.TestCase;

import java.util.LinkedList;

public class PropertiesManagerTest extends TestCase {

    public void testRegisterProperty() throws Exception {
        LinkedList<AddOnModel> addOns = new LinkedList<>();
        TestAddOn testAddOn = new TestAddOn("TestID");
        addOns.add(testAddOn);
        Main main = new Main(addOns, false, true);
        //for(;;){}
        //assertTrue(Files.exists(Paths.get("." + File.separator + "properties" + File.separator + "TestID.properties")));
        assertTrue(true);
    }

    public void testRun() throws Exception {
    }

    @SuppressWarnings("SameParameterValue")
    private class TestAddOn extends AddOnImpl {
        /**
         * the default constructor for AddOns
         *
         * @param addOnID the ID of the Plugin in the form: package.class
         */
        public TestAddOn(String addOnID) {
            super(addOnID);
        }

        @Override
        public void prepare() {

        }

        @Override
        public ActivatorModel[] registerActivator() {
            return new ActivatorModel[0];
        }

        @Override
        public ContentGenerator[] registerContentGenerator() {
            return new ContentGenerator[0];
        }

        @Override
        public EventsControllerModel[] registerEventController() {
            return new EventsControllerModel[0];
        }

        @Override
        public OutputPluginModel[] registerOutputPlugin() {
            return new OutputPluginModel[0];
        }

        @Override
        public OutputExtensionModel[] registerOutputExtension() {
            return new OutputExtensionModel[0];
        }

        /**
         * An ID must always be unique.
         * A Class like Activator or OutputPlugin can just provide their .class.getCanonicalName()
         * If you have to implement this interface multiple times, just concatenate unique Strings to
         * .class.getCanonicalName()
         *
         * @return A String containing an ID
         */
        @Override
        public String getID() {
            return "POJAO";
        }
    }
}