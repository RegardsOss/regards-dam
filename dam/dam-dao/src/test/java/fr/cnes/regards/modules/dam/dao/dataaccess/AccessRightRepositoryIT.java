/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.dam.dao.dataaccess;

import java.time.OffsetDateTime;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractDaoTransactionalTest;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessgroup.AccessGroup;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessLevel;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.AccessRight;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.QualityFilter;
import fr.cnes.regards.modules.dam.domain.dataaccess.accessright.QualityLevel;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.model.dao.IModelRepository;
import fr.cnes.regards.modules.model.domain.Model;

/**
 *
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=dam_ar_dao" })
public class AccessRightRepositoryIT extends AbstractDaoTransactionalTest {

    @Autowired
    private IAccessRightRepository repo;

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

    private Dataset ds1;

    private Dataset ds2;

    private AccessRight ar1;

    private AccessRight ar2;

    @Before
    public void init() {
        OffsetDateTime now = OffsetDateTime.now();
        qf = new QualityFilter(10, 0, QualityLevel.ACCEPTED);
        Model model = Model.build("model1", "desc", EntityType.DATASET);
        model = modelRepo.save(model);
        ds1 = new Dataset(model, "PROJECT", "ds1", "ds1");
        ds1.setLabel("label");
        ds1.setLicence("licence");
        ds1.setCreationDate(now);
        ds1 = dsRepo.save(ds1);
        ds2 = new Dataset(model, "PROJECT", "ds2", "ds2");
        ds2.setLabel("label");
        ds2.setLicence("licence");
        ds2.setCreationDate(now);
        ds2 = dsRepo.save(ds2);

        ag1 = new AccessGroup(ag1Name);
        ag1 = agRepo.save(ag1);
        ar1 = new AccessRight(qf, al, ds1, ag1);
        ar1 = repo.save(ar1);
        ag2 = new AccessGroup(ag2Name);
        ag2 = agRepo.save(ag2);
        ar2 = new AccessRight(qf, al, ds2, ag2);
        ar2 = repo.save(ar2);
    }

    @Test
    public void testfindAllByAccessGroupName() {
        Page<AccessRight> response = repo.findAllByAccessGroup(ag1, PageRequest.of(0, 10));
        Assert.assertTrue(response.getContent().contains(ar1));
        Assert.assertFalse(response.getContent().contains(ar2));
    }

    @Test
    public void testfindAllByDataset() {
        Page<AccessRight> response = repo.findAllByDataset(ds1, PageRequest.of(0, 10));
        Assert.assertTrue(response.getContent().contains(ar1));
        Assert.assertFalse(response.getContent().contains(ar2));
    }

    @Test
    public void testfindAllByAccessGroupNameByDataset() {
        Page<AccessRight> response = repo.findAllByAccessGroupAndDataset(ag1, ds1, PageRequest.of(0, 10));
        Assert.assertTrue(response.getContent().contains(ar1));
        Assert.assertFalse(response.getContent().contains(ar2));

        response = repo.findAllByAccessGroupAndDataset(ag1, ds2, PageRequest.of(0, 10));
        Assert.assertFalse(response.hasContent());
    }

}
