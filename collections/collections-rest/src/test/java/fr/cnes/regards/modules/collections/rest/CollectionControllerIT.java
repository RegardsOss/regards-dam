/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.collections.rest;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(locations = { "classpath:test.properties" })
// @EnableJpaRepositories(basePackages = { "fr.cnes.regards.modules" })
// @EntityScan(basePackages = { "fr.cnes.regards.modules" })
public class CollectionControllerIT extends AbstractRegardsIT {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CollectionControllerIT.class);

    private Model model1;

    private Collection collection1;

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private IModelRepository modelRepository;

    private List<ResultMatcher> expectations;

    @Before
    public void setup() {
        expectations = new ArrayList<>();

        // Reset entities list
        collectionRepository.deleteAll();

        // Bootstrap default values
        model1 = new Model();
        model1.setId(1L);

        modelRepository.save(model1);

        collection1 = collectionRepository.save(new Collection(1L, "IpID", model1, "pDescription", "pName"));
    }

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections")
    @Test
    public void testGetCollections() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize(1)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].name", Matchers.is(collection1.getName())));

        performDefaultGet("/collections", expectations, "Failed to fetch collection list");
    }

    @Requirement("REGARDS_DSL_DAM_COL_xxx")
    @Purpose("Shall create a new collection")
    @Test
    public void testPostCollection() {
        final Collection collection2 = new Collection(2L, "IpID2", model1, "pDescription2", "pName2");

        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));

        // Some assertion based on https://github.com/jayway/JsonPath#path-examples syntax
        expectations.add(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(collection2.getDescription())));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(collection2.getName())));
        expectations.add(MockMvcResultMatchers.jsonPath("$.sidId", Matchers.anything()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.links.*", Matchers.hasSize(0)));
        performDefaultPost("/collections", collection2, expectations, "Failed to create a new collection");

    }

    @Requirement("REGARDS_DSL_DAM_COL_310")
    @Purpose("Shall retrieve a collection using its id")
    @Test
    public void testGetCollectionById() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(collection1.getDescription())));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(collection1.getName())));
        performDefaultGet("/collections/{collection_id}", expectations,
                          "Failed to fetch a specific collection using its id", collection1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_410")
    @Purpose("Shall retrieve a collection using its model id")
    @Test
    public void testGetCollectionByModelId() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize(1)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].name", Matchers.is(collection1.getName())));
        expectations
                .add(MockMvcResultMatchers.jsonPath("$.[0].description", Matchers.is(collection1.getDescription())));
        performDefaultGet("/collections/model/{model_id}", expectations,
                          "Failed to fetch a specific collection using its model id", model1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_xxx")
    @Purpose("Shall update a collection")
    @Test
    public void testUpdateCollection() {
        final Collection collectionClone = new Collection();
        collectionClone.setId(collection1.getId());
        final String newName = "new name";
        collectionClone.setName(newName);
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(newName)));
        performDefaultPut("/collections/{collection_id}", collectionClone, expectations,
                          "Failed to update a specific collection using its id", collection1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_110")
    @Purpose("Shall delete a collection")
    @Test
    public void testDeleteCollection() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        performDefaultDelete("/collections/{collection_id}", expectations,
                             "Failed to delete a specific collection using its id", collection1.getId());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
