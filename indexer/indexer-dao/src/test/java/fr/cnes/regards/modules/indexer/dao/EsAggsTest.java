package fr.cnes.regards.modules.indexer.dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.cnes.regards.framework.gson.adapters.MultimapAdapter;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor;
import fr.cnes.regards.modules.indexer.domain.IDocFiles;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.summary.DocFilesSummary;

/**
 * Test on complex aggs
 * @author oroussel
 */
public class EsAggsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(EsAggsTest.class);

    /**
     * Class to test
     */
    private static IEsRepository repository;

    /**
     * JSON mapper
     */
    private static Gson gson;

    private static final String INDEX = "aggstest";

    private static final String TYPE = "DATA";

    /**
     * Before class setting up method
     * @throws Exception exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        Map<String, String> propMap = Maps.newHashMap();
        boolean repositoryOK = true;
        // we get the properties into target/test-classes because this is where maven will put the filtered file(with
        // real values and not placeholder)
        Stream<String> props = Files.lines(Paths.get("target/test-classes/test.properties"));
        props.filter(line -> !(line.startsWith("#") || line.trim().isEmpty())).forEach(line -> {
            String[] keyVal = line.split("=");
            propMap.put(keyVal[0], keyVal[1]);
        });
        try {
            gson = new GsonBuilder().registerTypeAdapter(Multimap.class, new MultimapAdapter()).create();
            repository = new EsRepository(gson, null, propMap.get("regards.elasticsearch.address"),
                    Integer.parseInt(propMap.get("regards.elasticsearch.http.port")),
                    new AggregationBuilderFacetTypeVisitor(10, 1));
        } catch (NoNodeAvailableException e) {
            LOGGER.error("NO NODE AVAILABLE");
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
    }

    private static final String[] TAGS = new String[] { "RIRI", "FIFI", "LOULOU", "MICHOU", "JOJO" };

    private static final Random random = new Random();

    private static Set<String> randomTags() {
        Set<String> randomSet = new HashSet<>();
        int size = random.nextInt(TAGS.length) + 1;
        if (size == TAGS.length) {
            return Sets.newHashSet(TAGS);
        }
        for (int i = 0; i < size; i++) {
            while (!randomSet.add(TAGS[random.nextInt(TAGS.length)])) {
                ;
            }
        }
        return randomSet;
    }

    private void createData() {
        if (repository.indexExists(INDEX)) {
            repository.deleteAll(INDEX);
        } else {
            repository.createIndex(INDEX);
        }

        Set<Data> datas = new HashSet<>();
        File rootDir = Paths.get("src/test/resources/testdir").toFile();
        for (File file : rootDir.listFiles()) {
            System.out.println(file.getName() + ", " + file.length());
            Data data = new Data();
            data.setDocId(file.getName());
            data.setTags(randomTags());
            data.getFiles().put(DataType.RAWDATA, new DataFile(file, DataType.RAWDATA));
            data.getFiles().put(DataType.QUICKLOOK_HD, new DataFile(file, DataType.QUICKLOOK_HD));
            datas.add(data);
        }
        repository.saveBulk(INDEX, datas);
    }

    @Test
    public void test() {
        createData();
        DocFilesSummary summary = new DocFilesSummary();
        SimpleSearchKey<Data> searchKey = new SimpleSearchKey<>(TYPE, Data.class);
        searchKey.setSearchIndex(INDEX);
        repository.computeInternalDataFilesSummary(searchKey, null, "tags", summary, "RAWDATA", "QUICKLOOK_HD");
        System.out.println(summary);
        Assert.assertEquals(12, summary.getDocumentsCount());
        // 36 because 24 RAWDATA (each RAWDATA is doubled with same name and "2" at the end) and 12 QUICKLOOKS
        Assert.assertEquals(36, summary.getFilesCount());
        Assert.assertEquals(354107379, summary.getFilesSize()); // 3 * 118 Mb
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("FIFI"));
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("RIRI"));
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("LOULOU"));
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("FIFI"));
    }

    private static class Data implements IIndexable, IDocFiles {

        private String docId;

        private Set<String> tags = new HashSet<>();

        private Multimap<DataType, fr.cnes.regards.modules.indexer.domain.DataFile> files = HashMultimap.create();

        public Data() {
        }

        @SuppressWarnings("unused")
        public Data(String docId, Set<String> tags) {
            this.docId = docId;
            this.tags = tags;
        }

        @Override
        public String getDocId() {
            return docId;
        }

        @Override
        public String getType() {
            return TYPE;
        }

        public void setDocId(String docId) {
            this.docId = docId;
        }

        @SuppressWarnings("unused")
        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        @Override
        public Multimap<DataType, fr.cnes.regards.modules.indexer.domain.DataFile> getFiles() {
            return files;
        }

        @SuppressWarnings("unused")
        public void setFiles(Multimap<DataType, fr.cnes.regards.modules.indexer.domain.DataFile> files) {
            this.files = files;
        }
    }

    private static class DataFile extends fr.cnes.regards.modules.indexer.domain.DataFile {

        @SuppressWarnings("unused")
        public DataFile() {
        }

        public DataFile(File file, DataType type) {
            this.setFilesize(file.length());
            switch (type) {
                case RAWDATA:
                    super.setUri(file.toURI());
                    break;
                case QUICKLOOK_HD:
                    super.setUri(new File(file.getParentFile(), file.getName() + "_QL_HD").toURI());
                    break;
                case QUICKLOOK_MD:
                    super.setUri(new File(file.getParentFile(), file.getName() + "_QL_MD").toURI());
                    break;
                case QUICKLOOK_SD:
                    super.setUri(new File(file.getParentFile(), file.getName() + "_QL_SD").toURI());
                    break;
                case DESCRIPTION:
                case AIP:
                case DOCUMENT:
                case OTHER:
                case THUMBNAIL:
                    throw new IllegalArgumentException("Unsupported data type : " + type);
            }
        }
    }
}
