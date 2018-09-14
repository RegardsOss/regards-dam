package fr.cnes.regards.modules.indexer.dao;

import java.io.Serializable;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.svenjacobs.loremipsum.LoremIpsum;
import fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SearchKey;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.facet.FacetType;

/**
 * EsRepository test
 * @author oroussel
 */
@RunWith(SpringRunner.class)
@TestPropertySource("classpath:test.properties")
public class EsRepositoryTest {

    /**
     * Class to test
     */
    private static IEsRepository repository;

    /**
     * JSON mapper
     */
    private static Gson gson;

    @Value("${regards.elasticsearch.address}")
    private String elasticHost;

    @Value("${regards.elasticsearch.http.port}")
    private int elasticPort;

    /**
     * Befor class setting up method
     * @throws Exception exception
     */
    @Before
    public void setUp() throws Exception {
        boolean repositoryOK = true;
        // we get the properties into target/test-classes because this is where maven will put the filtered file(with real values and not placeholder)
        try {
            gson = new GsonBuilder().create();
            repository = new EsRepository(gson, null, elasticHost, elasticPort,
                                          new AggregationBuilderFacetTypeVisitor(10, 1));
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
        cleanFct.accept("titi");
        cleanFct.accept("tutu");
        cleanFct.accept("items");
        cleanFct.accept("mergeditems");
        cleanFct.accept("bulktest");
        cleanFct.accept("loading");
    }

    @After
    public void tearDown() throws Exception {
        if (repository != null) {
            repository.close();
        }
    }

    @Test
    public void testDeleteByQuery() {
        if (repository.indexExists("Pouet")) {
            repository.deleteIndex("Pouet");
        }
        repository.createIndex("Pouet");
        repository.deleteAll("Pouet");
    }

    @Test
    public void testCreateDeleteIndex() throws UnknownHostException {
        Assert.assertTrue(repository.createIndex("test"));
        Assert.assertTrue(repository.deleteIndex("test"));
    }

    @Test
    public void testCreateIndexWithSpecialMappings() {
        Assert.assertTrue(repository.createIndex("test"));
        String[] types = { "pipo", "bimbo" };
        repository.setAutomaticDoubleMapping("test", types);
        repository.setGeometryMapping("test", types);

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
        final Item item1 = new Item("1", "group1", "group2", "group3");
        Assert.assertTrue(repository.save("items", item1));
        Assert.assertTrue(repository.save("items", new Item("2", "group1", "group3")));
        // Update
        final Item item2 = new Item("2", "group4", "group5");
        Assert.assertFalse(repository.save("items", item2));

        // Get
        final Item item1FromIndex = repository.get("items", "item", "1", Item.class);
        Assert.assertNotNull(item1FromIndex);
        Assert.assertEquals(item1, item1FromIndex);

        // Get an inexistant item
        Assert.assertNull(repository.get("items", "item", "3", Item.class));

        // Save and get an empty item
        try {
            repository.save("items", new Item());
            Assert.fail("docId and type not provided, this should have thrown an IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }

        // Testing get method with an Item as parameter (instead of id and type)
        Item toFindItem = new Item("1");
        toFindItem = repository.get("items", toFindItem);

        // Delete
        Assert.assertTrue(repository.delete("items", "item", "1"));
        Assert.assertTrue(repository.delete("items", "item", "2"));
        Assert.assertTrue(repository.delete("items", "item", "4"));
        Assert.assertTrue(repository.delete("items", "item", "1"));

    }

    /**
     *
     */
    @Test
    public void testBulkSave() {
        // Twice same first item (=> create then update) plus an empty item
        final Item item1 = new Item("1", "group1", "group2", "group3");
        final Item item2 = new Item("1", "group1", "group2", "group3");
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
        int savedItemsCount = repository.saveBulk("bulktest", list);
        Assert.assertEquals(list.size(), savedItemsCount);

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
            final Item item = new Item(Integer.toString(i),
                                       Stream.generate(() -> words[(int) (Math.random() * words.length)])
                                               .limit((int) (Math.random() * 10)).collect(Collectors.toSet())
                                               .toArray(new String[0]));
            item.setName(words[(int) (Math.random() * words.length)]);
            item.setHeight((int) (Math.random() * 1000));
            item.setPrice(Math.random() * 10000.);
            items.add(item);
            if (i % 10_000 == 0) {
                final long start = System.currentTimeMillis();
                repository.saveBulk("loading", items);
                System.out.println("Loading (10 000 items): " + (System.currentTimeMillis() - start) + " ms");
                items.clear();
            }
        }
        if (items.size() > 0) {
            final long start = System.currentTimeMillis();
            repository.saveBulk("loading", items);
            System.out.println("Loading (" + items.size() + " items): " + (System.currentTimeMillis() - start) + " ms");
        }
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
        SearchKey<Item, Item> searchKey = new SearchKey<>( "item", Item.class);
        searchKey.setSearchIndex("loading");
        repository.searchAll(searchKey, h -> i.getAndIncrement(), ICriterion.all());
        System.out.println((System.currentTimeMillis() - start) + " ms");
        Assert.assertEquals(count, i.get());

        Assert.assertEquals(Long.valueOf(count), repository.count(searchKey, null));
        Assert.assertTrue(repository.count(searchKey, ICriterion.between("price", 1000, 2000)) < Long.valueOf(count));

        Assert.assertEquals(500902683.6326989, repository.sum(searchKey, ICriterion.all(), "price"), 1e7);
        Assert.assertEquals(49871257., repository.sum(searchKey, ICriterion.all(), "height"), 1e6);
    }

    @Test
    public void testEmpty() {
        String index = "toto";
        if (repository.indexExists(index)) {
            repository.deleteAll(index);
        } else {
            repository.createIndex(index);
        }

        Map<String, FacetType> facetMap = new HashMap<>();
        facetMap.put("titi", FacetType.DATE);
        facetMap.put("tutu", FacetType.NUMERIC);
        facetMap.put("tata", FacetType.STRING);
        SimpleSearchKey<IIndexable> searchKey = new SimpleSearchKey<>("toto", IIndexable.class);
        searchKey.setSearchIndex(index);
        FacetPage<IIndexable> page = (FacetPage<IIndexable>) repository
                .search(searchKey, 10, ICriterion.all(), facetMap);
        Assert.assertNotNull(page.getFacets());
        Assert.assertTrue(page.getFacets().isEmpty());
    }

    /**
     * Item class
     */
    // CHECKSTYLE:OFF
    private static class Item implements Serializable, IIndexable {

        private String id;

        private String name;

        private List<String> groups;

        private Item subItem;

        private int height;

        private double price;

        public Item() {
        }

        public Item(String id, String... groups) {
            this.id = id;
            this.groups = Lists.newArrayList(groups);
        }

        @SuppressWarnings("unused")
        public Item(String id, String name, int height, double price, String... groups) {
            this(id, groups);
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

        @Override
        public String getLabel() {
            return name;
        }

        public Item getSubItem() {
            return subItem;
        }

        @SuppressWarnings("unused")
        public void setSubItem(Item subItem) {
            this.subItem = subItem;
        }

        @SuppressWarnings("unused")
        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        @SuppressWarnings("unused")
        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        @JsonProperty("id")
        @Override
        public String getDocId() {
            return id;
        }

        @Override
        public String getType() {
            return "item";
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = (prime * result) + ((id == null) ? 0 : id.hashCode());
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
            Item other = (Item) obj;
            if (id == null) {
                if (other.id != null) {
                    return false;
                }
            } else if (!id.equals(other.id)) {
                return false;
            }
            return true;
        }

    }
    // CHECKSTYLE:ON
}
