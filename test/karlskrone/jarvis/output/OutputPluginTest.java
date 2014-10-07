package karlskrone.jarvis.output;

import karlskrone.jarvis.contentgenerator.ContentData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import static org.junit.Assert.*;

public class OutputPluginTest {

    private static final class Lock { }
    private final Object lock = new Lock();

    @Test
    public void testDistributeContentData() throws Exception {
        ContentData cd1 = new ContentData("1");
        ContentData cd2 = new ContentData("2");
        ContentData cd3 = new ContentData("3");

        List<ContentData> cdList = new ArrayList<>();
        cdList.add(cd1);
        cdList.add(cd2);
        cdList.add(cd3);

        OutputPlugin outputPlugin = new OutputPlugin("abcd");
        OutputExtension ext1 = new OutputExtension("789") {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        OutputExtension ext2 = new OutputExtension("10") {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);

        ext1.addContentDataToWishList(cd1.getId());
        ext2.addContentDataToWishList(cd2.getId());
        ext2.addContentDataToWishList(cd3.getId());

        outputPlugin.setContentDataList(cdList);
        outputPlugin.distributeContentData();

        assertTrue(ext1.getContentDataList().size() == 1);
        assertTrue(ext2.getContentDataList().size() == 2);
    }

    @Test
    public void testAddOutputExtension() throws Exception {
        OutputPlugin outputPlugin = new OutputPlugin("abcd");
        OutputExtension ext1 = new OutputExtension("789") {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        OutputExtension ext2 = new OutputExtension("10") {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);
        assertTrue(outputPlugin.getOutputExtensionList().size() == 2);
    }

    @Test
    public void testRemoveOutputExtension() throws Exception {
        OutputPlugin outputPlugin = new OutputPlugin("abcd");
        OutputExtension ext1 = new OutputExtension("789") {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        OutputExtension ext2 = new OutputExtension("10") {
            @Override
            public Object call() throws Exception {
                return null;
            }
        };
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);
        outputPlugin.removeOutputExtension(ext1.getId());
        assertTrue(outputPlugin.getOutputExtensionList().size() == 1 && outputPlugin.getOutputExtensionList().get(0).equals(ext2));
    }

    @Test
    public void testRun() throws Exception {
        boolean[] isWorking = {false, false, false};
        OutputPlugin outputPlugin = new OutputPlugin("abcd");

        OutputExtension ext1 = new OutputExtension("789") {
            @Override
            public Object call() throws Exception {
                isWorking[0]  = true;
                return null;
            }
        };

        OutputExtension ext2 = new OutputExtension("10") {
            @Override
            public Object call() throws Exception {
                isWorking[1]  = true;
                return null;
            }
        };

        OutputExtension ext3 = new OutputExtension("140") {
            @Override
            public Object call() throws Exception {
                isWorking[2]  = true;
                return null;
            }
        };
        outputPlugin.addOutputExtension(ext1);
        outputPlugin.addOutputExtension(ext2);
        outputPlugin.addOutputExtension(ext3);

        outputPlugin.run();

        synchronized (lock) {
            boolean finished;
            do {
                finished = true;
                for (int i = 0; i < outputPlugin.getFutureList().size(); i++) {
                    LinkedList<Future> fList = outputPlugin.getFutureList();
                    Future f = fList.get(i);
                    if (!f.isDone())
                        finished = false;
                }
                lock.wait(5);
            }
            while (!finished);
        }

        assertTrue(isWorking[0] && isWorking[1] && isWorking[2]);


    }
}