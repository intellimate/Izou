package intellimate.izou.output;

import intellimate.izou.contentgenerator.ContentData;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

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

        OutputPlugin outputPlugin = new OutputPlugin("abcd") {
            @Override
            public void renderFinalOutput() {

            }
        };
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

        ext1.addResourceIdToWishList(cd1.getId());
        ext2.addResourceIdToWishList(cd2.getId());
        ext2.addResourceIdToWishList(cd3.getId());

        outputPlugin.addContentDataList(cdList);
        outputPlugin.distributeEvent(cdList);

        assertTrue(ext1.getEvent().size() == 1);
        assertTrue(ext2.getEvent().size() == 2);
    }

    @Test
    public void testAddOutputExtension() throws Exception {
        OutputPlugin outputPlugin = new OutputPlugin("abcd") {
            @Override
            public void renderFinalOutput() {

            }
        };
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
        OutputPlugin outputPlugin = new OutputPlugin("abcd") {
            @Override
            public void renderFinalOutput() {

            }
        };
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
        outputPlugin.removeOutputExtension(ext1.getID());
        assertTrue(outputPlugin.getOutputExtensionList().size() == 1 && outputPlugin.getOutputExtensionList().get(0).equals(ext2));
    }

    @Test
    public void testOutputPluginParameters() {
        ContentData cd1 = new ContentData("1");
        ContentData cd2 = new ContentData("2");
        ContentData cd3 = new ContentData("3");

        List<ContentData> cdList = new ArrayList<>();
        cdList.add(cd1);
        cdList.add(cd2);
        cdList.add(cd3);

        OutputPlugin outputPlugin = new OutputPlugin("abcd") {
            @Override
            public void renderFinalOutput() {

            }
        };
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

        ext1.addResourceIdToWishList(cd1.getId());
        ext2.addResourceIdToWishList(cd2.getId());
        ext2.addResourceIdToWishList(cd3.getId());

        outputPlugin.addContentDataList(cdList);
        outputPlugin.distributeEvent(cdList);

        boolean t1, t2, t3;
        t1 = ext1.canRun();
        t2 = ext2.canRun();
        t3 = outputPlugin.canRun();

        assertTrue(t1 && t2 && t3);
    }
}