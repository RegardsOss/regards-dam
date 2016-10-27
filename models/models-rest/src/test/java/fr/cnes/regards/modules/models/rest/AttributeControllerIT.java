/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.models.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.web.bind.annotation.RequestMethod;

import fr.cnes.regards.framework.test.integration.AbstractRegardsIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;

/**
 * Test module API
 *
 * @author msordi
 *
 */
public class AttributeControllerIT extends AbstractRegardsIT {

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(AttributeControllerIT.class);

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
        final String tenant = "PROJECT";

        final String jwt = jwtService.generateToken(tenant, "email", "MSI", role);
        authService.setAuthorities(tenant, endpoint, RequestMethod.GET, role);

        final List<ResultMatcher> expectations = new ArrayList<>(1);
        expectations.add(MockMvcResultMatchers.status().isOk());

        performGet(endpoint, jwt, expectations, "Cannot get all attributes");
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
