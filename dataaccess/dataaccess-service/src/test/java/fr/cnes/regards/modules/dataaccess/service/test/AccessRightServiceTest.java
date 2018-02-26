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
package fr.cnes.regards.modules.dataaccess.service.test;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import fr.cnes.regards.framework.amqp.IInstancePublisher;
import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.amqp.ISubscriber;
import fr.cnes.regards.framework.amqp.exception.RabbitMQVhostException;
import fr.cnes.regards.framework.module.rest.exception.EntityInconsistentIdentifierException;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.multitenant.ITenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.dataaccess.dao.IAccessRightRepository;
import fr.cnes.regards.modules.dataaccess.domain.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessLevel;
import fr.cnes.regards.modules.dataaccess.domain.accessright.AccessRight;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityFilter;
import fr.cnes.regards.modules.dataaccess.domain.accessright.QualityLevel;
import fr.cnes.regards.modules.dataaccess.service.AccessGroupService;
import fr.cnes.regards.modules.dataaccess.service.IAccessGroupService;
import fr.cnes.regards.modules.dataaccess.service.IAccessRightService;
import fr.cnes.regards.modules.entities.dao.IDocumentLSRepository;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.service.CollectionService;
import fr.cnes.regards.modules.entities.service.DatasetService;
import fr.cnes.regards.modules.entities.service.IDatasetService;
import fr.cnes.regards.modules.entities.service.IDocumentService;
import fr.cnes.regards.modules.entities.service.IEntitiesService;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.models.dao.IAttributeModelRepository;
import fr.cnes.regards.modules.models.dao.IAttributePropertyRepository;
import fr.cnes.regards.modules.models.dao.IFragmentRepository;
import fr.cnes.regards.modules.models.dao.IRestrictionRepository;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author Sylvain Vissiere-Guerinet
 *
 */
@RunWith(SpringRunner.class)
@DirtiesContext
public class AccessRightServiceTest {

    private static final String DESC = "Desc";

    @Autowired
    private IAccessRightService service;

    @Autowired
    private IAccessRightRepository arRepo;

    @Autowired
    private IAccessGroupService agService;

    @Autowired
    private IDatasetService dsService;

    private AccessGroup AG1;

    private AccessGroup AG2;

    private Dataset DS1;

    private Dataset DS2;

    private AccessRight GAR11;

    private AccessRight GAR22;

    private AccessRight GAR12;

    private AccessRight GAR21;

    @Configuration
    @ComponentScan(basePackages = { "fr.cnes.regards.modules.dataaccess" })
    static class Conf {

        @Bean
        public IAccessRightRepository accessRightRepository() {
            return Mockito.mock(IAccessRightRepository.class);
        }

        @Bean
        public IAccessGroupService accessGroupService() {
            return Mockito.mock(AccessGroupService.class);
        }

        @Bean
        public IDatasetService datasetService() {
            return Mockito.mock(DatasetService.class);
        }

        @Bean
        public IPublisher publisher() {
            return Mockito.mock(IPublisher.class);
        }

        @Bean
        public ISubscriber subscriber() {
            return Mockito.mock(ISubscriber.class);
        }

        @Bean
        public ITenantResolver tenantResolver() {
            return Mockito.mock(ITenantResolver.class);
        }

        @Bean
        public IRuntimeTenantResolver runtimeTenantResolver() {
            return Mockito.mock(IRuntimeTenantResolver.class);
        }

        @Bean
        public IInstancePublisher instancePublisher() {
            return Mockito.mock(IInstancePublisher.class);
        }

        @Bean
        public CollectionService collectionService() {
            return Mockito.mock(CollectionService.class);
        }

        @Bean
        public IAttributeModelRepository attributeModelRepository() {
            return Mockito.mock(IAttributeModelRepository.class);
        }

        @Bean
        public IRestrictionRepository restrictionRepository() {
            return Mockito.mock(IRestrictionRepository.class);
        }

        @Bean
        public IFragmentRepository fragmentRepository() {
            return Mockito.mock(IFragmentRepository.class);
        }

        @Bean
        public IAttributePropertyRepository attributePropertyRepository() {
            return Mockito.mock(IAttributePropertyRepository.class);
        }

        @Bean
        public IDocumentLSRepository documentLSRepository() {
            return Mockito.mock(IDocumentLSRepository.class);
        }

        @Bean
        public IModelService modelService() {
            return Mockito.mock(IModelService.class);
        }

        @Bean
        public IDocumentService documentService() {
            return Mockito.mock(IDocumentService.class);
        }

        @Bean
        public IEntitiesService entitiesService() {
            return Mockito.mock(IEntitiesService.class);
        }

        @Bean
        public IPluginService pluginService() {
            return Mockito.mock(IPluginService.class);
        }

        @Bean
        public Gson gson() {
            return new GsonBuilder().create();
        }

        @Bean
        public IEsRepository esRepository() {
            return Mockito.mock(IEsRepository.class);
        }

        @Bean
        public CacheManager cacheManager() {
            return new SimpleCacheManager();
        }
    }

    @Before
    public void init() {
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
