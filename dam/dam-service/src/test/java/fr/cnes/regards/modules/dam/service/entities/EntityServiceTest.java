/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.service.entities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.entities.IAbstractEntityRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.Document;
import fr.cnes.regards.modules.dam.domain.models.Model;

/**
 * @author Sylvain Vissiere-Guerinet
 */
public class EntityServiceTest {

    private IAbstractEntityRepository<AbstractEntity<?>> entitiesRepositoryMocked;

    private Collection collection2;

    private Collection collection3;

    private Collection collection4;

    private DataObject data;

    private Document doc;

    private Dataset dataset;

    private Dataset dataset2;

    private Model model2;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Before
    public void init() {

        // populate the repository
        model2 = new Model();
        model2.setId(2L);

        collection2 = new Collection(model2, "PROJECT", "COL2", "collection2");
        collection2.setId(2L);
        collection3 = new Collection(model2, "PROJECT", "COL3", "collection3");
        collection3.setId(3L);
        collection3.setLabel("pName3");
        collection4 = new Collection(model2, "PROJECT", "COL4", "collection4");
        collection4.setId(4L);
        collection2.addTags(collection4.getIpId().toString());

        data = new DataObject(new Model(), "PROJECT", "OBJ1", "object");
        data.setId(1L);
        doc = new Document(model2, "PROJECT", "DOC1", "doc");
        doc.setId(2L);
        dataset = new Dataset(model2, "PROJECT", "DS1", "dataset");
        dataset.setLicence("licence");
        dataset.setId(3L);
        dataset.setLabel("dataset");
        dataset2 = new Dataset(model2, "PROJECT", "DS2", "dataset2");
        dataset2.setLicence("licence");

        // IModelAttrAssocService pModelAttributeService = Mockito.mock(IModelAttrAssocService.class);
        // IModelService pModelService = Mockito.mock(IModelService.class);

        entitiesRepositoryMocked = Mockito.mock(IAbstractEntityRepository.class);
        List<AbstractEntity<?>> findByTagsValueCol2IpId = new ArrayList<>();
        findByTagsValueCol2IpId.add(collection4);
        Mockito.when(entitiesRepositoryMocked.findByTags(collection2.getIpId().toString()))
                .thenReturn(findByTagsValueCol2IpId);

        // EntityManager emMocked = Mockito.mock(EntityManager.class);

        // IPublisher publisherMocked = Mockito.mock(IPublisher.class);
        IRuntimeTenantResolver runtimeTenantResolver = Mockito.mock(IRuntimeTenantResolver.class);
        Mockito.when(runtimeTenantResolver.getTenant()).thenReturn("Tenant");

        Mockito.when(entitiesRepositoryMocked.findById(1L)).thenReturn(Optional.of(data));
        Mockito.when(entitiesRepositoryMocked.findById(2L)).thenReturn(Optional.of(doc));
        Mockito.when(entitiesRepositoryMocked.findById(3L)).thenReturn(Optional.of(dataset));
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_400")
    @Requirement("REGARDS_DSL_SYS_ARC_420")
    @Purpose("A document identifier is an URN")
    public void testAssociateDatasetToAnything() {
        List<AbstractEntity<?>> entityList = new ArrayList<>();
        entityList.add(collection3);
        entityList.add(dataset2);
        entityList.add(data);
        entityList.add(doc);
        Set<UniformResourceName> entityURNList = new HashSet<>();
        entityURNList.add(collection3.getIpId());
        entityURNList.add(dataset2.getIpId());
        entityURNList.add(data.getIpId());
        entityURNList.add(doc.getIpId());
        Mockito.when(entitiesRepositoryMocked.findByIpIdIn(entityURNList)).thenReturn(entityList);

        // TODO
        // entityServiceMocked.associate(dataset, entityURNList);
        Assert.assertFalse(dataset.getTags().contains(collection3.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(dataset2.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(data.getIpId().toString()));
        Assert.assertFalse(dataset.getTags().contains(doc.getIpId().toString()));
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_450")
    @Purpose("The URN identifier of a document is unique")
    public void documentUrnUnicity() throws ModuleException, IOException {
        String docName = "un document";

        Document document1 = new Document(model2, "PROJECT", docName, docName);
        Document document2 = new Document(model2, "PROJECT", docName, docName);

        Assert.assertNotNull(document1);
        Assert.assertNotNull(document2);
        Assert.assertNotNull(document1.getIpId());
        Assert.assertNotNull(document2.getIpId());
        Assert.assertNotEquals(document1.getIpId(), document2.getIpId());
    }

    @Test
    @Requirement("REGARDS_DSL_SYS_ARC_450")
    @Purpose("The URN identifier of a data object is uniq")
    public void dataUrnUnicity() throws ModuleException, IOException {
        String dataObjectName = "un document";

        DataObject document1 = new DataObject(model2, "PROJECT", dataObjectName, dataObjectName);
        DataObject document2 = new DataObject(model2, "PROJECT", dataObjectName, dataObjectName);

        Assert.assertNotNull(document1);
        Assert.assertNotNull(document2);
        Assert.assertNotNull(document1.getIpId());
        Assert.assertNotNull(document2.getIpId());
        Assert.assertNotEquals(document1.getIpId(), document2.getIpId());
    }

}