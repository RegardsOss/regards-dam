/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.cnes.regards.modules.dataaccess.rest;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestParamBuilder;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.dataaccess.dao.IAccessGroupRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.service.IAccessGroupService;
import fr.cnes.regards.modules.models.client.IAttributeModelClient;
import fr.cnes.regards.modules.models.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.opensearch.service.IOpenSearchService;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;

/**
 * REST module controller
 *
 * @author Marc Sordi
 */
@MultitenantTransactional
@TestPropertySource("classpath:test.properties")
public class AccessGroupControllerIT extends AbstractRegardsTransactionalIT {

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


        @Bean
        public IProjectUsersClient mockProjectUsersClient() {
            return Mockito.mock(IProjectUsersClient.class);
        }

    }

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessGroupControllerIT.class);

    private static final String ACCESS_GROUPS_ERROR_MSG = "";

    private static final String AG1_NAME = "AG1";

    private static final String AG2_NAME = "AG2";

    private static final String USER1_EMAIL = "user1@user1.user1";

    private AccessGroup ag1;

    @Autowired
    private IAccessGroupRepository dao;

    @Autowired
    private IAccessGroupService agService;

    @Before
    public void init() {
        IProjectUsersClient projectUserClientMock = Mockito.mock(IProjectUsersClient.class);
        // Replace stubs by mocks
        ReflectionTestUtils.setField(agService, "projectUserClient", projectUserClientMock, IProjectUsersClient.class);
        Mockito.when(projectUserClientMock.retrieveProjectUserByEmail(Matchers.any()))
                .thenReturn(new ResponseEntity<>(new Resource<>(new ProjectUser()), HttpStatus.OK));
        Mockito.when(projectUserClientMock.retrieveProjectUserByEmail(Matchers.any()))
                .thenReturn(new ResponseEntity<>(new Resource<>(new ProjectUser()), HttpStatus.OK));
        ag1 = new AccessGroup(AG1_NAME);
        ag1 = dao.save(ag1);
        AccessGroup ag2 = new AccessGroup(AG2_NAME);
        ag2.setPublic(true);
        ag2 = dao.save(ag2);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_810")
    public void testRetrieveAccessGroupsList() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessGroupController.PATH_ACCESS_GROUPS, expectations, ACCESS_GROUPS_ERROR_MSG);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_810")
    public void testRetrievePublicAccessGroupsList() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessGroupController.PATH_ACCESS_GROUPS, expectations, ACCESS_GROUPS_ERROR_MSG,
                          RequestParamBuilder.build().param("public", "true"));
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_810")
    public void testRetrieveAccessGroup() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultGet(AccessGroupController.PATH_ACCESS_GROUPS + AccessGroupController.PATH_ACCESS_GROUPS_NAME,
                          expectations, ACCESS_GROUPS_ERROR_MSG, AG1_NAME);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_810")
    public void testDeleteAccessGroup() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(AccessGroupController.PATH_ACCESS_GROUPS + AccessGroupController.PATH_ACCESS_GROUPS_NAME,
                             expectations, ACCESS_GROUPS_ERROR_MSG, AG2_NAME);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_810")
    public void testCreateAccessGroup() {

        final AccessGroup toBeCreated = new AccessGroup("NameIsNeeded");

        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        // TODO: complete with some checks(Id is present, links...)
        performDefaultPost(AccessGroupController.PATH_ACCESS_GROUPS, toBeCreated, expectations,
                           ACCESS_GROUPS_ERROR_MSG);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_820")
    public void testAssociateUserToAccessGroup() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultPut(AccessGroupController.PATH_ACCESS_GROUPS
                + AccessGroupController.PATH_ACCESS_GROUPS_NAME_EMAIL, null, expectations, ACCESS_GROUPS_ERROR_MSG,
                          AG1_NAME, USER1_EMAIL);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SET_830")
    public void testDissociateUserFromAccessGroup() {
        final List<ResultMatcher> expectations = new ArrayList<>();
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_PATH_ROOT).isNotEmpty());
        performDefaultDelete(AccessGroupController.PATH_ACCESS_GROUPS
                + AccessGroupController.PATH_ACCESS_GROUPS_NAME_EMAIL, expectations, ACCESS_GROUPS_ERROR_MSG, AG1_NAME,
                             USER1_EMAIL);
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
