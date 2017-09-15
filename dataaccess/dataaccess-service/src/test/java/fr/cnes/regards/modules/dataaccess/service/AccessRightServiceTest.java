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
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.User;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityFilter;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityLevel;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.DatasetService;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
public class AccessRightServiceTest {

    private static final String DESC = "Desc";

    private IAccessRightService service;

    private IAccessRightRepository arRepo;

    private IAccessGroupService agService;

    private IDatasetService dsService;

    private IPublisher eventPublisher;

    private AccessGroup AG1;

    private AccessGroup AG2;

    private Dataset DS1;

    private Dataset DS2;

    private User USER1;

    private User USER2;

    private AccessRight GAR11;

    private AccessRight GAR22;

    private AccessRight GAR12;

    private AccessRight GAR21;

    @Before
    public void init() {
        arRepo = Mockito.mock(IAccessRightRepository.class);
        agService = Mockito.mock(AccessGroupService.class);
        dsService = Mockito.mock(DatasetService.class);
        eventPublisher = Mockito.mock(IPublisher.class);
        service = new AccessRightService(arRepo, agService, dsService, eventPublisher);

        final Model model = Model.build("MODEL", DESC, EntityType.DATASET);
        AG1 = new AccessGroup("AG1");
        AG2 = new AccessGroup("AG2");
        DS1 = new Dataset(model, "PROJECT", "DS1");
        DS2 = new Dataset(model, "PROJECT", "DS2");
        final QualityFilter qf = new QualityFilter(10, 0, QualityLevel.ACCEPTED);
        final AccessLevel al = AccessLevel.FULL_ACCESS;
        GAR11 = new AccessRight(qf, al, DS1, AG1);
        GAR11.setId(3L);
        GAR12 = new AccessRight(qf, al, DS1, AG2);
        GAR21 = new AccessRight(qf, al, DS2, AG1);
        GAR22 = new AccessRight(qf, al, DS2, AG2);
        GAR22.setId(1L);
    }

    @Test
    public void testRetrieveAccessRightsNoArgs() throws EntityNotFoundException {
        final List<AccessRight> expected = new ArrayList<>();
        expected.add(GAR11);
        expected.add(GAR12);
        expected.add(GAR21);
        expected.add(GAR22);
        final Page<AccessRight> pageExpected = new PageImpl<>(expected);
        final Pageable pageable = new PageRequest(0, 10);
        Mockito.when(arRepo.findAll(pageable)).thenReturn(pageExpected);

        final Page<AccessRight> result = service.retrieveAccessRights(null, null, new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertTrue(result.getContent().contains(GAR12));
        Assert.assertTrue(result.getContent().contains(GAR21));
        Assert.assertTrue(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsDSArgs() throws EntityNotFoundException {
        final List<AccessRight> expected = new ArrayList<>();
        expected.add(GAR11);
        expected.add(GAR12);
        final Page<AccessRight> pageExpected = new PageImpl<>(expected);
        final Pageable pageable = new PageRequest(0, 10);
        Mockito.when(arRepo.findAllByDataset(DS1, pageable)).thenReturn(pageExpected);
        Mockito.when(dsService.load(DS1.getIpId())).thenReturn(DS1);

        final Page<AccessRight> result = service.retrieveAccessRights(null, DS1.getIpId(), new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertTrue(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsGroupArgs() throws EntityNotFoundException {
        final List<AccessRight> expected = new ArrayList<>();
        expected.add(GAR11);
        expected.add(GAR21);
        final Page<AccessRight> pageExpected = new PageImpl<>(expected);
        final Pageable pageable = new PageRequest(0, 10);
        Mockito.when(arRepo.findAllByAccessGroup(AG1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.retrieveAccessGroup(AG1.getName())).thenReturn(AG1);

        final Page<AccessRight> result = service.retrieveAccessRights(AG1.getName(), null, new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertTrue(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test
    public void testRetrieveAccessRightsFullArgs() throws EntityNotFoundException {
        final List<AccessRight> expected = new ArrayList<>();
        expected.add(GAR11);
        final Page<AccessRight> pageExpected = new PageImpl<>(expected);
        final Pageable pageable = new PageRequest(0, 10);
        Mockito.when(arRepo.findAllByAccessGroupAndDataset(AG1, DS1, pageable)).thenReturn(pageExpected);
        Mockito.when(agService.retrieveAccessGroup(AG1.getName())).thenReturn(AG1);
        Mockito.when(dsService.load(DS1.getIpId())).thenReturn(DS1);

        final Page<AccessRight> result = service.retrieveAccessRights(AG1.getName(), DS1.getIpId(),
                                                                      new PageRequest(0, 10));
        Assert.assertTrue(result.getContent().contains(GAR11));
        Assert.assertFalse(result.getContent().contains(GAR12));
        Assert.assertFalse(result.getContent().contains(GAR21));
        Assert.assertFalse(result.getContent().contains(GAR22));
    }

    @Test(expected = EntityNotFoundException.class)
    public void testUpdateAccessRightNotFound() throws RabbitMQVhostException, ModuleException {
        Mockito.when(arRepo.findOne(3L)).thenReturn(null);
        service.updateAccessRight(3L, GAR22);
    }

    @Test(expected = EntityInconsistentIdentifierException.class)
    public void testUpdateAccessRightInconsistentId() throws RabbitMQVhostException, ModuleException {
        Mockito.when(arRepo.findById(3L)).thenReturn(GAR11);
        service.updateAccessRight(3L, GAR22);
    }
}
