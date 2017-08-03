package fr.cnes.regards.modules.indexer.dao;

import java.util.function.Consumer;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.elasticsearch.index.IndexNotFoundException;
import org.junit.Assume;
import org.junit.BeforeClass;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.cnes.regards.modules.indexer.dao.builder.AggregationBuilderFacetTypeVisitor;

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

    /**
     * Befor class setting up method
     *
     * @throws Exception
     *             exception
     */
    @BeforeClass
    public static void setUp() throws Exception {
        boolean repositoryOK = true;
        try {
            gson = new GsonBuilder().create();
            // FIXME valeurs en dur pour l'instant
            repository = new EsRepository(gson, null, "172.26.47.52", 9300, "regards",
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
}
