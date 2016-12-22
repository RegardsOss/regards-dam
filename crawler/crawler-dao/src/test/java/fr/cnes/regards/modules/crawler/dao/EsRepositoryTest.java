package fr.cnes.regards.modules.crawler.dao;

import java.io.IOException;
import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.index.IndexNotFoundException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.data.domain.Page;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;

import de.svenjacobs.loremipsum.LoremIpsum;

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
        repository = new EsRepository();
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
        // cleanFct.accept("loading");
    }

    @AfterClass
    public static void tearDown() throws Exception {
        repository.close();
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
        final Item item1 = new Item(1, "group1", "group2", "group3");
        Assert.assertTrue(repository.save("items", "test", "1", item1));
        Assert.assertTrue(repository.save("items", "test", "2", new Item(2, "group1", "group3")));
        // Update
        final Item item2 = new Item(2, "group4", "group5");
        Assert.assertFalse(repository.save("items", "test", "2", item2));

        // Get
        final Item item1FromIndex = repository.get("items", "test", "1", Item.class);
        Assert.assertNotNull(item1FromIndex);
        Assert.assertEquals(item1, item1FromIndex);

        // Get an inexistant item
        Assert.assertNull(repository.get("items", "test", "3", Item.class));

        // Save and get an empty item
        repository.save("items", "test", "3", new Item());
        Assert.assertNotNull(repository.get("items", "test", "3", Item.class));

        // Delete
        Assert.assertTrue(repository.delete("items", "test", "1"));
        Assert.assertTrue(repository.delete("items", "test", "2"));
        Assert.assertTrue(repository.delete("items", "test", "3"));
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
        final Item item1 = new Item(1, "group1", "group2", "group3");
        // final Item subItem = new Item(10);
        // subItem.setName("Bert");
        // item1.setSubItem(subItem);
        Assert.assertTrue(repository.save("mergeditems", "test", "1", item1));

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
        Assert.assertEquals(1, item1FromIndex.getId());

        // Adding subItem
        propsMap.clear();
        propsMap.put("subItem.name", "Bart");
        propsMap.put("subItem.id", 20);
        propsMap.put("subItem.groups", new String[] { "G1, G2" });

        Assert.assertTrue(repository.merge("mergeditems", "test", "1", propsMap));
        item1FromIndex = repository.get("mergeditems", "test", "1", Item.class);
        Assert.assertNotNull(item1FromIndex.getSubItem());
        Assert.assertEquals("Bart", item1FromIndex.getSubItem().getName());
        Assert.assertEquals(20, item1FromIndex.getSubItem().getId());
        Assert.assertEquals(Arrays.asList(new String[] { "G1, G2" }), item1FromIndex.getSubItem().getGroups());
    }

    @Test
    public void testBulkSave() {
        final Item item1 = new Item(1, "group1", "group2", "group3");
        final Item item2 = new Item(1, "group1", "group2", "group3");
        final Map<String, Object> map = new HashMap<>();
        map.put("1", item1);
        map.put("2", item2);
        map.put("25", this.getClass());
        final Map<String, Throwable> errorMap = repository.saveBulk("bulktest", "items", map);
        Assert.assertNotNull(errorMap);
        Assert.assertEquals(1, errorMap.size());
    }

    @Test
    public void testLoad() {
        loadItemsBulk(100_000);
    }

    /**
     * Load generated data into Elsaticsearch
     * @param pCount number of documents to insert
     */
    private void loadItemsBulk(int pCount) {
        final LoremIpsum loremIpsum = new LoremIpsum();
        final String[] words = loremIpsum.getWords(100).split(" ");

        final Map<String, Item> itemMap = new HashMap<>();
        for (int i = 0; i < pCount; i++) {
            final Item item = new Item(i, Stream.generate(() -> words[(int) (Math.random() * words.length)])
                    .limit((int) (Math.random() * 10)).collect(Collectors.toSet()).toArray(new String[0]));
            item.setName(words[(int) (Math.random() * words.length)]);
            item.setId(i);
            item.setHeight((int) (Math.random() * 1000));
            item.setPrice(Math.random() * 10000.);
            itemMap.put(Integer.toString(i), item);
        }
        final long start = System.currentTimeMillis();
        repository.saveBulk("loading", "items", itemMap);
        System.out.println("Loading (" + pCount + " items): " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Test of search all
     */
    @Test
    public void testSearchAll() {
        Page<Item> itemsPage = repository.searchAllLimited("loading", Item.class, 100);
        while (!itemsPage.isLast() && (itemsPage.getNumber() < 99)) {
            itemsPage = repository.searchAllLimited("loading", Item.class, itemsPage.nextPageable());
        }
        final AtomicInteger i = new AtomicInteger(0);
        long start = System.currentTimeMillis();
        repository.searchAll("loading", h -> i.getAndIncrement());
        System.out.println((System.currentTimeMillis() - start) + " ms");
        Assert.assertEquals(100_000, i.get());

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
    private static final class Item implements Serializable {

        private int id;

        private String name;

        private List<String> groups;

        private Item subItem;

        private int height;

        private double price;

        public Item() {
        }

        public Item(int id, String... groups) {
            this.id = id;
            this.groups = Lists.newArrayList(groups);
        }

        public Item(int id, String name, int height, double price, String... groups) {
            this(id, groups);
            this.name = name;
            this.height = height;
            this.price = price;
        }

        public int getId() {
            return id;
        }

        @SuppressWarnings("unused")
        public void setId(int pId) {
            id = pId;
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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + id;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Item other = (Item) obj;
            if (id != other.id) {
                return false;
            }
            return true;
        }

    }
}
