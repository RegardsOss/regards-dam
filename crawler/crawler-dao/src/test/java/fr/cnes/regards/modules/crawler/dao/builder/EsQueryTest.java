package fr.cnes.regards.modules.crawler.dao.builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.data.domain.Page;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.cnes.regards.framework.gson.adapters.LocalDateTimeAdapter;
import fr.cnes.regards.modules.crawler.dao.EsRepository;
import fr.cnes.regards.modules.crawler.dao.FacetPage;
import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;
import fr.cnes.regards.modules.crawler.domain.facet.DateFacet;
import fr.cnes.regards.modules.crawler.domain.facet.FacetType;
import fr.cnes.regards.modules.crawler.domain.facet.IFacet;
import fr.cnes.regards.modules.crawler.domain.facet.NumericFacet;
import fr.cnes.regards.modules.crawler.domain.facet.StringFacet;

public class EsQueryTest {

    private static final String INDEX = "criterions";

    private static final String INDEX2 = "criterions2";

    private static final String TYPE1 = "type1";

    /**
     * Class to test
     */
    private static IEsRepository repository;

    /**
     * JSON mapper
     */
    private static Gson gson;

    /**
     * Befor class setting up method
     *
     * @throws Exception
     *             exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        // By now, repository try to connect localhost:9300 for ElasticSearch
        boolean repositoryOK = true;
        try {
            gson = new GsonBuilder().create();
            // FIXME valeurs en dur pour l'instant
            // repository = new EsRepository(gson, null, "172.26.47.52", 9300, "regards");
            repository = new EsRepository(gson, null, "localhost", 9300, "regards");
        } catch (NoNodeAvailableException e) {
            repositoryOK = false;
        }
        // Do not launch tests is Elasticsearch is not available
        Assume.assumeTrue(repositoryOK);

        /*
         * final Consumer<String> cleanFct = (pIndex) -> { try { repository.deleteIndex(pIndex); } catch (final
         * IndexNotFoundException infe) { } }; // All created indices from tests cleanFct.accept(INDEX);
         */
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (repository != null) {
            repository.close();
        }
    }

    private void createData() {
        try {
            repository.deleteIndex(INDEX);
        } catch (IndexNotFoundException infe) {
        }
        repository.createIndex(INDEX);
        final String[] STRINGS = { "Le", "petit", "chat", "est", "mort", "de", "sa", "belle", "mort",
                "ou écrasé on sait pas trop" };
        final String[] LOREM_IPSUM = { "Lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
                "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore", "magna", "aliqua",
                "Ut" };
        AtomicInteger ai = new AtomicInteger(1);
        final int[] INTS = IntStream.generate(() -> ai.getAndIncrement()).limit(20).toArray();
        final double[] DOUBLES = { Math.PI, Math.E, Math.sqrt(2), 1.2, 2.3, 5.e24, -0.3e12, 1.54e-12, 1.0,
                1.1234567891011121314, 0., 0., 0., 0., 0. };
        LocalDateTime date = LocalDateTime.of(2017, Month.JANUARY, 1, 12, 0);
        AtomicInteger ai2 = new AtomicInteger(0);
        final LocalDateTime[] DATES = Stream.generate(() -> date.plusDays(ai2.getAndIncrement())).limit(20)
                .collect(Collectors.toList()).toArray(new LocalDateTime[20]);
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            // size attribute from 1 to 10
            Properties att = new Properties(i + 1, (9 - i) + (Math.random() / 10.), STRINGS[i],
                    LocalDateTime.of(2017, Month.JANUARY, 1 + i, 10, 47), Arrays.copyOfRange(LOREM_IPSUM, i, i + 10),
                    Arrays.copyOfRange(INTS, i, i + 10), Arrays.copyOfRange(DOUBLES, i, i + 5),
                    Arrays.copyOfRange(DATES, i, i + 10));
            items.add(new Item(Integer.toString(i), TYPE1, att));
        }
        repository.saveBulk(INDEX, items);
    }

    private void createData2() {
        try {
            repository.deleteIndex(INDEX2);
        } catch (IndexNotFoundException infe) {
        }
        repository.createIndex(INDEX2);
        final String[] STRINGS = { "Le", "petit", "chat", "est", "mort", "de", "sa", "belle", "mort",
                "ou écrasé on sait pas trop" };
        final String[] LOREM_IPSUM = { "Lorem", "ipsum", "dolor", "sit", "amet", "consectetur", "adipiscing", "elit",
                "sed", "do", "eiusmod", "tempor", "incididunt", "ut", "labore", "et", "dolore", "magna", "aliqua",
                "Ut" };
        AtomicInteger ai = new AtomicInteger(1);
        final int[] INTS = IntStream.generate(() -> ai.getAndIncrement()).limit(20).toArray();
        final double[] DOUBLES = { Math.PI, Math.E, Math.sqrt(2), 1.2, 2.3, 5.e24, -0.3e12, 1.54e-12, 1.0,
                1.1234567891011121314, 0., 0., 0., 0., 0. };
        LocalDateTime date = LocalDateTime.of(2017, Month.JANUARY, 1, 12, 0);
        AtomicInteger ai2 = new AtomicInteger(0);
        final LocalDateTime[] DATES = Stream.generate(() -> date.plusDays(ai2.getAndIncrement())).limit(20)
                .collect(Collectors.toList()).toArray(new LocalDateTime[20]);
        List<Item> items = new ArrayList<>();
        for (int i = 0; i < 1_000_000; i++) {
            // size attribute from 1 to 10
            Properties att = new Properties(i + 1, (9 - i) + (Math.random() / 10.), STRINGS[i % 10],
                    LocalDateTime.of(2017, Month.JANUARY, 1 + (i % 10), 10, 47),
                    Arrays.copyOfRange(LOREM_IPSUM, i % 10, (i % 10) + 10),
                    Arrays.copyOfRange(INTS, i % 10, (i % 10) + 10), Arrays.copyOfRange(DOUBLES, i % 10, (i % 10) + 5),
                    Arrays.copyOfRange(DATES, i % 10, (i % 10) + 10));
            items.add(new Item(Integer.toString(i), TYPE1, att));
            if ((i % 1000) == 0) {
                long start = System.currentTimeMillis();
                repository.saveBulk(INDEX2, items);
                System.out.println(i + " : " + (System.currentTimeMillis() - start) + " ms");
                items.clear();
            }
        }
        repository.saveBulk(INDEX2, items);

    }

    @Test
    public void testSearch() {
        this.createData();

        // On integers
        ICriterion gt5crit = ICriterion.gt("properties.size", 5);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, gt5crit).getContent().size());
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, ICriterion.not(gt5crit)).getContent().size());

        ICriterion range2_4crit = ICriterion.between("properties.size", 2, 4);
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, range2_4crit).getContent().size());

        ICriterion lt1crit = ICriterion.lt("properties.size", 1);
        Assert.assertEquals(0, repository.search(INDEX, Item.class, 10, lt1crit).getContent().size());

        ICriterion inCrit = ICriterion.in("properties.size", 1, 3, 5, 7, 9);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, inCrit).getContent().size());

        ICriterion allCrit = ICriterion.ne("atributes.size", -1);
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, allCrit).getContent().size());

        // On doubles
        ICriterion allDCrit = ICriterion.between("properties.weight", 0., 10.);
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, allDCrit).getContent().size());

        ICriterion almostEqualsCrit = ICriterion.eq("properties.weight", 5, 0.1);
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, almostEqualsCrit).getContent().size());

        // On Strings
        ICriterion mortCrit = ICriterion.equals("properties.text", "mort");
        Assert.assertEquals(2, repository.search(INDEX, Item.class, 10, mortCrit).getContent().size());

        ICriterion optionaltextWithoutBlanksCrit = ICriterion.in("properties.text", "Le", "petit", "chat", "est",
                                                                 "mort", "de", "sa", "belle");
        Assert.assertEquals(9, repository.search(INDEX, Item.class, 10, optionaltextWithoutBlanksCrit).getContent()
                .size());

        ICriterion optionaltextWithBlanksCrit = ICriterion.in("properties.text", "mort", "ou écrasé on sait pas trop");
        Assert.assertEquals(3,
                            repository.search(INDEX, Item.class, 10, optionaltextWithBlanksCrit).getContent().size());
        ICriterion startsWithCrit = ICriterion.startsWith("properties.text", "ou é");
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, startsWithCrit).getContent().size());

        ICriterion endsWithCrit = ICriterion.endsWith("properties.text", "t");
        // Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, endsWithCrit).getContent().size());
        // FIXME : By now, search regexp is applied on each word instead of whole phrase
        Assert.assertEquals(6, repository.search(INDEX, Item.class, 10, endsWithCrit).getContent().size());

        // On Dates
        ICriterion gtDateCriterion = ICriterion.gt("properties.date",
                                                   LocalDateTime.of(2017, Month.JANUARY, 1, 10, 47, 0));
        Assert.assertEquals(9, repository.search(INDEX, Item.class, 10, gtDateCriterion).getContent().size());
        ICriterion geDateCriterion = ICriterion.ge("properties.date",
                                                   LocalDateTime.of(2017, Month.JANUARY, 1, 10, 47, 0));
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, geDateCriterion).getContent().size());

        ICriterion betweenDateCriterion = ICriterion.between("properties.date",
                                                             LocalDateTime.of(2017, Month.JANUARY, 2, 10, 47, 0),
                                                             LocalDateTime.of(2017, Month.JANUARY, 4, 10, 47, 0));
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, betweenDateCriterion).getContent().size());

        // On strings array
        ICriterion containsStringCrit = ICriterion.contains("properties.tags", "dolor");
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, containsStringCrit).getContent().size());
        // On int array
        ICriterion containsIntCrit = ICriterion.contains("properties.ints", 3);
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, containsIntCrit).getContent().size());
        // On double array
        ICriterion containsDoubleCrit1 = ICriterion.contains("properties.doubles", 3.1416, 1.e-4);
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, containsDoubleCrit1).getContent().size());
        ICriterion containsDoubleCrit2 = ICriterion.contains("properties.doubles", 1.12345678910, 1.e-11);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, containsDoubleCrit2).getContent().size());
        // On date array
        ICriterion containsDateCrit = ICriterion
                .containsDateBetween("properties.dates", LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0),
                                     LocalDateTime.of(2017, Month.JANUARY, 1, 23, 59, 59, 999));
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, containsDateCrit).getContent().size());
        ICriterion containsDateCrit2 = ICriterion
                .containsDateBetween("properties.dates", LocalDateTime.of(2017, Month.JANUARY, 2, 0, 0),
                                     LocalDateTime.of(2017, Month.JANUARY, 3, 23, 59, 59, 999));
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, containsDateCrit2).getContent().size());

        // On int ranges
        ICriterion intoIntsCrit1 = ICriterion.into("properties.intRange", 10);
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, intoIntsCrit1).getContent().size());
        ICriterion intoIntsCrit2 = ICriterion.into("properties.intRange", -1);
        Assert.assertEquals(0, repository.search(INDEX, Item.class, 10, intoIntsCrit2).getContent().size());

        // On double ranges
        ICriterion intoDoublesCrit1 = ICriterion.into("properties.doubleRange", Math.PI);
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, intoDoublesCrit1).getContent().size());
        ICriterion intoDoublesCrit2 = ICriterion.into("properties.doubleRange", -4e12);
        Assert.assertEquals(0, repository.search(INDEX, Item.class, 10, intoDoublesCrit2).getContent().size());

        // On date ranges
        ICriterion interDatesCrit1 = ICriterion.intersects("properties.dateRange",
                                                           LocalDateTime.of(2016, Month.JANUARY, 4, 12, 0, 0),
                                                           LocalDateTime.of(2018, Month.JANUARY, 4, 12, 0, 0));
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, interDatesCrit1).getContent().size());
        ICriterion interDatesCrit2 = ICriterion.intersects("properties.dateRange",
                                                           LocalDateTime.of(2016, Month.JANUARY, 4, 12, 0, 0),
                                                           LocalDateTime.of(2017, Month.JANUARY, 1, 12, 0, 0));
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, interDatesCrit2).getContent().size());
        ICriterion interDatesCrit3 = ICriterion.intersects("properties.dateRange",
                                                           LocalDateTime.of(2017, Month.JANUARY, 19, 12, 0, 0),
                                                           LocalDateTime.of(2018, Month.JANUARY, 1, 12, 0, 0));
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, interDatesCrit3).getContent().size());

        ICriterion interDatesCrit4 = ICriterion.intersects("properties.dateRange",
                                                           LocalDateTime.of(2017, Month.JANUARY, 2, 12, 0, 0),
                                                           LocalDateTime.of(2017, Month.JANUARY, 18, 12, 0, 0));
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, interDatesCrit4).getContent().size());

        // On boolean
        ICriterion booleanCrit = ICriterion.eq("properties.bool", true);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, booleanCrit).getContent().size());

        // Test for multiFieldsSearch, while data have been created into Elasticsearch...
        Assert.assertEquals(1, repository.multiFieldsSearch(INDEX, Item.class, 10, 1, "properties.ints").getContent()
                .size());
        Assert.assertEquals(1, repository
                .multiFieldsSearch(INDEX, Item.class, 10, "Lorem", "properties.text", "properties.tags").getContent()
                .size());
        Assert.assertEquals(2,
                            repository.multiFieldsSearch(INDEX, Item.class, 10,
                                                         LocalDateTime.of(2017, Month.JANUARY, 10, 12, 0),
                                                         "properties.dateRange.*")
                                    .getContent().size());
        Assert.assertEquals(10, repository
                .multiFieldsSearch(INDEX, Item.class, 10, LocalDateTime.of(2017, Month.JANUARY, 10, 12, 0),
                                   "properties.dateRange.*", "properties.dates")
                .getContent().size());
        Assert.assertEquals(1, repository.multiFieldsSearch(INDEX, Item.class, 10, Math.PI, "properties.double*")
                .getContent().size());
        Assert.assertEquals(5, repository.multiFieldsSearch(INDEX, Item.class, 10, true, "properties.bool").getContent()
                .size());

        // No criterion
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, ICriterion.all()).getContent().size());
    }

    @Test
    public void testSearchWithFacets() {
        this.createData();

        // Search with aggregations
        ImmutableMap.Builder<String, FacetType> facetMapBuilder = new ImmutableMap.Builder<>();
        Page<Item> page = repository.search(INDEX, Item.class, 10, ICriterion.all(),
                                            facetMapBuilder.put("properties.tags", FacetType.STRING).build());
        Assert.assertEquals(10, page.getContent().size());
        Assert.assertTrue(page instanceof FacetPage);
        Map<String, IFacet<?>> facetMap = ((FacetPage<Item>) page).getFacetMap();
        Assert.assertTrue(facetMap.containsKey("properties.tags"));
        Assert.assertTrue(facetMap.get("properties.tags") instanceof StringFacet);
        StringFacet strFacet = (StringFacet) facetMap.get("properties.tags");
        Assert.assertNotNull(strFacet);

        FacetPage<Item> facetPage = (FacetPage<Item>) repository
                .search(INDEX, Item.class, 10, ICriterion.all(),
                        facetMapBuilder.put("properties.ints", FacetType.NUMERIC).build());
        Assert.assertEquals(10, facetPage.getContent().size());
        facetMap = facetPage.getFacetMap();
        Assert.assertTrue(facetMap.containsKey("properties.ints"));
        Assert.assertTrue(facetMap.get("properties.ints") instanceof NumericFacet);
        NumericFacet numFacet = (NumericFacet) facetMap.get("properties.ints");
        Assert.assertNotNull(numFacet);

        facetPage = (FacetPage<Item>) repository
                .search(INDEX, Item.class, 10, ICriterion.all(),
                        facetMapBuilder.put("properties.dates", FacetType.DATE).build());
        Assert.assertEquals(10, facetPage.getContent().size());
        facetMap = facetPage.getFacetMap();
        Assert.assertTrue(facetMap.containsKey("properties.dates"));
        Assert.assertTrue(facetMap.get("properties.dates") instanceof DateFacet);
        DateFacet dateFacet = (DateFacet) facetMap.get("properties.dates");
        Assert.assertNotNull(dateFacet);

        // With criterions
        ICriterion interDatesCrit4 = ICriterion.intersects("properties.dateRange",
                                                           LocalDateTime.of(2017, Month.JANUARY, 2, 12, 0, 0),
                                                           LocalDateTime.of(2017, Month.JANUARY, 18, 12, 0, 0));
        Map<String, FacetType> facetReqMap = new ImmutableMap.Builder<String, FacetType>()
                .put("properties.tags", FacetType.STRING).put("properties.ints", FacetType.NUMERIC)
                .put("properties.dates", FacetType.DATE).build();
        Assert.assertEquals(10, page.getContent().size());
        page = repository.search(INDEX, Item.class, 10, interDatesCrit4, facetReqMap);
        Assert.assertTrue(page instanceof FacetPage);
        facetMap = ((FacetPage<Item>) page).getFacetMap();
        Assert.assertTrue(facetMap.containsKey("properties.tags"));
        Assert.assertTrue(facetMap.get("properties.tags") instanceof StringFacet);
        strFacet = (StringFacet) facetMap.get("properties.tags");
        Assert.assertNotNull(strFacet);
        Assert.assertTrue(facetMap.containsKey("properties.ints"));
        Assert.assertTrue(facetMap.get("properties.ints") instanceof NumericFacet);
        numFacet = (NumericFacet) facetMap.get("properties.ints");
        Assert.assertNotNull(numFacet);
        Assert.assertTrue(facetMap.containsKey("properties.dates"));
        Assert.assertTrue(facetMap.get("properties.dates") instanceof DateFacet);
        dateFacet = (DateFacet) facetMap.get("properties.dates");
        Assert.assertNotNull(dateFacet);
    }

    @Test
    public void testWithSort() {
        this.createData();

        LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<>();
        sortMap.put("properties.text", true);
        sortMap.put("properties.size", false);
        List<Item> items = repository.search(INDEX, Item.class, 10, sortMap, ICriterion.all()).getContent();
        List<Item> itemsSorted = Lists.newArrayList(items);
        Comparator<Item> comparator = Comparator.comparing(item -> item.getProperties().getText());
        comparator = comparator
                .thenComparing(Comparator.<Item, Integer> comparing(item -> item.getProperties().getSize()).reversed());
        itemsSorted.sort(comparator);
        Assert.assertEquals(items, itemsSorted);
    }

    /**
     * This test creates 1_000_000 entities so it is to be used only once to test perf.
     * Some search queries are done then
     */
    @Ignore
    @Test
    public void testLoad() {
        // Remove this comment to create 1_000_000 entities into ES if not already present
        // this.createData2();
        // Search with aggregations
        ImmutableMap.Builder<String, FacetType> facetMapBuilder = new ImmutableMap.Builder<>();
        facetMapBuilder.put("properties.size", FacetType.NUMERIC).put("properties.weight", FacetType.NUMERIC)
                .put("properties.text", FacetType.STRING)
                // .put("properties.date", FacetType.DATE);
                .put("properties.tags", FacetType.STRING);
        // .put("properties.ints", FacetType.NUMERIC);
        // .put("properties.doubles", FacetType.NUMERIC).put("properties.dates", FacetType.DATE);
        LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<>();
        // sortMap.put("docId", false);
        long start = System.currentTimeMillis();
        Page<Item> page = repository.search(INDEX2, Item.class, 100, ICriterion.all(), facetMapBuilder.build(),
                                            sortMap);
        System.out.println("search : " + (System.currentTimeMillis() - start) + " ms");
        // while (page.hasNext()) {
        // start = System.currentTimeMillis();
        // page = repository.search(INDEX2, Item.class, page.nextPageable(), ICriterion.all(), facetMapBuilder.build(),
        // sortMap);
        // System.out.println("recherche : " + (System.currentTimeMillis() - start) + " ms");
        // }
        // sortMap.clear();
        // sortMap.put("properties.date", Boolean.FALSE);
        // long start = System.currentTimeMillis();
        // Page<Item> page = repository.search(INDEX2, Item.class, 100, sortMap, ICriterion.all());
        // System.out.println("recherche : " + (System.currentTimeMillis() - start) + " ms");

        // long start = System.currentTimeMillis();
        // repository.get(INDEX2, TYPE1, "229009", Item.class);
        // System.out.println("get : " + (System.currentTimeMillis() - start) + " ms");
    }

    /**
     * Test updating large entity data update (1_000_000 here)
     */
    @Test
    @Ignore
    public void testUpdatePerf() {
        // Remove this comment to create 1_000_000 entities into ES if not already present
        // this.createData2();
        // Search all items and set tags for all of them

        String[] tags = new String[] { "URN:AIP:CRITERIONS2:etc..." };

        List<Item> items = new ArrayList<>();
        Consumer<Item> updater = item -> {
            // Updating item
            item.getProperties().setTags(tags);
            items.add(item);
            if (items.size() == 10000) {
                long start = System.currentTimeMillis();
                repository.saveBulk(INDEX2, items);
                System.out.println("Update 10000 : " + (System.currentTimeMillis() - start) + " ms");
                items.clear();
            }
        };

        long start = System.currentTimeMillis();
        repository.searchAll(INDEX2, Item.class, updater, ICriterion.all());
        if (!items.isEmpty()) {
            long startS = System.currentTimeMillis();
            repository.saveBulk(INDEX, items);
            System.out.println("Update " + items.size() + " : " + (System.currentTimeMillis() - startS) + " ms");
        }
        System.out.println("Update : " + (System.currentTimeMillis() - start) + " ms");
    }

    private void nothing(Item i) {

    }

    /**
     * Test crossing 1_000_000 entities
     */
    @Test
    @Ignore
    public void testCrossingPerf() {
        // Remove this comment to create 1_000_000 entities into ES if not already present
        // this.createData2();
        // Search all items and set tags for all of them

        long start = System.currentTimeMillis();
        repository.searchAll(INDEX2, Item.class, this::nothing, ICriterion.all());
        System.out.println("Crossing 1 000 000 entities : " + (System.currentTimeMillis() - start) + " ms");
    }

    private static class Item implements IIndexable, Serializable {

        private String docId;

        private String type;

        private Properties properties;

        private Item() {
        }

        public Item(String pId, String pType, Properties pProperties) {
            super();
            docId = pId;
            type = pType;
            properties = pProperties;
        }

        @Override
        public String getDocId() {
            return docId;
        }

        @Override
        public String getType() {
            return type;
        }

        public void setDocId(String pId) {
            docId = pId;
        }

        public Properties getProperties() {
            return properties;
        }

        public void setProperties(Properties pProperties) {
            properties = pProperties;
        }

        public void setType(String pType) {
            type = pType;
        }

    }

    private static class Range<T> {

        public T lowerBound;

        public T upperBound;
    }

    private static class Properties implements Serializable {

        private int size;

        private double weight;

        private String text;

        private String date;

        private String[] tags;

        private int[] ints;

        private double[] doubles;

        private String[] dates;

        private final Range<String> dateRange;

        private final Range<Integer> intRange;

        private final Range<Double> doubleRange;

        private boolean bool;

        public Properties(int pSize, double pWeight, String pText, LocalDateTime pDate, String[] pTags, int[] pInts,
                double[] pDoubles, LocalDateTime[] pDates) {
            super();
            size = pSize;
            weight = pWeight;
            text = pText;
            date = LocalDateTimeAdapter.format(pDate);
            tags = pTags;
            ints = pInts;
            doubles = pDoubles;
            dates = Arrays.stream(pDates).map(d -> LocalDateTimeAdapter.format(d)).collect(Collectors.toList())
                    .toArray(new String[pDates.length]);
            dateRange = new Range<>();
            dateRange.lowerBound = dates[0];
            dateRange.upperBound = dates[dates.length - 1];
            intRange = new Range<>();
            intRange.lowerBound = ints[0];
            intRange.upperBound = ints[ints.length - 1];
            doubleRange = new Range<>();
            doubleRange.lowerBound = Math.min(doubles[0], doubles[doubles.length - 1]);
            doubleRange.upperBound = Math.max(doubles[0], doubles[doubles.length - 1]);
            bool = ((size % 2) == 0);
        }

        public int getSize() {
            return size;
        }

        public void setSize(int pSize) {
            size = pSize;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double pWeight) {
            weight = pWeight;
        }

        public String getText() {
            return text;
        }

        public void setText(String pText) {
            text = pText;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String pDate) {
            date = pDate;
        }

        public String[] getTags() {
            return tags;
        }

        public void setTags(String[] pTags) {
            tags = pTags;
        }

        public int[] getInts() {
            return ints;
        }

        public void setInts(int[] pInts) {
            ints = pInts;
        }

        public double[] getDoubles() {
            return doubles;
        }

        public void setDoubles(double[] pDoubles) {
            doubles = pDoubles;
        }

        public String[] getDates() {
            return dates;
        }

        public void setDates(String[] pDates) {
            dates = pDates;
        }

        public boolean isBool() {
            return bool;
        }

        public void setBool(boolean pBool) {
            bool = pBool;
        }

    }
}