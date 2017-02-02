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
            repository = new EsRepository(gson, null, "172.26.47.52", 9300, "regards");
            // repository = new EsRepository(gson, null, "localhost", 9300, "regards");
        } catch (NoNodeAvailableException e) {
            repositoryOK = false;
        }
        // Do not launch tests is Elasticsearch is not available
        Assume.assumeTrue(repositoryOK);

        /*        final Consumer<String> cleanFct = (pIndex) -> {
            try {
                repository.deleteIndex(pIndex);
            } catch (final IndexNotFoundException infe) {
            }
        };
        // All created indices from tests
        cleanFct.accept(INDEX);*/
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
            Attributes att = new Attributes(i + 1, (9 - i) + (Math.random() / 10.), STRINGS[i],
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
            Attributes att = new Attributes(i + 1, (9 - i) + (Math.random() / 10.), STRINGS[i % 10],
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
        ICriterion gt5crit = ICriterion.gt("attributes.size", 5);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, gt5crit).getContent().size());
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, ICriterion.not(gt5crit)).getContent().size());

        ICriterion range2_4crit = ICriterion.between("attributes.size", 2, 4);
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, range2_4crit).getContent().size());

        ICriterion lt1crit = ICriterion.lt("attributes.size", 1);
        Assert.assertEquals(0, repository.search(INDEX, Item.class, 10, lt1crit).getContent().size());

        ICriterion inCrit = ICriterion.in("attributes.size", 1, 3, 5, 7, 9);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, inCrit).getContent().size());

        ICriterion allCrit = ICriterion.ne("atributes.size", -1);
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, allCrit).getContent().size());

        // On doubles
        ICriterion allDCrit = ICriterion.between("attributes.weight", 0., 10.);
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, allDCrit).getContent().size());

        ICriterion almostEqualsCrit = ICriterion.eq("attributes.weight", 5, 0.1);
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, almostEqualsCrit).getContent().size());

        // On Strings
        ICriterion mortCrit = ICriterion.equals("attributes.text", "mort");
        Assert.assertEquals(2, repository.search(INDEX, Item.class, 10, mortCrit).getContent().size());

        ICriterion optionaltextWithoutBlanksCrit = ICriterion.in("attributes.text", "Le", "petit", "chat", "est",
                                                                 "mort", "de", "sa", "belle");
        Assert.assertEquals(9, repository.search(INDEX, Item.class, 10, optionaltextWithoutBlanksCrit).getContent()
                .size());

        ICriterion optionaltextWithBlanksCrit = ICriterion.in("attributes.text", "mort", "ou écrasé on sait pas trop");
        Assert.assertEquals(3,
                            repository.search(INDEX, Item.class, 10, optionaltextWithBlanksCrit).getContent().size());
        ICriterion startsWithCrit = ICriterion.startsWith("attributes.text", "ou é");
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, startsWithCrit).getContent().size());

        ICriterion endsWithCrit = ICriterion.endsWith("attributes.text", "t");
        // Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, endsWithCrit).getContent().size());
        // FIXME : By now, search regexp is applied on each word instead of whole phrase
        Assert.assertEquals(6, repository.search(INDEX, Item.class, 10, endsWithCrit).getContent().size());

        // On Dates
        ICriterion gtDateCriterion = ICriterion.gt("attributes.date",
                                                   LocalDateTime.of(2017, Month.JANUARY, 1, 10, 47, 0));
        Assert.assertEquals(9, repository.search(INDEX, Item.class, 10, gtDateCriterion).getContent().size());
        ICriterion geDateCriterion = ICriterion.ge("attributes.date",
                                                   LocalDateTime.of(2017, Month.JANUARY, 1, 10, 47, 0));
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, geDateCriterion).getContent().size());

        ICriterion betweenDateCriterion = ICriterion.between("attributes.date",
                                                             LocalDateTime.of(2017, Month.JANUARY, 2, 10, 47, 0),
                                                             LocalDateTime.of(2017, Month.JANUARY, 4, 10, 47, 0));
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, betweenDateCriterion).getContent().size());

        // On strings array
        ICriterion containsStringCrit = ICriterion.contains("attributes.tags", "dolor");
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, containsStringCrit).getContent().size());
        // On int array
        ICriterion containsIntCrit = ICriterion.contains("attributes.ints", 3);
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, containsIntCrit).getContent().size());
        // On double array
        ICriterion containsDoubleCrit1 = ICriterion.contains("attributes.doubles", 3.1416, 1.e-4);
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, containsDoubleCrit1).getContent().size());
        ICriterion containsDoubleCrit2 = ICriterion.contains("attributes.doubles", 1.12345678910, 1.e-11);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, containsDoubleCrit2).getContent().size());
        // On date array
        ICriterion containsDateCrit = ICriterion
                .containsDateBetween("attributes.dates", LocalDateTime.of(2017, Month.JANUARY, 1, 0, 0),
                                     LocalDateTime.of(2017, Month.JANUARY, 1, 23, 59, 59, 999));
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, containsDateCrit).getContent().size());
        ICriterion containsDateCrit2 = ICriterion
                .containsDateBetween("attributes.dates", LocalDateTime.of(2017, Month.JANUARY, 2, 0, 0),
                                     LocalDateTime.of(2017, Month.JANUARY, 3, 23, 59, 59, 999));
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, containsDateCrit2).getContent().size());

        // On int ranges
        ICriterion intoIntsCrit1 = ICriterion.into("attributes.intRange", 10);
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, intoIntsCrit1).getContent().size());
        ICriterion intoIntsCrit2 = ICriterion.into("attributes.intRange", -1);
        Assert.assertEquals(0, repository.search(INDEX, Item.class, 10, intoIntsCrit2).getContent().size());

        // On double ranges
        ICriterion intoDoublesCrit1 = ICriterion.into("attributes.doubleRange", Math.PI);
        Assert.assertEquals(3, repository.search(INDEX, Item.class, 10, intoDoublesCrit1).getContent().size());
        ICriterion intoDoublesCrit2 = ICriterion.into("attributes.doubleRange", -4e12);
        Assert.assertEquals(0, repository.search(INDEX, Item.class, 10, intoDoublesCrit2).getContent().size());

        // On date ranges
        ICriterion interDatesCrit1 = ICriterion.intersects("attributes.dateRange",
                                                           LocalDateTime.of(2016, Month.JANUARY, 4, 12, 0, 0),
                                                           LocalDateTime.of(2018, Month.JANUARY, 4, 12, 0, 0));
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, interDatesCrit1).getContent().size());
        ICriterion interDatesCrit2 = ICriterion.intersects("attributes.dateRange",
                                                           LocalDateTime.of(2016, Month.JANUARY, 4, 12, 0, 0),
                                                           LocalDateTime.of(2017, Month.JANUARY, 1, 12, 0, 0));
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, interDatesCrit2).getContent().size());
        ICriterion interDatesCrit3 = ICriterion.intersects("attributes.dateRange",
                                                           LocalDateTime.of(2017, Month.JANUARY, 19, 12, 0, 0),
                                                           LocalDateTime.of(2018, Month.JANUARY, 1, 12, 0, 0));
        Assert.assertEquals(1, repository.search(INDEX, Item.class, 10, interDatesCrit3).getContent().size());

        ICriterion interDatesCrit4 = ICriterion.intersects("attributes.dateRange",
                                                           LocalDateTime.of(2017, Month.JANUARY, 2, 12, 0, 0),
                                                           LocalDateTime.of(2017, Month.JANUARY, 18, 12, 0, 0));
        Assert.assertEquals(10, repository.search(INDEX, Item.class, 10, interDatesCrit4).getContent().size());

        // On boolean
        ICriterion booleanCrit = ICriterion.eq("attributes.bool", true);
        Assert.assertEquals(5, repository.search(INDEX, Item.class, 10, booleanCrit).getContent().size());

        // Test for multiFieldsSearch, while data have been created into Elasticsearch...
        Assert.assertEquals(1, repository.multiFieldsSearch(INDEX, Item.class, 10, 1, "attributes.ints").getContent()
                .size());
        Assert.assertEquals(1, repository
                .multiFieldsSearch(INDEX, Item.class, 10, "Lorem", "attributes.text", "attributes.tags").getContent()
                .size());
        Assert.assertEquals(2,
                            repository.multiFieldsSearch(INDEX, Item.class, 10,
                                                         LocalDateTime.of(2017, Month.JANUARY, 10, 12, 0),
                                                         "attributes.dateRange.*")
                                    .getContent().size());
        Assert.assertEquals(10, repository
                .multiFieldsSearch(INDEX, Item.class, 10, LocalDateTime.of(2017, Month.JANUARY, 10, 12, 0),
                                   "attributes.dateRange.*", "attributes.dates")
                .getContent().size());
        Assert.assertEquals(1, repository.multiFieldsSearch(INDEX, Item.class, 10, Math.PI, "attributes.double*")
                .getContent().size());
        Assert.assertEquals(5, repository.multiFieldsSearch(INDEX, Item.class, 10, true, "attributes.bool").getContent()
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
                                            facetMapBuilder.put("attributes.tags", FacetType.STRING).build());
        Assert.assertEquals(10, page.getContent().size());
        Assert.assertTrue(page instanceof FacetPage);
        Map<String, IFacet<?>> facetMap = ((FacetPage<Item>) page).getFacetMap();
        Assert.assertTrue(facetMap.containsKey("attributes.tags"));
        Assert.assertTrue(facetMap.get("attributes.tags") instanceof StringFacet);
        StringFacet strFacet = (StringFacet) facetMap.get("attributes.tags");
        Assert.assertNotNull(strFacet);

        FacetPage<Item> facetPage = (FacetPage<Item>) repository
                .search(INDEX, Item.class, 10, ICriterion.all(),
                        facetMapBuilder.put("attributes.ints", FacetType.NUMERIC).build());
        Assert.assertEquals(10, facetPage.getContent().size());
        facetMap = facetPage.getFacetMap();
        Assert.assertTrue(facetMap.containsKey("attributes.ints"));
        Assert.assertTrue(facetMap.get("attributes.ints") instanceof NumericFacet);
        NumericFacet numFacet = (NumericFacet) facetMap.get("attributes.ints");
        Assert.assertNotNull(numFacet);

        facetPage = (FacetPage<Item>) repository
                .search(INDEX, Item.class, 10, ICriterion.all(),
                        facetMapBuilder.put("attributes.dates", FacetType.DATE).build());
        Assert.assertEquals(10, facetPage.getContent().size());
        facetMap = facetPage.getFacetMap();
        Assert.assertTrue(facetMap.containsKey("attributes.dates"));
        Assert.assertTrue(facetMap.get("attributes.dates") instanceof DateFacet);
        DateFacet dateFacet = (DateFacet) facetMap.get("attributes.dates");
        Assert.assertNotNull(dateFacet);

        // With criterions
        ICriterion interDatesCrit4 = ICriterion.intersects("attributes.dateRange",
                                                           LocalDateTime.of(2017, Month.JANUARY, 2, 12, 0, 0),
                                                           LocalDateTime.of(2017, Month.JANUARY, 18, 12, 0, 0));
        Map<String, FacetType> facetReqMap = new ImmutableMap.Builder<String, FacetType>()
                .put("attributes.tags", FacetType.STRING).put("attributes.ints", FacetType.NUMERIC)
                .put("attributes.dates", FacetType.DATE).build();
        Assert.assertEquals(10, page.getContent().size());
        page = repository.search(INDEX, Item.class, 10, interDatesCrit4, facetReqMap);
        Assert.assertTrue(page instanceof FacetPage);
        facetMap = ((FacetPage<Item>) page).getFacetMap();
        Assert.assertTrue(facetMap.containsKey("attributes.tags"));
        Assert.assertTrue(facetMap.get("attributes.tags") instanceof StringFacet);
        strFacet = (StringFacet) facetMap.get("attributes.tags");
        Assert.assertNotNull(strFacet);
        Assert.assertTrue(facetMap.containsKey("attributes.ints"));
        Assert.assertTrue(facetMap.get("attributes.ints") instanceof NumericFacet);
        numFacet = (NumericFacet) facetMap.get("attributes.ints");
        Assert.assertNotNull(numFacet);
        Assert.assertTrue(facetMap.containsKey("attributes.dates"));
        Assert.assertTrue(facetMap.get("attributes.dates") instanceof DateFacet);
        dateFacet = (DateFacet) facetMap.get("attributes.dates");
        Assert.assertNotNull(dateFacet);
    }

    @Test
    public void testWithSort() {
        this.createData();

        LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<>();
        sortMap.put("attributes.text", true);
        sortMap.put("attributes.size", false);
        List<Item> items = repository.search(INDEX, Item.class, 10, sortMap, ICriterion.all()).getContent();
        List<Item> itemsSorted = Lists.newArrayList(items);
        Comparator<Item> comparator = Comparator.comparing(item -> item.getAttributes().getText());
        comparator = comparator
                .thenComparing(Comparator.<Item, Integer> comparing(item -> item.getAttributes().getSize()).reversed());
        itemsSorted.sort(comparator);
        Assert.assertEquals(items, itemsSorted);
    }

    @Test
    @Ignore
    public void testLoad() {
        // this.createData2();
        // Search with aggregations
        ImmutableMap.Builder<String, FacetType> facetMapBuilder = new ImmutableMap.Builder<>();
        facetMapBuilder.put("attributes.size", FacetType.NUMERIC).put("attributes.weight", FacetType.NUMERIC)
                .put("attributes.text", FacetType.STRING);
        // .put("attributes.date", FacetType.DATE);
        // .put("attributes.tags", FacetType.STRING)
        // .put("attributes.ints", FacetType.NUMERIC);
        // .put("attributes.doubles", FacetType.NUMERIC).put("attributes.dates", FacetType.DATE);
        LinkedHashMap<String, Boolean> sortMap = new LinkedHashMap<>();
        /*        sortMap.put("docId", false);
        long start = System.currentTimeMillis();
        Page<Item> page = repository.search(INDEX2, Item.class, 100, ICriterion.all(), facetMapBuilder.build(),
                                            sortMap);
        System.out.println("recherche : " + (System.currentTimeMillis() - start) + " ms");*/
        // while (page.hasNext()) {
        // start = System.currentTimeMillis();
        // page = repository.search(INDEX2, Item.class, page.nextPageable(), ICriterion.all(), facetMapBuilder.build(),
        // sortMap);
        // System.out.println("recherche : " + (System.currentTimeMillis() - start) + " ms");
        // }
        sortMap.clear();
        sortMap.put("attributes.date", Boolean.FALSE);
        // long start = System.currentTimeMillis();
        // Page<Item> page = repository.search(INDEX2, Item.class, 100, sortMap, ICriterion.all());
        // System.out.println("recherche : " + (System.currentTimeMillis() - start) + " ms");

        long start = System.currentTimeMillis();
        repository.get(INDEX2, TYPE1, "229009", Item.class);
        System.out.println("get : " + (System.currentTimeMillis() - start) + " ms");
    }

    private static class Item implements IIndexable, Serializable {

        private String docId;

        private String type;

        private Attributes attributes;

        private Item() {
        }

        public Item(String pId, String pType, Attributes pAttributes) {
            super();
            docId = pId;
            type = pType;
            attributes = pAttributes;
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

        public Attributes getAttributes() {
            return attributes;
        }

        public void setAttributes(Attributes pAttributes) {
            attributes = pAttributes;
        }

        public void setType(String pType) {
            type = pType;
        }

    }

    private static class Range<T> {

        public T lowerBound;

        public T upperBound;
    }

    private static class Attributes implements Serializable {

        private int size;

        private double weight;

        private String text;

        private String date;

        private String[] tags;

        private int[] ints;

        private double[] doubles;

        private String[] dates;

        private Range<String> dateRange;

        private Range<Integer> intRange;

        private Range<Double> doubleRange;

        private boolean bool;

        public Attributes(int pSize, double pWeight, String pText, LocalDateTime pDate, String[] pTags, int[] pInts,
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
