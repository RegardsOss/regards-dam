/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@MultitenantTransactional
@TestPropertySource("classpath:test.properties")
public class UserControllerIT extends AbstractRegardsTransactionalIT {

    private static final Logger LOG = LoggerFactory.getLogger(UserControllerIT.class);

    private static final String USER_ERROR_MSG = "the response body should not be empty and status should be 200";

    private User USER1;

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_810")
    public void testRetrieveAccessGroupsListOfUser() {
        USER1 = new User("user1@user1.user1");
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(UserController.BASE_PATH, expectations, USER_ERROR_MSG, USER1.getEmail());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

}
