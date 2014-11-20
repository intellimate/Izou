package intellimate.izou.contentgenerator;

import intellimate.izou.addon.AddOn;
import intellimate.izou.events.EventManagerTestSetup;
import intellimate.izou.fullplugintesting.TestAddOn;
import intellimate.izou.main.Main;
import intellimate.izou.system.Context;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class ContentGeneratorManagerTest {
    public static ContentGenerator<Boolean> contentGenerator;
    public static EventManagerTestSetup eventManagerTestSetup;
    public static ContentGeneratorManager contentGeneratorManager;
    private static final class Lock { }
    private final Object lock = new Lock();

    public ContentGeneratorManagerTest() {
        TestAddOn testAddOn = new TestAddOn("test-AddOn");
        List<AddOn> addOnList = new ArrayList<>();
        addOnList.add(testAddOn);
        Main main = new Main(addOnList);
        Context context = new Context(testAddOn, main, "1", "debug");

        eventManagerTestSetup = new EventManagerTestSetup();
        contentGeneratorManager = new ContentGeneratorManager(eventManagerTestSetup.getManager());
        contentGenerator = new ContentGenerator<Boolean>("1", context) {
            @Override
            public ContentData<Boolean> generate(String eventID) throws Exception {
                ContentData<Boolean> cd = new ContentData<>("1");
                cd.setData(true);
                return cd;
            }

            @Override
            public void handleError(Exception e) {

            }
        };
        contentGenerator.registerAllNeededDependencies(contentGeneratorManager, eventManagerTestSetup.getManager());
    }

    @Test
    public void testAddAndGetContentGenerator() throws Exception {
        ContentGeneratorManager cntmgr = new ContentGeneratorManager(eventManagerTestSetup.getManager());
        cntmgr.addContentGenerator(contentGenerator);
        List<ContentGenerator> list = cntmgr.getContentGeneratorList();
        assertTrue((list.size() == 1) && list.contains(contentGenerator));
    }

    @Test
    public void testRemoveContentGenerators() throws Exception {
        ContentGeneratorManager cntmgr = new ContentGeneratorManager(eventManagerTestSetup.getManager());
        cntmgr.addContentGenerator(contentGenerator);
        cntmgr.removeContentGenerators(contentGenerator);
        List<ContentGenerator> list = cntmgr.getContentGeneratorList();
        assertFalse((list.size() == 1) || list.contains(contentGenerator));
    }

    @Test
    public void testDeleteContentGenerators() throws Exception {
        ContentGeneratorManager cntmgr = new ContentGeneratorManager(eventManagerTestSetup.getManager());
        cntmgr.addContentGenerator(contentGenerator);
        cntmgr.deleteContentGenerator(contentGenerator);
        List<ContentGenerator> list = cntmgr.getContentGeneratorList();
        List list2 = contentGenerator.getRegisteredEvents();
        assertFalse((list.size() == 1) || list.contains(contentGenerator) || !list2.isEmpty());
    }
}