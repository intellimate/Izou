package karlskrone.jarvis.contentgenerator;

import karlskrone.jarvis.events.EventManagerTestSetup;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class ContentGeneratorManagerTest {
    public static ContentGenerator<Boolean> contentGenerator;
    public static EventManagerTestSetup eventManagerTestSetup;
    public static ContentGeneratorManager contentGeneratorManager;
    private static final class Lock { }
    private final Object lock = new Lock();

    public ContentGeneratorManagerTest() {
        eventManagerTestSetup = new EventManagerTestSetup();
        contentGeneratorManager = new ContentGeneratorManager(eventManagerTestSetup.getManager());
        contentGenerator = new ContentGenerator<Boolean>("1") {
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
        contentGenerator.setContentGeneratorManager(contentGeneratorManager);
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