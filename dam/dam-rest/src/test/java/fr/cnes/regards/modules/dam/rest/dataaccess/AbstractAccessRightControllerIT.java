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
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
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
public abstract class AbstractAccessRightControllerIT extends AbstractRegardsTransactionalIT {

    protected static final String ACCESS_RIGHTS_ERROR_MSG = "Should have been an answer";

    @Autowired
    protected IModelRepository modelRepo;

    @Autowired
    protected IDatasetRepository dsRepo;

    @Autowired
    protected IAccessGroupRepository agRepo;

    protected AccessGroup ag1;

    protected AccessGroup ag2;

    protected final String ag1Name = "AG1";

    protected final String ag2Name = "AG2";

    protected QualityFilter qf;

    protected final AccessLevel al = AccessLevel.FULL_ACCESS;

    protected DataAccessRight dar;

    protected Dataset ds1;

    protected Dataset ds2;

    protected final String ds1Name = "DS1";

    protected final String ds2Name = "DS2";

    protected AccessRight ar1;

    protected AccessRight ar2;

    protected AccessRight ar3;

    protected ProjectUser projectUser;

    protected User user;

    protected final String email = "test@email.com";

    @Autowired
    protected IAccessGroupService agService;

    @Autowired
    protected IAccessRightRepository arRepo;

    @Autowired
    protected IRuntimeTenantResolver runtimetenantResolver;

    @Autowired
    protected IProjectUsersClient projectUserClientMock;

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
}
