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
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.IAbstractEntityRepository;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.deleted.IDeletedEntityRepository;
import fr.cnes.regards.modules.entities.domain.AbstractEntity;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelAttrAssocService;
import fr.cnes.regards.modules.models.service.IModelService;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
public class CollectionServiceTest {

    private Model pModel1;

    private Model pModel2;

    private Collection collection1;

    private Collection collection2;

    private Collection collection3;

    private Collection collection4;

    private UniformResourceName collection2URN;

    private ICollectionRepository collectionRepositoryMocked;

    private ICollectionService collectionServiceMocked;

    private IAbstractEntityRepository<AbstractEntity<?>> entitiesRepositoryMocked;

    /**
     * initialize the repo before each test
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
    public void init() {

        // populate the repository
        pModel1 = new Model();
        pModel1.setId(1L);
        pModel2 = new Model();
        pModel2.setId(2L);

        collection1 = new Collection(pModel1, "PROJECT", "collection1");
        collection1.setId(1L);
        collection2 = new Collection(pModel2, "PROJECT", "collection2");
        collection2.setId(2L);
        collection3 = new Collection(pModel2, "PROJECT", "collection3");
        collection3.setId(3L);
        collection4 = new Collection(pModel2, "PROJECT", "collection4");
        collection4.setId(4L);
        collection2URN = collection2.getIpId();
        Set<String> collection1Tags = collection1.getTags();
        collection1Tags.add(collection2URN.toString());
        Set<String> collection2Tags = collection2.getTags();
        collection2Tags.add(collection1.getIpId().toString());
        collection2.setTags(collection2Tags);

        // create a mock repository
        collectionRepositoryMocked = Mockito.mock(ICollectionRepository.class);
        Mockito.when(collectionRepositoryMocked.findOne(collection1.getId())).thenReturn(collection1);
        Mockito.when(collectionRepositoryMocked.findOne(collection2.getId())).thenReturn(collection2);
        Mockito.when(collectionRepositoryMocked.findOne(collection3.getId())).thenReturn(collection3);

        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        List<AbstractEntity<?>> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(collection1);
        Mockito.when(entitiesRepositoryMocked.findByTags(collection2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);
        Mockito.when(entitiesRepositoryMocked.findOne(collection1.getId())).thenReturn((AbstractEntity) collection1);
        Mockito.when(entitiesRepositoryMocked.findOne(collection2.getId())).thenReturn((AbstractEntity) collection2);
        Mockito.when(entitiesRepositoryMocked.findOne(collection3.getId())).thenReturn((AbstractEntity) collection3);

        IModelAttrAssocService pModelAttributeService = Mockito.mock(IModelAttrAssocService.class);
        IModelService pModelService = Mockito.mock(IModelService.class);
        IDeletedEntityRepository deletedEntityRepositoryMocked = Mockito.mock(IDeletedEntityRepository.class);

        IPublisher publisherMocked = Mockito.mock(IPublisher.class);

        IRuntimeTenantResolver runtimeTenantResolver = Mockito.mock(IRuntimeTenantResolver.class);
        Mockito.when(runtimeTenantResolver.getTenant()).thenReturn("Tenant");

        collectionServiceMocked = new CollectionService(pModelAttributeService, entitiesRepositoryMocked, pModelService,
                deletedEntityRepositoryMocked, collectionRepositoryMocked, null, null, publisherMocked,
                runtimeTenantResolver, null);
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_COL_010")
    @Requirement("REGARDS_DSL_SYS_ARC_400")
    @Purpose("REGARDS allows to create a collection based on a model - The collection identifier is an URN")
    public void createCollection() throws ModuleException, IOException {
        Mockito.when(collectionRepositoryMocked.save(collection2)).thenReturn(collection2);
        final Collection collection = collectionServiceMocked.create(collection2);
        Assert.assertEquals(collection2, collection);
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_450")
    @Purpose("The URN identifier of a collection is uniq")
    public void collectionUrnUnicity() throws ModuleException, IOException {

        String collName = "collection1";

        Collection coll1 = new Collection(pModel1, "PROJECT", collName);
        Collection coll2 = new Collection(pModel1, "PROJECT", collName);

        Assert.assertNotNull(coll1);
        Assert.assertNotNull(coll2);
        Assert.assertNotNull(coll1.getIpId());
        Assert.assertNotNull(coll2.getIpId());
        Assert.assertNotEquals(coll1.getIpId(), coll2.getIpId());
    }

}
