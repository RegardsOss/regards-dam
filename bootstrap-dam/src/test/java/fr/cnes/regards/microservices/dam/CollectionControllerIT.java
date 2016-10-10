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
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.microservices.core.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.microservices.core.test.AbstractRegardsIntegrationTest;
import fr.cnes.regards.microservices.core.test.report.annotation.Purpose;
import fr.cnes.regards.microservices.core.test.report.annotation.Requirement;
import fr.cnes.regards.modules.collections.dao.ICollectionRepository;
import fr.cnes.regards.modules.collections.domain.Collection;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.security.utils.jwt.JWTService;

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
    private MethodAuthorizationService authService;

    private Model pModel1;

    private Collection collection1;

    @Autowired
    private ICollectionRepository collectionRepository;

    @Before
    public void setup() {
        String role = "USER";
        jwt = jwtService.generateToken("PROJECT", "email", "MSI", role);
        authService.setAuthorities("/collections", RequestMethod.GET, role);
        authService.setAuthorities("/collections/model/{model_id}", RequestMethod.GET, role, "ADMIN");
        authService.setAuthorities("/collections", RequestMethod.POST, role, "ADMIN");
        authService.setAuthorities("/collections/{collection_id}", RequestMethod.GET, role, "ADMIN");

        // Reset entities list
        collectionRepository.deleteAll();

        // Bootstrap default values
        pModel1 = new Model();
        collection1 = collectionRepository.save(new Collection(pModel1, "pDescription", "pName"));
    }

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections")
    @Test
    public void testGetCollections() {
        List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.*", Matchers.hasSize(1)));
        expectations.add(MockMvcResultMatchers.jsonPath("$.[0].name", Matchers.is(collection1.getName())));

        performGet("/collections", jwt, expectations, "Failed to fetch collection list");
    }

    @Requirement("REGARDS_DSL_DAM_COL_xxx")
    @Purpose("Shall retrieve all collections")
    @Test
    public void testPostCollection() {

        Collection collection2 = collectionRepository.save(new Collection(pModel1, "pDescription2", "pName2"));
        List<ResultMatcher> expectations = new ArrayList<>();
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
        List<ResultMatcher> expectations = new ArrayList<>();

        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType("application/json;charset=UTF-8"));
        expectations.add(MockMvcResultMatchers.jsonPath("$.description", Matchers.is(collection1.getDescription())));
        expectations.add(MockMvcResultMatchers.jsonPath("$.name", Matchers.is(collection1.getName())));
        performGet("/collections/{collection_id}", jwt, expectations,
                   "Failed to fetch a specific collection using its id", collection1.getId());
    }
}
