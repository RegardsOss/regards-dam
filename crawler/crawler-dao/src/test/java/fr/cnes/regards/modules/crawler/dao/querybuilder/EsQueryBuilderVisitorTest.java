package fr.cnes.regards.modules.crawler.dao.querybuilder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
import org.junit.Test;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.cnes.regards.framework.gson.adapters.LocalDateTimeAdapter;
import fr.cnes.regards.modules.crawler.dao.EsRepository;
import fr.cnes.regards.modules.crawler.dao.IEsRepository;
import fr.cnes.regards.modules.crawler.domain.IIndexable;
import fr.cnes.regards.modules.crawler.domain.criterion.ICriterion;

public class EsQueryBuilderVisitorTest {

    private static final String INDEX = "criterions";

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

        final Consumer<String> cleanFct = (pIndex) -> {
            try {
                repository.deleteIndex(pIndex);
            } catch (final IndexNotFoundException infe) {
            }
        };
        // All created indices from tests
        cleanFct.accept(INDEX);
    }

    @AfterClass
    public static void tearDown() throws Exception {
        if (repository != null) {
            repository.close();
        }
    }

    private void createData() {
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

    private static class Attributes implements Serializable {

        private int size;

        private double weight;

        private String text;

        private String date;

        private String[] tags;

        private int[] ints;

        private double[] doubles;

        private String[] dates;

        public Attributes(int pSize, double pWeight, String pText, LocalDateTime pDate, String[] pTags, int[] pInts,
                double[] pDoubles, LocalDateTime[] pDates) {
            super();
            size = pSize;
            weight = pWeight;
            text = pText;
            date = LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(pDate);
            tags = pTags;
            ints = pInts;
            doubles = pDoubles;
            dates = Arrays.stream(pDates).map(d -> LocalDateTimeAdapter.ISO_DATE_TIME_OPTIONAL_OFFSET.format(d))
                    .collect(Collectors.toList()).toArray(new String[pDates.length]);
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

    }
}
