package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.contentgenerator.ContentGeneratorOld;
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
        Main main = new Main(addOns);
        for(;;){}
        //TODO: @Julian the for loop never ends
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
        public ContentGeneratorOld[] registerContentGenerator() {
            return new ContentGeneratorOld[0];
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
    }
}