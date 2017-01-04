/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.dataaccess.service;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.exception.RabbitMQVhostException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.dao.IGroupAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.dao.IUserAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AbstractAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.GroupAccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityFilter;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.UserAccessRight;
import fr.cnes.regards.modules.dataset.domain.DataSet;
import fr.cnes.regards.modules.dataset.service.DataSetService;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.domain.EntityType;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessRightServiceTest {

    private static final String DESC = "Desc";

    private AccessRightService service;

    private IAccessRightRepository<AbstractAccessRight> arRepo;

    private IGroupAccessRightRepository garRepo;

    private IUserAccessRightRepository uarRepo;

    private AccessGroupService agService;

    private DataSetService dsService;

    private IPublisher eventPublisher;

    private AccessGroup AG1;

    private AccessGroup AG2;

    private DataSet DS1;

    private DataSet DS2;

    private User USER1;

    private User USER2;

    private GroupAccessRight GAR11;

    private GroupAccessRight GAR22;

    private UserAccessRight UAR11;

    private UserAccessRight UAR22;

    private GroupAccessRight GAR12;

    private GroupAccessRight GAR21;

    private UserAccessRight UAR12;

    private UserAccessRight UAR21;

    @SuppressWarnings("unchecked")
    @Before
    public void init() {
        arRepo = Mockito.mock(IAccessRightRepository.class);
        garRepo = Mockito.mock(IGroupAccessRightRepository.class);
        uarRepo = Mockito.mock(IUserAccessRightRepository.class);
        agService = Mockito.mock(AccessGroupService.class);
        dsService = Mockito.mock(DataSetService.class);
        eventPublisher = Mockito.mock(IPublisher.class);
        service = new AccessRightService(arRepo, garRepo, uarRepo, agService, dsService, eventPublisher);

        final Model model = Model.build("MODEL", DESC, EntityType.DATASET);
        AG1 = new AccessGroup("AG1");
        AG2 = new AccessGroup("AG2");
        DS1 = new DataSet(model, DESC, "DS1");
        DS2 = new DataSet(model, DESC, "DS2");
        USER1 = new User("user1@user1.user1");
        USER2 = new User("user2@user2.user2");
        final QualityFilter qf = new QualityFilter(10, 0, QualityLevel.ACCEPTED);
        final AccessLevel al = AccessLevel.FULL_ACCES;
        GAR11 = new GroupAccessRight(qf, al, DS1, AG1);
        GAR12 = new GroupAccessRight(qf, al, DS1, AG2);
        GAR21 = new GroupAccessRight(qf, al, DS2, AG1);
        GAR22 = new GroupAccessRight(qf, al, DS2, AG2);
        GAR22.setId(1L);
        UAR11 = new UserAccessRight(qf, al, DS1, USER1);
        UAR12 = new UserAccessRight(qf, al, DS1, USER2);
        UAR21 = new UserAccessRight(qf, al, DS2, USER1);
        UAR22 = new UserAccessRight(qf, al, DS2, USER2);
    }

    @Test
    public void testRetrieveAccessRightsFullArgs() throws EntityNotFoundException {
        List<UserAccessRight> expected = new ArrayList<>();
        expected.add(UAR11);
        Page<UserAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(uarRepo.findAllByUserAndDataSet(USER1, DS1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.existUser(USER1)).thenReturn(Boolean.TRUE);
        Mockito.when(dsService.retrieveDataSet(DS1.getIpId())).thenReturn(DS1);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(AG1.getName(), DS1.getIpId(), USER1.getEmail(),
                                                                        new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(UAR11));
        Assert.assertFalse(result.getContent().contains(UAR12));
        Assert.assertFalse(result.getContent().contains(UAR21));
        Assert.assertFalse(result.getContent().contains(UAR22));
        Assert.assertFalse(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsNoArgs() throws EntityNotFoundException {
        List<AbstractAccessRight> expected = new ArrayList<>();
        expected.add(UAR11);
        expected.add(UAR12);
        expected.add(UAR21);
        expected.add(UAR22);
        expected.add(GAR11);
        expected.add(GAR12);
        expected.add(GAR21);
        expected.add(GAR22);
        Page<AbstractAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(arRepo.findAll(pageable)).thenReturn(pageExpected);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(null, null, null, new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(UAR11));
        Assert.assertTrue(result.getContent().contains(UAR12));
        Assert.assertTrue(result.getContent().contains(UAR21));
        Assert.assertTrue(result.getContent().contains(UAR22));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertTrue(result.getContent().contains(GAR12));
        Assert.assertTrue(result.getContent().contains(GAR21));
        Assert.assertTrue(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsDSArgs() throws EntityNotFoundException {
        List<AbstractAccessRight> expected = new ArrayList<>();
        expected.add(UAR11);
        expected.add(UAR12);
        expected.add(GAR11);
        expected.add(GAR12);
        Page<AbstractAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(arRepo.findAllByDataSet(DS1, pageable)).thenReturn(pageExpected);
        Mockito.when(dsService.retrieveDataSet(DS1.getIpId())).thenReturn(DS1);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(null, DS1.getIpId(), null,
                                                                        new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(UAR11));
        Assert.assertTrue(result.getContent().contains(UAR12));
        Assert.assertFalse(result.getContent().contains(UAR21));
        Assert.assertFalse(result.getContent().contains(UAR22));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertTrue(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsGroupArgs() throws EntityNotFoundException {
        List<GroupAccessRight> expected = new ArrayList<>();
        expected.add(GAR11);
        expected.add(GAR21);
        Page<GroupAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(garRepo.findAllByAccessGroup(AG1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.retrieveAccessGroup(AG1.getName())).thenReturn(AG1);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(AG1.getName(), null, null,
                                                                        new PageRequest(0, 10));
        Assert.assertFalse(result.getContent().contains(UAR11));
        Assert.assertFalse(result.getContent().contains(UAR12));
        Assert.assertFalse(result.getContent().contains(UAR21));
        Assert.assertFalse(result.getContent().contains(UAR22));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertTrue(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsUserArgs() throws EntityNotFoundException {
        List<UserAccessRight> expected = new ArrayList<>();
        expected.add(UAR11);
        expected.add(UAR21);
        Page<UserAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(uarRepo.findAllByUser(USER1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.existUser(USER1)).thenReturn(Boolean.TRUE);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(null, null, USER1.getEmail(),
                                                                        new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(UAR11));
        Assert.assertFalse(result.getContent().contains(UAR12));
        Assert.assertTrue(result.getContent().contains(UAR21));
        Assert.assertFalse(result.getContent().contains(UAR22));
        Assert.assertFalse(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsGroupAndDSArgs() throws EntityNotFoundException {
        List<GroupAccessRight> expected = new ArrayList<>();
        expected.add(GAR11);
        Page<GroupAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(garRepo.findAllByAccessGroupAndDataSet(AG1, DS1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.retrieveAccessGroup(AG1.getName())).thenReturn(AG1);
        Mockito.when(dsService.retrieveDataSet(DS1.getIpId())).thenReturn(DS1);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(AG1.getName(), DS1.getIpId(), null,
                                                                        new PageRequest(0, 10));
        Assert.assertFalse(result.getContent().contains(UAR11));
        Assert.assertFalse(result.getContent().contains(UAR12));
        Assert.assertFalse(result.getContent().contains(UAR21));
        Assert.assertFalse(result.getContent().contains(UAR22));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsUserAndDSArgs() throws EntityNotFoundException {
        List<UserAccessRight> expected = new ArrayList<>();
        expected.add(UAR11);
        Page<UserAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(uarRepo.findAllByUserAndDataSet(USER1, DS1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.existUser(USER1)).thenReturn(Boolean.TRUE);
        Mockito.when(dsService.retrieveDataSet(DS1.getIpId())).thenReturn(DS1);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(null, DS1.getIpId(), USER1.getEmail(),
                                                                        new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(UAR11));
        Assert.assertFalse(result.getContent().contains(UAR12));
        Assert.assertFalse(result.getContent().contains(UAR21));
        Assert.assertFalse(result.getContent().contains(UAR22));
        Assert.assertFalse(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsGroupAndUserArgs() throws EntityNotFoundException {
        List<UserAccessRight> expected = new ArrayList<>();
        expected.add(UAR11);
        expected.add(UAR21);
        Page<UserAccessRight> pageExpected = new PageImpl<>(expected);
        Pageable pageable = new PageRequest(0, 10);
        Mockito.when(uarRepo.findAllByUser(USER1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.existUser(USER1)).thenReturn(Boolean.TRUE);

        Page<AbstractAccessRight> result = service.retrieveAccessRights(AG1.getName(), null, USER1.getEmail(),
                                                                        new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(UAR11));
        Assert.assertFalse(result.getContent().contains(UAR12));
        Assert.assertTrue(result.getContent().contains(UAR21));
        Assert.assertFalse(result.getContent().contains(UAR22));
        Assert.assertFalse(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateAccessRightNotFound()
            throws EntityNotFoundException, EntityInconsistentIdentifierException, RabbitMQVhostException {
        Mockito.when(arRepo.findOne(3L)).thenReturn(null);
        service.updateAccessRight(3L, GAR22);
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void testUpdateAccessRightInconsistentId()
            throws EntityNotFoundException, EntityInconsistentIdentifierException, RabbitMQVhostException {
        Mockito.when(arRepo.findOne(3L)).thenReturn(UAR11);
        service.updateAccessRight(3L, GAR22);
    }
}
