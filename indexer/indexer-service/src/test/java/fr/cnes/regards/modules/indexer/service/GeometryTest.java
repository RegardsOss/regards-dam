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
package fr.cnes.regards.modules.indexer.service;

import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.OaisUniformResourceName;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.service.test.SearchConfiguration;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * @author oroussel
 */
@Ignore
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SearchConfiguration.class })
public class GeometryTest {

    @Autowired
    private IEsRepository repos;

    private static final String TENANT = "geoshape";

    @Autowired
    private Gson gson;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private Model model;

    private final SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);

    @Before
    public void setup() throws TransformException, SQLException, IOException {
        tenantResolver.forceTenant(TENANT);
        searchKey.setSearchIndex(TENANT);

        model = new Model();
        model.setName("Wgs84 model");

        if (repos.indexExists(TENANT)) {
            repos.deleteIndex(TENANT);
        }

        repos.createIndex(TENANT);
    }

    private DataObject createDataObject(String label, IGeometry shape) {
        DataObject object = new DataObject(model, TENANT, label, label);
        object.setIpId(new OaisUniformResourceName(OAISIdentifier.SIP, EntityType.DATA, TENANT, UUID.randomUUID(), 1));
        object.setNormalizedGeometry(GeoHelper.normalize(shape));
        object.setWgs84(GeoHelper.normalize(shape));
        return object;
    }

    @Test
    public void lineStringTest() {
        repos.save(TENANT, createDataObject("LS1", IGeometry.lineString(90, 45, 100, 45)));
        repos.refresh(TENANT);

        ICriterion crit = ICriterion.intersectsBbox(94, 40, 96, 60);
        Page<DataObject> page = repos.search(searchKey, 100, crit);
        Assert.assertEquals(1, page.getTotalElements());

        repos.save(TENANT, createDataObject("LS2", IGeometry.lineString(350, 45, 10, 45)));
        repos.refresh(TENANT);

        crit = ICriterion.intersectsBbox(40, 40, 50, 60);
        page = repos.search(searchKey, 100, crit);
        Assert.assertEquals(0, page.getTotalElements());

        // repos.save(TENANT, createDataObject("LS2", IGeometry.lineString(170, 45, -170, 45)));
        // repos.refresh(TENANT);
        //
        // crit = ICriterion.intersectsBbox(171, 40, 175, 60);
        // page = repos.search(searchKey, 100, crit);
        // Assert.assertEquals(1, page.getTotalElements());
        // Assert.assertEquals("LS2", page.getContent().get(0).getLabel());
        // Assert.assertTrue("LS2 should have been transformed into MultiLineString",
        // page.getContent().get(0).getNormalizedGeometry() instanceof MultiLineString);
        // Assert.assertTrue("LS2 should have been transformed into MultiLineString",
        // page.getContent().get(0).getWgs84() instanceof MultiLineString);

    }
}
