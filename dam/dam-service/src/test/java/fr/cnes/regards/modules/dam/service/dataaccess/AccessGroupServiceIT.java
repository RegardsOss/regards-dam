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
package fr.cnes.regards.modules.dam.service.dataaccess;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.utils.RegardsTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceTransactionalIT;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.dam.dao.dataaccess.IAccessGroupRepository;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.service.dataaccess.AccessGroupService;
import fr.cnes.regards.modules.dam.service.dataaccess.IAccessGroupService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource(locations = "classpath:dataaccess.properties")
@ContextConfiguration(classes = { TestAccessGroupConfiguration.class })
@RegardsTransactional
@DirtiesContext
public class AccessGroupServiceIT extends AbstractRegardsServiceTransactionalIT {

    @Autowired
    private IAccessGroupService accessGroupService;

    private AccessGroup accessGroup1;

    private static final String AG1_NAME = "AG1";

    private static final String USER1_EMAIL = "toto@tata.titi";

    @Autowired
    private IAccessGroupRepository dao;

    @Autowired
    private IProjectUsersClient projectUserClient;

    @Autowired
    private IRuntimeTenantResolver runtimeTenantResolver;

    @Before
    public void init() {
        runtimeTenantResolver.forceTenant(getDefaultTenant());

        accessGroup1 = new AccessGroup(AG1_NAME);
        dao.save(accessGroup1);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void testCreateAccessGroupDuplicate() throws EntityAlreadyExistsException {
        final AccessGroup duplicate = new AccessGroup(AG1_NAME);
        accessGroupService.createAccessGroup(duplicate);
    }

    @Test
    public void testCreateAccessGroup() throws EntityAlreadyExistsException {
        final AccessGroup notDuplicate = new AccessGroup(AG1_NAME + "different");
        final AccessGroup shouldReturn = new AccessGroup(AG1_NAME + "different");
        shouldReturn.setId(2L);

        final AccessGroup after = accessGroupService.createAccessGroup(notDuplicate);
        Assert.assertEquals(shouldReturn, after);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testAssociateUserToGroupWithUnknownUser() throws EntityNotFoundException {
        final ResponseEntity<Resource<ProjectUser>> mockedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Mockito.when(projectUserClient.retrieveProjectUserByEmail(USER1_EMAIL)).thenReturn(mockedResponse);

        accessGroupService.associateUserToAccessGroup(USER1_EMAIL, AG1_NAME);
    }

    @Test(expected = EntityNotFoundException.class)
    public void testDissociateUserFromGroupWithUnknownUser() throws EntityNotFoundException {
        final ResponseEntity<Resource<ProjectUser>> mockedResponse = new ResponseEntity<>(HttpStatus.NOT_FOUND);
        Mockito.when(projectUserClient.retrieveProjectUserByEmail(USER1_EMAIL)).thenReturn(mockedResponse);

        accessGroupService.dissociateUserFromAccessGroup(USER1_EMAIL, AG1_NAME);
    }

    @Test
    public void testDocumentAccessGroupCreated() throws EntityAlreadyExistsException {
        final AccessGroup documentAccessGroup = dao.findOneByName(AccessGroupService.ACCESS_GROUP_PUBLIC_DOCUMENTS);
        dao.findAll();
        Assert.assertEquals(documentAccessGroup.isPublic(), true);
    }

}
