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
package fr.cnes.regards.modules.indexer.service;

import static fr.cnes.regards.modules.indexer.service.GeoUtil.toWgs84;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.junit.After;
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
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.utils.RsRuntimeException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.indexer.service.test.SearchConfiguration;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author oroussel
 */
@Ignore
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SearchConfiguration.class })
public class PostgisVsElasticSearchTest {

    @Autowired
    private IEsRepository repos;

    private static final String TENANT = "postgis";

    private final NumberFormat format = DecimalFormat.getInstance();

    @Autowired
    private Gson gson;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private Connection ctx;

    private PreparedStatement distancePstmt;

    @Before
    public void setup() throws TransformException, SQLException {
        tenantResolver.forceTenant(TENANT);

        Properties props = new Properties();
        props.put("user", "root");
        props.put("password", "root");
        ctx = DriverManager.getConnection("jdbc:postgresql://localhost:5442/postgres", props);

        distancePstmt = ctx
                .prepareStatement("SELECT ST_Distance(ST_SetSRID(geometry::geography, 949900), ST_SetSRID(ST_Point(-144.0, -78.0)::geography, 949900), true) FROM s1_geo WHERE id = ?");
    }

    @After
    public void tearDown() throws SQLException {
        ctx.close();
    }

    private double getDistance(int id) throws SQLException {
        distancePstmt.setInt(1, id);
        try (ResultSet rset = distancePstmt.executeQuery()) {
            if (rset.next()) {
                return rset.getDouble(1);
            } else {
                return -1;
            }
        }
    }

    // private static int[] BULK_SIZES = new int[] { 100, 200, 400, 800, 1600, 3200, 6400, 10_000 };
    private static int[] BULK_SIZES = new int[] { 800 };

    @Test
    public void fillEs() throws SQLException, TransformException, InterruptedException {
        if (!repos.indexExists(TENANT)) {
            repos.createIndex(TENANT);
            repos.setGeometryMapping(TENANT, EntityType.DATA.toString());
            repos.setAutomaticDoubleMapping(TENANT, EntityType.DATA.toString());
            repos.setSettingsForBulk(TENANT);

            Model model = new Model();
            model.setName("Data model");

            long start = System.currentTimeMillis();
            List<DataObject> dos = new ArrayList<>();
            int count = 0;

            int i = 0;
            int bulkSize = BULK_SIZES[i];

            try (PreparedStatement pstmt = ctx
                    .prepareStatement("SELECT id, title, ST_AsGeoJSON(geometry) FROM s1_geo WHERE id <= 100000 ORDER BY id")) {
                for (ResultSet rset = pstmt.executeQuery(); rset.next();) {
                    // System.out.printf("%d, %s, %s\n", rset.getInt(1), rset.getString(2), rset.getString(3));
                    IGeometry geometry = gson.fromJson(rset.getString(3), IGeometry.class);
                    DataObject object = new DataObject(model, TENANT, rset.getString(2), rset.getString(2));
                    object.setId((long) rset.getInt(1));
                    object.setIpId(new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, TENANT,
                            UUID.randomUUID(), 1));
                    geometry.setCrs(Crs.MARS_49900.toString());
                    object.setGeometry(geometry);
                    object.setWgs84(toWgs84(geometry));
                    dos.add(object);

                    if (dos.size() >= bulkSize) {
                        int saved = 0;
                        try {
                            long now = System.currentTimeMillis();
                            saved = repos.saveBulk(TENANT, dos);
                            long duration = System.currentTimeMillis() - now;
                            System.out
                                    .printf("Bulk size: %d, saved objects: %d, time: %d ms, time rate: %f ms per object\n",
                                            bulkSize, saved, duration, duration / (double) saved);
                        } catch (RsRuntimeException e) {
                            System.out.println("Time out !");
                            repos.refresh(TENANT);
                            System.out.println("Waiting 2 mn...");
                            Thread.sleep(120_000);
                            System.out.println("Trying again...");
                            long now = System.currentTimeMillis();
                            saved = repos.saveBulk(TENANT, dos);
                            long duration = System.currentTimeMillis() - now;
                            System.out
                                    .printf("Bulk size: %d, saved objects: %d, time: %d ms, time rate: %f ms per object\n",
                                            bulkSize, saved, duration, duration / (double) saved);
                        }
                        i = (i + 1) % BULK_SIZES.length;
                        bulkSize = BULK_SIZES[i];
                        count += dos.size();
                        System.out.println("Saved " + count + "(" + ((System.currentTimeMillis() - start) / 1000)
                                + ") s");
                        dos.clear();
                    }
                }
                if (!dos.isEmpty()) {
                    try {
                        repos.saveBulk(TENANT, dos);
                    } catch (RsRuntimeException e) {
                        System.out.println("Time out !");
                        repos.refresh(TENANT);
                        repos.saveBulk(TENANT, dos);
                    }
                    count += dos.size();
                    System.out.println("Saved " + count + "(" + ((System.currentTimeMillis() - start) / 1000) + " s");
                    dos.clear();
                }
            }
            ctx.close();
            repos.unsetSettingsForBulk(TENANT);
        }
    }

    @Test
    public void testCircleOnMars() throws SQLException {
        List<Integer> postgisIds = new ArrayList<>();
        // With Postgis
        try (PreparedStatement pstmt = ctx.prepareStatement(
                                                            // "SELECT id FROM s1_geo WHERE
                                                            // ST_DWithin(geometry::geography, "
                                                            // + "ST_SetSRID(ST_Point(-144.0, -78.0),
                                                            // 949900)::geography, 50000) AND id <= 100000;"
                                                            "SELECT id FROM s1_geo WHERE ST_Distance(geometry::geography, "
                                                                    + "ST_SetSRID(ST_Point(-144.0, -78.0)::geography, 949900), true) <= 50000 AND id <= 100000")) {
            for (ResultSet rset = pstmt.executeQuery(); rset.next();) {
                postgisIds.add(rset.getInt(1));
            }
        }

        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.MARS_49900);
        searchKey.setSearchIndex(TENANT);
        double[] center = new double[] { -144.0, -78.0 };
        ICriterion circleCrit = ICriterion.intersectsCircle(center, "50km");
        List<Integer> esIds = new ArrayList<>();
        Page<DataObject> resultPage = repos.search(searchKey, 1000, circleCrit);
        for (DataObject object : resultPage.getContent()) {
            esIds.add(object.getId().intValue());
        }

        int pgCount = postgisIds.size();
        ArrayList<Integer> esIdsCopy = new ArrayList<>(esIds);
        esIdsCopy.removeAll(postgisIds);
        postgisIds.removeAll(esIds);
        if (!postgisIds.isEmpty()) {
            System.out.println("In Postgis results, not In ES");
            for (Iterator<Integer> i = postgisIds.iterator(); i.hasNext();) {
                Integer id = i.next();
                double trueDistance = GeoHelper.getDistance(
                                                            repos.search(searchKey, 1, ICriterion.eq("id", id))
                                                                    .getContent().get(0).getGeometry(),
                                                            center, Crs.MARS_49900);
                if (trueDistance > 50_000.0) {
                    System.out
                            .printf("id: %d, false positive returned by Postgis (%f m) while distance computed with GeoTools is %f m\n",
                                    id, getDistance(id), trueDistance);
                    i.remove();
                }
            }
        }
        System.out.println("In ES results, not In PG");
        if (!esIdsCopy.isEmpty()) {
            for (Integer id : esIdsCopy) {
                System.out.printf("id: %d, distance computed with postgis: %f\n", id, getDistance(id));
            }
        }

        Assert.assertTrue(String.format("PG found %d results, ES %d, %d by PG and not by ES, %d by ES and not by PG",
                                        pgCount, esIds.size(), postgisIds.size(), esIdsCopy.size()),
                          postgisIds.isEmpty() && esIdsCopy.isEmpty());

    }

    @Test
    public void testPolygonOnMars() throws SQLException {
        List<Integer> postgisIds = new ArrayList<>();
        // With Postgis
        try (PreparedStatement pstmt = ctx
                .prepareStatement("SELECT id FROM s1_geo WHERE ST_Intersects(geometry::geography, "
                        + "ST_Polygon(ST_GeomFromText('LINESTRING(-146.0 -78.0, -146.0 -77.0, -148.0 -77.0, "
                        + "-148.0 -78.0, -146.0 -78.0)'), 949900)::geography) AND id <= 100000")) {
            for (ResultSet rset = pstmt.executeQuery(); rset.next();) {
                postgisIds.add(rset.getInt(1));
            }
        }

        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.MARS_49900);
        searchKey.setSearchIndex(TENANT);
        double[][][] polygon = new double[][][] {
                { { -146.0, -78.0 }, { -146.0, -77.0 }, { -148.0, -77.0 }, { -148.0, -78.0 }, { -146.0, -78.0 } } };
        ICriterion polygonCrit = ICriterion.intersectsPolygon(polygon);
        List<Integer> esIds = new ArrayList<>();
        Page<DataObject> resultPage = repos.search(searchKey, 1000, polygonCrit);
        for (DataObject object : resultPage.getContent()) {
            esIds.add(object.getId().intValue());
        }

        int pgCount = postgisIds.size();
        ArrayList<Integer> esIdsCopy = new ArrayList<>(esIds);
        esIdsCopy.removeAll(postgisIds);
        postgisIds.removeAll(esIds);
        System.out.println("In Postgis results, not In ES");
        for (Integer id : postgisIds) {
            System.out.printf("id: %d\n", id);
        }
        System.out.println("In ES results, not In PG");
        for (Integer id : esIdsCopy) {
            System.out.printf("id: %d\n", id);
        }

        Assert.assertTrue(String.format("PG found %d results, ES %d, %d by PG and not by ES, %d by ES and not by PG",
                                        pgCount, esIds.size(), postgisIds.size(), esIdsCopy.size()),
                          postgisIds.isEmpty() && esIdsCopy.isEmpty());

    }
}
