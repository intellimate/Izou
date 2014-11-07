package intellimate.izou.addon;

import intellimate.izou.activator.Activator;
import intellimate.izou.contentgenerator.ContentGenerator;
import intellimate.izou.events.EventController;
import intellimate.izou.main.Main;
import intellimate.izou.output.OutputExtension;
import intellimate.izou.output.OutputPlugin;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;

public class PropertiesManagerTest extends TestCase {

    public void testRegisterProperty() throws Exception {
        LinkedList<AddOn> addOns = new LinkedList<>();
        TestAddOn testAddOn = new TestAddOn("TestID");
        addOns.add(testAddOn);
        Main main = new Main(addOns);
        for(;;);
    }

    public void testRun() throws Exception {

    }

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
        public EventController[] registerEventController() {
            return new EventController[0];
        }

        @Override
        public OutputPlugin[] registerOutputPlugin() {
            return new OutputPlugin[0];
        }

        @Override
        public OutputExtension[] registerOutputExtension() {
            return new OutputExtension[0];
        }

        @Override
        public Path registerPropertiesFile() {
            String pathString = null;
            try {
                pathString = new File(".").getCanonicalPath() + File.separator;// + "TestID.properties";
            } catch (IOException e) {
                e.printStackTrace();
            }
            return Paths.get(pathString);
        }
    }
}