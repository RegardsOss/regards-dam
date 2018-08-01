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
package fr.cnes.regards.modules.entities.dao;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=projectdb" })
public class CollectionTest extends AbstractDaoTransactionalTest {

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private IModelRepository modelRepository;

    @Test
    public void test() {
        Model model1 = new Model();
        model1.setName("Model1");
        model1.setType(EntityType.COLLECTION);
        model1 = modelRepository.save(model1);
        Collection coll = new Collection(model1, "PROJECT", "coll");
        coll.setProviderId("IpID");
        Collection collection1 = collectionRepository.save(coll);
        Collection collection2 = collectionRepository.findOne(collection1.getId());
        Assert.assertEquals(collection1, collection2);
    }

    @Test
    public void searchByLabel() {
        // Fake model
        Model model1 = new Model();
        model1.setName("Model1");
        model1.setType(EntityType.COLLECTION);
        model1 = modelRepository.save(model1);

        String colBaseName = "col0";

        // Create collections
        List<Collection> cols = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            Collection coll = new Collection(model1, "PROJECT", colBaseName + i);
            coll.setCreationDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
            cols.add(coll);
        }

        Collection coll = new Collection(model1, "PROJECT", "nomatch");
        coll.setCreationDate(OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC));
        cols.add(coll);

        collectionRepository.save(cols);

        // Search
        EntitySpecifications<Collection> specs = new EntitySpecifications<>();
        List<Collection> collections = collectionRepository.findAll(specs.search("toto"));
        Assert.assertTrue(collections.isEmpty());

        collections = collectionRepository.findAll(specs.search("col"));
        Assert.assertFalse(collections.isEmpty());
        Assert.assertTrue(collections.size() == 3);
        Assert.assertTrue(collections.get(0).getLabel().endsWith("1"));
        Assert.assertTrue(collections.get(1).getLabel().endsWith("2"));
        Assert.assertTrue(collections.get(2).getLabel().endsWith("3"));

        collections = collectionRepository.findAll(specs.search("nomatch"));
        Assert.assertTrue(collections.size() == 1);
    }

}
