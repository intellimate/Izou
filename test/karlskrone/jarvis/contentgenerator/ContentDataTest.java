package karlskrone.jarvis.contentgenerator;

import org.junit.Test;

import static org.junit.Assert.*;

public class ContentDataTest {
    public static ContentData<Integer> contentData;

    public ContentDataTest()
    {
        contentData = new ContentData<>("1");
    }

    @Test
    public void testGetId() throws Exception {
        contentData = new ContentData<>("2");
        assertTrue(contentData.getId().equals("2"));
    }

    @Test
    public void testSetDataAndGetData() throws Exception {
        contentData.setData(1);
        assertTrue(contentData.getData() == 1);
    }
}