/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.rest.dataaccess;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestParamBuilder;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.dam.dao.dataaccess.IAccessGroupRepository;
import fr.cnes.regards.modules.dam.dao.dataaccess.IAccessRightRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.models.IModelRepository;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.User;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.DataAccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.DataAccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.QualityFilter;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.QualityLevel;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.service.dataaccess.IAccessGroupService;

/**
 * @author Sylvain Vissiere-Guerinet
 * @author Léo Mieulet
 */
@MultitenantTransactional
@TestPropertySource("classpath:test.properties")
public class AccessRightControllerIT extends AbstractRegardsTransactionalIT {
    private static final String ACCESS_RIGHTS_ERROR_MSG = "Should have been an answer";

    @Autowired
    private IModelRepository modelRepo;

    @Autowired
    private IDatasetRepository dsRepo;

    @Autowired
    private IAccessGroupRepository agRepo;

    private AccessGroup ag1;

    private AccessGroup ag2;

    private final String ag1Name = "AG1";

    private final String ag2Name = "AG2";

    private QualityFilter qf;

    private final AccessLevel al = AccessLevel.FULL_ACCESS;

    private DataAccessRight dar;

    private Dataset ds1;

    private Dataset ds2;

    private final String ds1Name = "DS1";

    private final String ds2Name = "DS2";

    private final String dsDesc = "http://test.test";

    private AccessRight ar1;

    private AccessRight ar2;

    private AccessRight ar3;

    private ProjectUser projectUser;

    private User user;

    private final String email = "test@email.com";

    @Autowired
    private IAccessGroupService agService;

    @Autowired
    private IAccessRightRepository arRepo;

    @Autowired
    private IRuntimeTenantResolver runtimetenantResolver;

    @Autowired
    private IProjectUsersClient projectUserClientMock;

    @Before
    public void init() {
        runtimetenantResolver.forceTenant(getDefaultTenant());
        OffsetDateTime now = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        // Replace stubs by mocks
        ReflectionTestUtils.setField(agService, "projectUserClient", projectUserClientMock, IProjectUsersClient.class);
        projectUser = new ProjectUser();
        projectUser.setEmail(email);
        Mockito.when(projectUserClientMock.retrieveProjectUser(ArgumentMatchers.any()))
                .thenReturn(new ResponseEntity<>(new Resource<>(projectUser), HttpStatus.OK));

        qf = new QualityFilter(10, 0, QualityLevel.ACCEPTED);
        dar = new DataAccessRight(DataAccessLevel.NO_ACCESS);
        user = new User();
        user.setEmail(email);

        Model model = Model.build("model1", "desc", EntityType.DATASET);
        model = modelRepo.save(model);
        ds1 = new Dataset(model, "PROJECT", "DS1", ds1Name);
        ds1.setLicence("licence");
        ds1.setCreationDate(now);
        ds1 = dsRepo.save(ds1);
        ds2 = new Dataset(model, "PROJECT", "DS2", ds2Name);
        ds2.setLicence("licence");
        ds2.setCreationDate(now);
        ds2 = dsRepo.save(ds2);

        ag1 = new AccessGroup(ag1Name);
        ag1.addUser(user);
        ag1 = agRepo.save(ag1);
        ar1 = new AccessRight(qf, al, ds1, ag1);
        ar1.setDataAccessRight(dar);
        ar1 = arRepo.save(ar1);
        ag2 = new AccessGroup(ag2Name);
        ag2 = agRepo.save(ag2);
        ar2 = new AccessRight(qf, al, ds2, ag2);
        ar2 = arRepo.save(ar2);
        ar3 = new AccessRight(qf, al, ds2, ag2);
    }

    @Test
    public void testRetrieveAccessRightsNoArgs() {
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveDatasetWithAccessRights() {
        performDefaultGet(DatasetWithAccessRightController.ROOT_PATH + DatasetWithAccessRightController.GROUP_PATH,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG,
                          ag1Name);
    }

    @Test
    public void testRetrieveAccessRightsGroupArgs() {
        String sb = "?" + "accessgroup=" + ag1.getName();
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsDSArgs() {
        String sb = "?" + "dataset=" + ds1.getIpId();
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRightsFullArgs() {
        String sb = "?" + "dataset=" + ds1.getIpId() + "&accessgroup=" + ag1.getName();
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + sb,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testRetrieveAccessRight() {
        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT), ACCESS_RIGHTS_ERROR_MSG,
                          ar1.getId());
    }

    @Test
    public void testCreateAccessRight() {
        // Associated dataset must be updated
        performDefaultPost(AccessRightController.PATH_ACCESS_RIGHTS, ar3,
                           customizer().expectStatusCreated().expectIsNotEmpty(JSON_PATH_ROOT)
                                   .expectValue("$.content.dataset.groups[0]", ag2.getName()), ACCESS_RIGHTS_ERROR_MSG);
    }

    @Test
    public void testUpdateAccessRight() {
        // Change access level and group (ag2 instead of ag1)
        AccessRight garTmp = new AccessRight(qf, AccessLevel.RESTRICTED_ACCESS, ds1, ag2);
        garTmp.setId(ar1.getId());
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp, customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT)
                                  .expectValue("$.content.accessLevel", "RESTRICTED_ACCESS")
                                  .expectValue("$.content.dataset.groups[0]", ag2.getName()), ACCESS_RIGHTS_ERROR_MSG,
                          ar1.getId());

        // Save again garTmp (with ag1 as access group and FULL_ACCESS)
        garTmp = new AccessRight(qf, AccessLevel.FULL_ACCESS, ds1, ag1);
        garTmp.setId(ar1.getId());
        performDefaultPut(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                          garTmp, customizer().expectStatusOk().expectIsNotEmpty(JSON_PATH_ROOT)
                                  .expectValue("$.content.accessLevel", "FULL_ACCESS")
                                  .expectValue("$.content.dataset.groups[0]", ag1.getName()), ACCESS_RIGHTS_ERROR_MSG,
                          ar1.getId());
    }

    @Test
    public void testDeleteAccessRight() {
        performDefaultDelete(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_ACCESS_RIGHTS_ID,
                             customizer().expectStatusNoContent(), ACCESS_RIGHTS_ERROR_MSG, ar1.getId());
    }

    @Test
    public void testIsUserAutorisedToAccessDataset() {
        RequestParamBuilder requestParamBuilder = RequestParamBuilder.build().param("dataset", ds1.getIpId().toString())
                .param("user", email);

        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_IS_DATASET_ACCESSIBLE,
                          customizer().expectStatusOk().expect(MockMvcResultMatchers.content().string("true")),
                          ACCESS_RIGHTS_ERROR_MSG, requestParamBuilder);

        String notExistingUser = "not.existing" + email;

        performDefaultGet(AccessRightController.PATH_ACCESS_RIGHTS + AccessRightController.PATH_IS_DATASET_ACCESSIBLE,
                          customizer().expectStatusOk().expect(MockMvcResultMatchers.content().string("false"))
                                  .addParameter("dataset", ds1.getIpId().toString())
                                  .addParameter("user", notExistingUser), ACCESS_RIGHTS_ERROR_MSG, requestParamBuilder);
    }
}
