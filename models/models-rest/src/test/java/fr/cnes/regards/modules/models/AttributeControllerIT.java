/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.security.utils.jwt.JWTService;
import fr.cnes.regards.framework.starter.security.endpoint.MethodAuthorizationService;
import fr.cnes.regards.framework.test.integration.annotation.RegardsIntegrationTest;
import fr.cnes.regards.microservices.core.test.report.annotation.Purpose;
import fr.cnes.regards.microservices.core.test.report.annotation.Requirement;

/**
 * Test module API
 *
 * @author msordi
 *
 */
@RunWith(SpringRunner.class)
@RegardsIntegrationTest
@AutoConfigureMockMvc
public class AttributeControllerIT {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeControllerIT.class);

    /**
     * Mock for MVC testing
     */
    @Autowired
    private MockMvc mvc;

    /**
     * JWT service
     */
    @Autowired
    private JWTService jwtService;

    /**
     * Authorization service method
     */
    @Autowired
    private MethodAuthorizationService authService;

    /**
     * Test get attributes
     *
     * @throws Exception
     *             if endpoint cannot be reached
     */
    @Requirement("REGARDS_DSL_DAM_MOD_010")
    @Requirement("REGARDS_DSL_DAM_MOD_020")
    @Purpose("Get model attributes to manage data models")
    @Test
    public void testGetAttributes() throws Exception {

        // Manage security context
        final String role = "USER";
        final String endpoint = "/models/attributes";

        final String jwt = jwtService.generateToken("PROJECT", "email", "MSI", role);
        authService.setAuthorities(endpoint, RequestMethod.GET, role);

        this.mvc.perform(get(endpoint).header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)).andExpect(status().isOk());
    }
}
