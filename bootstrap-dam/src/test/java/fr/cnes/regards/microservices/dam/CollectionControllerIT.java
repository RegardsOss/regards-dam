/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import java.util.ArrayList;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.security.autoconfigure.endpoint.DefaultMethodAuthorizationService;
import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.test.integration.AbstractRegardsIntegrationTest;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 *
 */
public class CollectionControllerIT extends AbstractRegardsIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionControllerIT.class);

    private String jwt;

    @Autowired
    private JWTService jwtService;

    @Autowired
    private DefaultMethodAuthorizationService authService;

    private Model model1;

    private Collection collection1;

    @Autowired
    private ICollectionRepository collectionRepository;

    private List<ResultMatcher> expectations;

    @Before
    public void setup() {
        final String role = "USER";
        jwt = jwtService.generateToken("PROJECT", "email", "MSI", role);
        expectations = new ArrayList<>();

        // Reset entities list
        collectionRepository.deleteAll();

        // Bootstrap default values
        model1 = new Model();

        collection1 = collectionRepository.save(new Collection(model1, "pDescription", "pName"));
    }

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections")
    @Test
    public void testGetCollections() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize(1)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].name", Matchers.is(collection1.getName())));

        performGet("/collections", jwt, expectations, "Failed to fetch collection list");
    }

    @Requirement("REGARDS_DSL_DAM_COL_xxx")
    @Purpose("Shall create a new collection")
    @Test
    public void testPostCollection() {
        final Collection collection2 = new Collection(model1, "pDescription2", "pName2");

        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));

        // Some assertion based on https://github.com/jayway/JsonPath#path-examples syntax
        expectations.add(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(collection2.getDescription())));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(collection2.getName())));
        expectations.add(MockMvcResultMatchers.jsonPath("$.sidId", Matchers.anything()));
        expectations.add(MockMvcResultMatchers.jsonPath("$.links.*", Matchers.hasSize(0)));
        performPost("/collections", jwt, collection2, expectations, "Failed to create a new collection");

    }

    @Requirement("REGARDS_DSL_DAM_COL_310")
    @Purpose("Shall retrieve a collection using its id")
    @Test
    public void testGetCollectionById() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(collection1.getDescription())));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(collection1.getName())));
        performGet("/collections/{collection_id}", jwt, expectations,
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
        performGet("/collections/model/{model_id}", jwt, expectations,
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
        performPut("/collections/{collection_id}", jwt, collectionClone, expectations,
                   "Failed to update a specific collection using its id", collection1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_110")
    @Purpose("Shall delete a collection")
    @Test
    public void testDeleteCollection() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        performDelete("/collections/{collection_id}", jwt, expectations,
                      "Failed to delete a specific collection using its id", collection1.getId());
    }
}
