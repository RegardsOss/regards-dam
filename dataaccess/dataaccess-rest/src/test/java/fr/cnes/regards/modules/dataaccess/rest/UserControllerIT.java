/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.rest;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.models.client.IAttributeModelClient;
import fr.cnes.regards.modules.models.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@MultitenantTransactional
@TestPropertySource("classpath:test.properties")
public class UserControllerIT extends AbstractRegardsTransactionalIT {

    @Configuration
    static class Conf {
        @Bean
        public IAttributeModelClient attributeModelClient() {
            return Mockito.mock(IAttributeModelClient.class);
        }

        @Bean
        @Primary
        public IOpenSearchService openSearchService() {
            return Mockito.mock(IOpenSearchService.class);
        }

        @Bean
        public IProjectsClient projectsClient() {
            return Mockito.mock(IProjectsClient.class);
        }

        @Bean
        public IModelAttrAssocClient modelAttrAssocClient() {
            return Mockito.mock(IModelAttrAssocClient.class);
        }
    }

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
