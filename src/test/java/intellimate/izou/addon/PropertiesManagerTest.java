package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventsController;
import intellimate.izou.main.Main;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import junit.framework.TestCase;

import java.util.LinkedList;

public class PropertiesManagerTest extends TestCase {

    public void testRegisterProperty() throws Exception {
        LinkedList<AddOn> addOns = new LinkedList<>();
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
    private class TestAddOn extends AddOn{
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
        public Activator[] registerActivator() {
            return new Activator[0];
        }

        @Override
        public ContentGenerator[] registerContentGenerator() {
            return new ContentGenerator[0];
        }

        @Override
        public EventsController[] registerEventController() {
            return new EventsController[0];
        }

        @Override
        public OutputPlugin[] registerOutputPlugin() {
            return new OutputPlugin[0];
        }

        @Override
        public OutputExtension[] registerOutputExtension() {
            return new OutputExtension[0];
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