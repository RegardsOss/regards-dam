package fr.cnes.regards.modules.indexer.dao;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor;
import fr.cnes.regards.modules.indexer.domain.DocFilesSummary;
import fr.cnes.regards.modules.indexer.domain.IDocFiles;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;

/**
 * Test on complex aggs
 * @author oroussel
 */
public class EsAggsTest {

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
     * Befor class setting up method
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
            gson = new GsonBuilder().create();
            repository = new EsRepository(gson, null, propMap.get("regards.elasticsearch.address"),
                                          Integer.parseInt(propMap.get("regards.elasticsearch.tcp.port")),
                                          propMap.get("regards.elasticsearch.cluster.name"),
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
    }

    private static final String[] TAGS = new String[] { "RIRI", "FIFI", "LOULOU", "MICHOU", "JOJO" };

    private static final Random random = new Random();

    private static Set<String> randomTags() {
        String[] randomTags = new String[random.nextInt(TAGS.length)];
        Set<String> randomSet = new HashSet<>();
        int size = random.nextInt(TAGS.length) + 1;
        if (size == TAGS.length) {
            return Sets.newHashSet(TAGS);
        }
        for (int i = 0; i < size; i++) {
            while (!randomSet.add(TAGS[random.nextInt(TAGS.length)]))
                ;
        }
        return randomSet;
    }

    private void createData() {
        repository.deleteIndex(INDEX);
        if (!repository.indexExists(INDEX)) {
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
        DocFilesSummary summary = repository
                .computeDataFilesSummary(new SimpleSearchKey<>(INDEX, TYPE, Data.class), null, "tags", "RAWDATA",
                                         "QUICKLOOK_HD");
        System.out.println(summary);
        Assert.assertEquals(12, summary.getTotalDocumentsCount());
        Assert.assertEquals(24, summary.getTotalFilesCount());
        Assert.assertEquals(236071754, summary.getTotalFilesSize()); // 2 * 118 Mb
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("FIFI"));
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("RIRI"));
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("LOULOU"));
        Assert.assertTrue(summary.getSubSummariesMap().containsKey("FIFI"));
    }

    private static class Data implements IIndexable, IDocFiles {

        private String docId;

        private Set<String> tags = new HashSet<>();

        private Map<DataType, fr.cnes.regards.modules.indexer.domain.DataFile> files = new HashMap<>();

        public Data() {
        }

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

        public Set<String> getTags() {
            return tags;
        }

        public void setTags(Set<String> tags) {
            this.tags = tags;
        }

        public Map<DataType, fr.cnes.regards.modules.indexer.domain.DataFile> getFiles() {
            return files;
        }

        public void setFiles(Map<DataType, fr.cnes.regards.modules.indexer.domain.DataFile> files) {
            this.files = files;
        }
    }

    private static class DataFile extends fr.cnes.regards.modules.indexer.domain.DataFile {
        public DataFile() {
        }

        public DataFile(File file, DataType type) {
            this.setFileSize(file.length());
            switch (type) {
                case RAWDATA:
                    super.setFileRef(file.toURI());
                    break;
                case QUICKLOOK_HD:
                    super.setFileRef(new File(file.getParentFile(), file.getName() + "_QL_HD").toURI());
                    break;
                case QUICKLOOK_MD:
                    super.setFileRef(new File(file.getParentFile(), file.getName() + "_QL_MD").toURI());
                    break;
                case QUICKLOOK_SD:
                    super.setFileRef(new File(file.getParentFile(), file.getName() + "_QL_SD").toURI());
                    break;
            }
        }
    }

    private static enum DataType {
        RAWDATA, QUICKLOOK_SD, QUICKLOOK_MD, QUICKLOOK_HD, DOCUMENT, THUMBNAIL, OTHER;
    }
}
