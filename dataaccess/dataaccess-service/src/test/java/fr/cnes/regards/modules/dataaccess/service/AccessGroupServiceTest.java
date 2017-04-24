/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.EntityAlreadyExistsException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.accessrights.client.IProjectUsersClient;
import fr.cnes.regards.modules.accessrights.domain.projects.ProjectUser;
import fr.cnes.regards.modules.dataaccess.dao.IAccessGroupRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessGroupServiceTest {

    /**
     *
     */
    private static final long AG1_ID = 1L;

    private AccessGroupService accessGroupService;

    private AccessGroup accessGroup1;

    private static final String AG1_NAME = "AG1";

    private static final String USER1_EMAIL = "toto@tata.titi";

    private IAccessGroupRepository dao;

    private IProjectUsersClient projectUserClient;

    /**
     * Publish for model changes
     */
    private IPublisher mockPublisher;

    @Before
    public void init() {
        dao = Mockito.mock(IAccessGroupRepository.class);
        projectUserClient = Mockito.mock(IProjectUsersClient.class);
        mockPublisher = Mockito.mock(IPublisher.class);
        accessGroupService = new AccessGroupService(dao, projectUserClient, mockPublisher);
        accessGroupService.setMicroserviceName("test");
        accessGroup1 = new AccessGroup(AG1_NAME);
        accessGroup1.setId(AG1_ID);
    }

    @Test(expected = EntityAlreadyExistsException.class)
    public void testCreateAccessGroupDuplicate() throws EntityAlreadyExistsException {
        Mockito.when(dao.findOneByName(AG1_NAME)).thenReturn(accessGroup1);

        final AccessGroup duplicate = new AccessGroup(AG1_NAME);
        accessGroupService.createAccessGroup(duplicate);
    }

    @Test
    public void testCreateAccessGroup() throws EntityAlreadyExistsException {
        final AccessGroup notDuplicate = new AccessGroup(AG1_NAME + "different");
        final AccessGroup shouldReturn = new AccessGroup(AG1_NAME + "different");
        shouldReturn.setId(2L);

        Mockito.when(dao.save(notDuplicate)).thenReturn(shouldReturn);

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

}
