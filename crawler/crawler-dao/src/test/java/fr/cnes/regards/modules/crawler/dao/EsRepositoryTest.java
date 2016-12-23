package fr.cnes.regards.modules.crawler.dao;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import de.svenjacobs.loremipsum.LoremIpsum;
import fr.cnes.regards.modules.crawler.domain.AbstractIndexable;

/**
 * EsRepository test
 */
public class EsRepositoryTest {

    /**
     * Class to test
     */
    private static IEsRepository repository;

    /**
     * Befor class setting up method
     * @throws Exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        // By now, repository try to connect localhost:9300 for ElasticSearch
        boolean repositoryOK = true;
        try {
            repository = new EsRepository();
        } catch (NoNodeAvailableException e) {
            repositoryOK = false;
        }
        // Do not launch tests is Elasticsearch is not available
        Assume.assumeTrue(repositoryOK);

        final Consumer<String> cleanFct = (pIndex) -> {
            try {
                repository.deleteIndex(pIndex);
            } catch (final IndexNotFoundException infe) {
            }
        };
        // All created indices from tests
        cleanFct.accept("test");
        cleanFct.accept("toto");
        cleanFct.accept("tutu");
        cleanFct.accept("items");
        cleanFct.accept("mergeditems");
        cleanFct.accept("bulktest");
        cleanFct.accept("loading");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (repository != null) {
            repository.close();
        }
    }

    @Test
    public void testCreateDeleteIndex() throws UnknownHostException {
        Assert.assertTrue(repository.createIndex("test"));
        Assert.assertTrue(repository.deleteIndex("test"));
    }

    @Test
    public void testFindIndices() {
        Assert.assertTrue(repository.createIndex("toto"));
        Assert.assertTrue(Arrays.stream(repository.findIndices()).anyMatch((pIndex) -> pIndex.equals("toto")));
    }

    @Test
    public void testIndexExists() {
        Assert.assertTrue(repository.createIndex("tutu"));
        Assert.assertTrue(repository.indexExists("tutu"));
        Assert.assertFalse(repository.indexExists("sqdkljhskjhdfkjhsdfkjhskdjfksjdhfkjhksdjhfksjdfgsjhfgsjdhgfjshdf"));
    }

    /**
     * Test save, get delete
     */
    @Test
    public void testSaveGetDelete() {
        repository.createIndex("items");
        // Creations for first two
        final Item item1 = new Item("1", "test", "group1", "group2", "group3");
        Assert.assertTrue(repository.save("items", item1));
        Assert.assertTrue(repository.save("items", new Item("2", "test", "group1", "group3")));
        // Update
        final Item item2 = new Item("2", "test", "group4", "group5");
        Assert.assertFalse(repository.save("items", item2));

        // Get
        final Item item1FromIndex = repository.get("items", "test", "1", Item.class);
        Assert.assertNotNull(item1FromIndex);
        Assert.assertEquals(item1, item1FromIndex);

        // Get an inexistant item
        Assert.assertNull(repository.get("items", "test", "3", Item.class));

        // Save and get an empty item
        try {
            repository.save("items", new Item());
            Assert.fail("docId and type not provided, this should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        // Testing get method with an Item as parameter (instead of id and type)
        Item toFindItem = new Item("1", "test");
        toFindItem = repository.get("items", toFindItem);

        // Delete
        Assert.assertTrue(repository.delete("items", "test", "1"));
        Assert.assertTrue(repository.delete("items", "test", "2"));
        Assert.assertFalse(repository.delete("items", "test", "4"));
        Assert.assertFalse(repository.delete("items", "test", "1"));

    }

    /**
     * Test merge
     */
    @Test
    public void testMerge() {
        repository.createIndex("mergeditems");
        // Creations for firt two
        final Item item1 = new Item("1", "test", "group1", "group2", "group3");
        // final Item subItem = new Item(10);
        // subItem.setName("Bert");
        // item1.setSubItem(subItem);
        Assert.assertTrue(repository.save("mergeditems", item1));

        // Add name and change groups
        final Map<String, Object> propsMap = new HashMap<>();
        propsMap.put("name", "Robert");
        propsMap.put("groups", new String[] { "group1" });
        Assert.assertTrue(repository.merge("mergeditems", "test", "1", propsMap));
        Item item1FromIndex = repository.get("mergeditems", "test", "1", Item.class);
        Assert.assertNotNull(item1FromIndex.getName());
        Assert.assertEquals("Robert", item1FromIndex.getName());
        Assert.assertNotNull(item1FromIndex.getGroups());
        Assert.assertEquals(1, item1FromIndex.getGroups().size());
        Assert.assertEquals("group1", item1FromIndex.getGroups().get(0));
        Assert.assertEquals("1", item1FromIndex.getDocId());

        // Adding subItem
        propsMap.clear();
        propsMap.put("subItem.name", "Bart");
        propsMap.put("subItem.groups", new String[] { "G1, G2" });

        Assert.assertTrue(repository.merge("mergeditems", "test", "1", propsMap));
        item1FromIndex = repository.get("mergeditems", "test", "1", Item.class);
        Assert.assertNotNull(item1FromIndex.getSubItem());
        Assert.assertEquals("Bart", item1FromIndex.getSubItem().getName());
        Assert.assertNull(item1FromIndex.getSubItem().getDocId());
        Assert.assertEquals(Arrays.asList(new String[] { "G1, G2" }), item1FromIndex.getSubItem().getGroups());
    }

    @Test
    public void testBulkSave() {
        // Twice same first item (=> create then update) plus an empty item
        final Item item1 = new Item("1", "test", "group1", "group2", "group3");
        final Item item2 = new Item("1", "test", "group1", "group2", "group3");
        final List<Item> list = new ArrayList<>();
        list.add(item1);
        list.add(item2);
        list.add(new Item());
        try {
            repository.saveBulk("bulktest", list);
            Assert.fail("saveBulk should have thrown an IllegalArgumentException (last item does not provide id nor "
                    + "type ");
        } catch (IllegalArgumentException e) {
        }

        // remove failed item
        list.remove(2);
        final Map<String, Throwable> errorMap = repository.saveBulk("bulktest", list);
        Assert.assertNull(errorMap);

        // If someone could find a case when a document save failed...Don't hesitate to talk it to me
    }

    // @Test
    public void testLoad() {
        loadItemsBulk(100_000);
    }

    /**
     * Load generated data into Elsaticsearch
     * @param pCount number of documents to insert
     */
    private void loadItemsBulk(int pCount) {
        repository.createIndex("loading");

        final LoremIpsum loremIpsum = new LoremIpsum();
        final String[] words = loremIpsum.getWords(100).split(" ");

        final List<Item> items = new ArrayList<>();
        for (int i = 0; i < pCount; i++) {
            final Item item = new Item(Integer.toString(i), "test",
                    Stream.generate(() -> words[(int) (Math.random() * words.length)]).limit((int) (Math.random() * 10))
                            .collect(Collectors.toSet()).toArray(new String[0]));
            item.setName(words[(int) (Math.random() * words.length)]);
            item.setHeight((int) (Math.random() * 1000));
            item.setPrice(Math.random() * 10000.);
            items.add(item);
        }
        final long start = System.currentTimeMillis();
        repository.saveBulk("loading", items);
        System.out.println("Loading (" + pCount + " items): " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Test of search all
     */
    @Test
    public void testSearchAll() {
        int count = 100_000;
        loadItemsBulk(count);

        Page<Item> itemsPage = repository.searchAllLimited("loading", Item.class, 100);
        while (!itemsPage.isLast() && (itemsPage.getNumber() < 99)) {
            itemsPage = repository.searchAllLimited("loading", Item.class, itemsPage.nextPageable());
        }
        final AtomicInteger i = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        repository.searchAll("loading", h -> i.getAndIncrement());
        System.out.println((System.currentTimeMillis() - start) + " ms");
        Assert.assertEquals(count, i.get());

        final ObjectMapper jsonMapper = new ObjectMapper();
        start = System.currentTimeMillis();
        repository.searchAll("loading", h -> {
            try {
                Item item = jsonMapper.readValue(h.getSourceAsString(), Item.class);
            } catch (final IOException e) {
                Throwables.propagate(e);
            }
        });
        System.out.println((System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Item class
     */
    private static class Item extends AbstractIndexable implements Serializable {

        private String name;

        private List<String> groups;

        private Item subItem;

        private int height;

        private double price;

        public Item() {
        }

        public Item(String id, String type, String... groups) {
            super(id, type);
            this.groups = Lists.newArrayList(groups);
        }

        public Item(String id, String type, String name, int height, double price, String... groups) {
            this(id, type, groups);
            this.name = name;
            this.height = height;
            this.price = price;
        }

        public List<String> getGroups() {
            return groups;
        }

        @SuppressWarnings("unused")
        public void setGroups(List<String> pGroups) {
            groups = pGroups;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Item getSubItem() {
            return subItem;
        }

        public void setSubItem(Item subItem) {
            this.subItem = subItem;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        @JsonProperty("id")
        @Override
        public String getDocId() {
            return super.getDocId();
        }

        @Override
        public void setDocId(String pDocId) {
            super.setDocId(pDocId);
        }

    }
}
