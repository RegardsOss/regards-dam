package fr.cnes.regards.modules.entities.service.crawler;

import java.net.UnknownHostException;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

//@RunWith(MockitoJUnitRunner.class)
public class CrawlerServiceTest {

    private ICrawlerService service;

    @Before
    public void tearUp() {
        service = new CrawlerService();
    }

    @After
    public void tearDown() throws Exception {
        service.close();
    }

    @Test
    public void testCreateDeleteIndex() throws UnknownHostException {
        Assert.assertTrue(service.createIndex("test"));
        Assert.assertTrue(service.deleteIndex("test"));
    }

    @Test
    public void testFindIndices() {
        Assert.assertTrue(service.createIndex("toto"));
        Assert.assertTrue(Arrays.stream(service.findIndices()).anyMatch((i) -> i.equals("toto")));
    }

}
