/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.microservices.dam;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.microservices.core.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.microservices.core.test.report.annotation.Purpose;
import fr.cnes.regards.microservices.core.test.report.annotation.Requirement;
import fr.cnes.regards.security.utils.jwt.JWTService;

/**
 * @author msordi
 *
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@AutoConfigureMockMvc
public class ModelControllerIT {

    private static final Logger LOG = LoggerFactory.getLogger(ModelControllerIT.class);

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
        authService_.setAuthorities("/models/attributes", RequestMethod.GET, role);
        authService_.setAuthorities("/models/attributes/{pAttributeId}", RequestMethod.GET, role, "ADMIN");
    }

    @Requirement("MA_REF_EXIGENCE")
    @Test
    public void testGetAttributes() {

        try {
            this.mvc_.perform(get("/models/attributes").header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt_))
                    .andExpect(status().isOk());
        }
        catch (Exception e) {
            String message = "Cannot reach model attributes";
            LOG.error(message, e);
            Assert.fail(message);
        }
    }

    @Requirement("TOTO1")
    @Requirement("TOTO2")
    @Purpose("Dis ok TOTO!")
    @Test
    public void testToto() {
        LOG.debug("OK TOTO");
    }

    @Requirement("FOO1")
    @Requirement("FOO2")
    @Test
    public void testFoo() {
        LOG.debug("OK FOO");
    }

}
