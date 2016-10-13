/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.starter.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.microservices.core.test.report.annotation.Purpose;
import fr.cnes.regards.microservices.core.test.report.annotation.Requirement;
import fr.cnes.regards.test.integration.AbstractRegardsIntegrationTest;

/**
 * @author lmieulet
 *
 */
public class CollectionControllerIT extends AbstractRegardsIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(CollectionControllerIT.class);

    private String jwt_;

    @Autowired
    private MockMvc mvc_;

    @Autowired
    private JWTService jwtService_;

    @Autowired
    private MethodAuthorizationService authService_;

    @Before
    public void setup() {
        String role = "USER";
        jwt_ = jwtService_.generateToken("PROJECT", "email", "MSI", role);
        authService_.setAuthorities("/collections", RequestMethod.GET, role);
        authService_.setAuthorities("/collections/model/{model_id}", RequestMethod.GET, role, "ADMIN");
    }

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections")
    @Test
    public void testGetCollections() {

        try {
            this.mvc_.perform(get("/collections").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt_))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            String message = "Cannot retrieve all collections";
            LOG.error(message, e);
            Assert.fail(message);
        }
    }

}
