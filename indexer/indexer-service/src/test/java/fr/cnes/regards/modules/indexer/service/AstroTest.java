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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.hipparchus.util.FastMath;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import fr.cnes.regards.framework.geojson.coordinates.PolygonPositions;
import fr.cnes.regards.framework.geojson.coordinates.Position;
import fr.cnes.regards.framework.geojson.coordinates.Positions;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
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
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SearchConfiguration.class })
public class AstroTest {

    @Autowired
    private IEsRepository repos;

    private static final String TENANT = "astro";

    private NumberFormat format = DecimalFormat.getInstance();

    @Autowired
    private Gson gson;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private Model model;

    private Connection ctx;

    private PreparedStatement distancePstmt;

    @Before
    public void setup() throws TransformException, SQLException, IOException {
        tenantResolver.forceTenant(TENANT);
        model = new Model();
        model.setName("Astro model");

        if (!repos.indexExists(TENANT)) {
            repos.createIndex(TENANT);
            repos.setGeometryMapping(TENANT, EntityType.DATA.toString());
            repos.setAutomaticDoubleMapping(TENANT, EntityType.DATA.toString());
            repos.setSettingsForBulk(TENANT);

            fillConstellations();
        }

        Properties props = new Properties();
        props.put("user", "root");
        props.put("password", "root");
        ctx = DriverManager.getConnection("jdbc:postgresql://localhost:5442/postgres", props);

        distancePstmt = ctx.prepareStatement("SELECT ST_AsGeoJSON(ST_Buffer(ST_GeomFromGeoJSON(?), 0.0))");

    }

    //    private IGeometry getCorrectedPolygon(IGeometry polygon) throws SQLException {
    //        distancePstmt.setString(1, gson.toJson(polygon));
    //        try (ResultSet rset = distancePstmt.executeQuery()) {
    //            if (rset.next()) {
    //                return gson.fromJson(rset.getString(1), IGeometry.class);
    //            } else {
    //                return null;
    //            }
    //        }
    //    }

    /**
     * Shit polygons concerning some constellations : Ursa Minor, Ursa Major, Pisces, Pegasus, Octans, Hydra, Crux,
     * Corvus, Cepheus.
     * It's better to not saving them because Postgis corrected polygons are worst
     */
    public void fillConstellations() throws IOException, TransformException, SQLException {
        try (BufferedWriter bw = new BufferedWriter(
                new FileWriter("/home/oroussel/Téléchargements/REGARDS/GEOJSON/constellations.json"))) {
            bw.write("{\n" + "  \"type\": \"FeatureCollection\",\n" + "  \"features\": [");

            Map<String, String> constMap = new HashMap<>();
            // Read "key;name" constellations file
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ClassLoader.getSystemResourceAsStream("constellations_names.txt"),
                                          Charset.defaultCharset()))) {
                String line = reader.readLine();
                while (line != null) {
                    String[] keyName = line.split(";");
                    constMap.put(keyName[0], keyName[1]);
                    line = reader.readLine();
                }
            }

            // Read constellations coordinates file
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(ClassLoader.getSystemResourceAsStream("constellations_polygons.txt"),
                                          Charset.defaultCharset()))) {
                // Useful variables
                String curConstellation = null;
                List<double[]> points = new ArrayList<>();
                List<double[]> pointsWgs84 = new ArrayList<>();

                String line = reader.readLine();
                boolean firstConst = true;
                while (line != null) {
                    String[] coordName = line.trim().split("\\s+");
                    double latitude = Double.parseDouble(coordName[1]);
                    double ra = Double.parseDouble(coordName[0]);
                    double longitude_0_360 = (ra / 24.) * 360;
                    double longitude = (longitude_0_360 >= 180.) ? longitude_0_360 - 360.0 : longitude_0_360;
                    double[] wgs84 = GeoHelper.transform(new double[] { longitude, latitude }, Crs.ASTRO, Crs.WGS_84);
                    //                if (wgs84[0] < 0) {
                    //                    wgs84[0] += 360;
                    //                }

                    // Since 2nd line
                    if (curConstellation != null) {
                        // Same constellation as previous one
                        if (curConstellation.equals(coordName[2])) {
                            double[] newPoint = new double[] { longitude, latitude };
                            double[] previousPoint = points.get(0);
                            if (GeoHelper.normalizeNextCoordinate(previousPoint, newPoint)) {
                                points.set(0, previousPoint);
                            }
                            points.add(0, newPoint);

                            double[] newPointWgs84 = new double[] { wgs84[0], wgs84[1] };
                            double[] previousPointWgs84 = pointsWgs84.get(0);
                            if (GeoHelper.normalizeNextCoordinate(previousPointWgs84, newPointWgs84)) {
                                pointsWgs84.set(0, previousPointWgs84);
                            }
                            pointsWgs84.add(0, newPointWgs84);
                        } else { // New constellation on line
                            // Finalize current constellation
                            IGeometry polygon = createPolygonFromPoints(pointsWgs84);
                            IGeometry polygonWgs84 = createPolygonFromPoints(pointsWgs84);

                            if (!firstConst) {
                                bw.write(",");
                            }
                            firstConst = false;
                            bw.write("{\n" + "      \"type\": \"Feature\",\n" + "      \"properties\": {\n"
                                             + "        \"label\": \"");
                            bw.write(constMap.get(curConstellation));
                            bw.write("\"\n" + "      },\n" + "      \"geometry\": ");

                            DataObject object = createDataObject(polygon, polygonWgs84, constMap.get(curConstellation));

                            bw.write(gson.toJson(object.getWgs84(), Polygon.class));
                            bw.write("}\n");
                            try {
                                repos.save(TENANT, object);
                            } catch (RuntimeException e) {
                                System.out.println("Cannot save " + constMap.get(curConstellation));
                                e.printStackTrace();
                            }
                            // Reset variables
                            curConstellation = null;
                            // -----------------------------
                            pointsWgs84.clear();
                            // -----------------------------
                        }
                    }

                    // Init new constellation
                    if (curConstellation == null) {
                        curConstellation = coordName[2];

                        // If first longitude is between -90 and -180, we use 180 -> 270 numeric (assuming it will probably
                        // cross dateline better than 0 line)
                        if ((longitude > -180) && (longitude <= -90)) {
                            longitude += 360;
                        }
                        points.add(new double[] { longitude, latitude });
                        if ((wgs84[0] > -180) && (wgs84[0] <= -90)) {
                            wgs84[0] += 360;
                        }
                        pointsWgs84.add(new double[] { wgs84[0], wgs84[1] });
                        // -----------------------------
                    }
                    line = reader.readLine();
                }
                // Finalize last constellation
                IGeometry polygon = createPolygonFromPoints(pointsWgs84);
                IGeometry polygonWgs84 = createPolygonFromPoints(pointsWgs84);

                bw.write(",\n");
                bw.write("{\n" + "      \"type\": \"Feature\",\n" + "      \"properties\": {\n"
                                 + "        \"label\": \"");
                bw.write(constMap.get(curConstellation));
                bw.write("\"\n" + "      },\n" + "      \"geometry\": ");

                DataObject object = createDataObject(polygon, polygonWgs84, constMap.get(curConstellation));
                bw.write(gson.toJson(object.getWgs84(), Polygon.class));
                bw.write("}\n");

                try {
                    repos.save(TENANT, object);
                } catch (RuntimeException e) {
                    System.out.println("Cannot save " + constMap.get(curConstellation));
                }

                repos.unsetSettingsForBulk(TENANT);
                repos.refresh(TENANT);
            }
            bw.write("  ]\n" + "}");
        }
    }

    private IGeometry createPolygonFromPoints(List<double[]> pointsWgs84) {
        PolygonPositions polyPos = new PolygonPositions();
        Positions poss = new Positions();
        for (double[] p : pointsWgs84) {
            poss.add(new Position(p[0], p[1]));
        }
        poss.add(poss.get(0));
        polyPos.add(poss);
        return IGeometry.polygon(polyPos);
    }

    private DataObject createDataObject(IGeometry shape, IGeometry shapeWgs84, String label) {
        System.out.println("Saving " + label);
        DataObject object = new DataObject(model, TENANT, label);
        object.setIpId(new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, TENANT, UUID.randomUUID(), 1));
        object.setGeometry(shape);
        object.getFeature().setCrs(Crs.ASTRO.toString());
        object.setWgs84(shapeWgs84);

        return object;
    }

    @Test
    public void testLeoMinor() {
        // 0.1 => 0.1 degrees
        ICriterion circleCrit = ICriterion.intersectsCircle(new double[] { 150.0, 35.0 }, "0.1");
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.ASTRO);
        searchKey.setSearchIndex(TENANT);
        Page<DataObject> page = repos.search(searchKey, 10, circleCrit);
        Assert.assertEquals(1, page.getTotalElements());

        // With 1 degree => Leo and Leo Minor

        //        TESTER AVEC MIZAR

        // With 90°, a lot
        circleCrit = ICriterion.intersectsCircle(new double[] { 150.0, 35.0 }, "90");
        page = repos.search(searchKey, 100, circleCrit);
        Assert.assertEquals(46, page.getTotalElements());
    }

    @Test
    public void testCircleAroundNorthPole() {
        // Circle centered on north pole with 7° angle => should return Camelopardis, Draco
        ICriterion circleCrit = ICriterion.intersectsCircle(new double[] { 0.0, 90.0 }, "7");
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.ASTRO);
        searchKey.setSearchIndex(TENANT);
        Page<DataObject> page = repos.search(searchKey, 100, circleCrit);
        Assert.assertEquals(4, page.getTotalElements());
        List constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Camelopardis"));
        Assert.assertTrue(constNames.contains("Draco"));
        Assert.assertTrue(constNames.contains("Ursa Minor"));
        Assert.assertTrue(constNames.contains("Cepheus"));

        // + Cassiopea
        circleCrit = ICriterion.intersectsCircle(new double[] { 0.0, 90.0 }, "16");
        page = repos.search(searchKey, 100, circleCrit);
        Assert.assertEquals(3, page.getTotalElements());
        constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Camelopardis"));
        Assert.assertTrue(constNames.contains("Draco"));
        Assert.assertTrue(constNames.contains("Cassiopeia"));

        // Into and Only Draco
        circleCrit = ICriterion.intersectsCircle(new double[] { -112.5, 65.0 }, "5");
        page = repos.search(searchKey, 100, circleCrit);
        Assert.assertEquals(1, page.getTotalElements());
        constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Draco"));

    }

    @Test
    public void testCircleAroundSouthPole() {
        // Circle centered on south pole with 7° angle => should return Camelopardis, Draco
        ICriterion circleCrit = ICriterion.intersectsCircle(new double[] { 0.0, -90.0 }, "7");
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.ASTRO);
        searchKey.setSearchIndex(TENANT);
        Page<DataObject> page = repos.search(searchKey, 100, circleCrit);
        Assert.assertEquals(2, page.getTotalElements());
        List constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Mensa"));
        Assert.assertTrue(constNames.contains("Octans"));
    }

    @Test
    public void testBbox() {
        // BBOx
        ICriterion bboxCrit = ICriterion.intersectsBbox(-167, 40, -158, 45);
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.ASTRO);
        searchKey.setSearchIndex(TENANT);
        Page<DataObject> page = repos.search(searchKey, 100, bboxCrit);
        Assert.assertEquals(1, page.getTotalElements());
        List constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Canes Venatici"));

        // BBox (15h, 60°, 16h, 70°) (into Draco reaching limit with Ursa Minor)
        bboxCrit = ICriterion.intersectsBbox(-120, 60, -105, 69.9);
        page = repos.search(searchKey, 100, bboxCrit);
        Assert.assertEquals(1, page.getTotalElements());
        constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Draco"));
    }

    @Test
    public void testCircleIntoDraco() {
        ICriterion circleCrit = ICriterion.intersectsCircle(new double[] { -112.5, 65.0 }, "5");
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.ASTRO);
        searchKey.setSearchIndex(TENANT);
        Page<DataObject> page = repos.search(searchKey, 100, circleCrit);
        Assert.assertEquals(2, page.getTotalElements());
        List constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Draco"));
        Assert.assertTrue(constNames.contains("Ursa Minor"));

        circleCrit = ICriterion.intersectsCircle(new double[] { -112.5, 65.0 }, "4.9");
        page = repos.search(searchKey, 100, circleCrit);
        Assert.assertEquals(1, page.getTotalElements());
        constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Draco"));
    }

    @Test
    public void testPolygon() {
        // BBox (8h, 50°, 9h, 60°)
        ICriterion crit = ICriterion.intersectsPolygon(new double[][][] {
                { { 120.0, 50.0 }, { 135, 50.0 }, { 135.0, 60.0 }, { 120.0, 60.0 }, { 120.0, 50.0 } } });
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setCrs(Crs.ASTRO);
        searchKey.setSearchIndex(TENANT);
        Page<DataObject> page = repos.search(searchKey, 100, crit);
        Assert.assertEquals(2, page.getTotalElements());
        List constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Lynx"));
        Assert.assertTrue(constNames.contains("Ursa Major"));

        // BBox (15h, 60°, 16h, 70°) (into Draco reaching limit with Ursa Minor)
        crit = ICriterion.intersectsPolygon(new double[][][] {
                { { -105, 60.0 }, { -120, 60.0 }, { -120.0, 70.0 }, { -105, 70.0 }, { -105, 60.0 } } });
        page = repos.search(searchKey, 100, crit);
        Assert.assertEquals(2, page.getTotalElements());
        constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Draco"));
        Assert.assertTrue(constNames.contains("Ursa Minor"));

        // BBox (15h, 60°, 16h, 69°) (into Draco close to Ursa Minor but not reaching it)
        crit = ICriterion.intersectsPolygon(new double[][][] {
                { { -105, 60.0 }, { -120, 60.0 }, { -120.0, 69.9 }, { -105, 69.9 }, { -105, 60.0 } } });
        page = repos.search(searchKey, 100, crit);
        Assert.assertEquals(1, page.getTotalElements());
        constNames = page.getContent().stream().map(DataObject::getLabel).collect(Collectors.toList());
        Assert.assertTrue(constNames.contains("Draco"));

        // BBox (15h, 60°, 16h, 69°) (into Draco close to Ursa Minor but not reaching it)
        crit = ICriterion.intersectsPolygon(new double[][][] {
                { { -105, 60.0 }, { -120, 60.0 }, { -120.0, 69.99 }, { -105, 69.99 }, { -105, 60.0 } } });
        page = repos.search(searchKey, 100, crit);
        if (page.getTotalElements() != 1) {
            double precision = 0.1;
            System.out.printf("Precision > %f ° (%f m)\n", precision,
                              FastMath.toRadians(precision) * GeoHelper.AUTHALIC_SPHERE_RADIUS);
            return;
        }

        // BBox (15h, 60°, 16h, 69°) (into Draco close to Ursa Minor but not reaching it)
        crit = ICriterion.intersectsPolygon(new double[][][] {
                { { -105, 60.0 }, { -120, 60.0 }, { -120.0, 69.999 }, { -105, 69.999 }, { -105, 60.0 } } });
        page = repos.search(searchKey, 100, crit);
        if (page.getTotalElements() != 1) {
            double precision = 0.01;
            System.out.printf("Precision > %f ° (%f m)\n", precision,
                              FastMath.toRadians(precision) * GeoHelper.AUTHALIC_SPHERE_RADIUS);
            return;
        }

        // BBox (15h, 60°, 16h, 69°) (into Draco close to Ursa Minor but not reaching it)
        // Not ok with quadtree and tree_levels 20 (69.99984 is ok)
        crit = ICriterion.intersectsPolygon(new double[][][] {
                { { -105, 60.0 }, { -120, 60.0 }, { -120.0, 69.9999 }, { -105, 69.9999 }, { -105, 60.0 } } });
        page = repos.search(searchKey, 100, crit);
        if (page.getTotalElements() != 1) {
            double precision = 0.001;
            System.out.printf("Precision > %f ° (%f m)\n", precision,
                              FastMath.toRadians(precision) * GeoHelper.AUTHALIC_SPHERE_RADIUS);
            return;
        }

        // BBox (15h, 60°, 16h, 69°) (into Draco close to Ursa Minor but not reaching it)
        // Ok with quadtree and tree_levels 21
        crit = ICriterion.intersectsPolygon(new double[][][] {
                { { -105, 60.0 }, { -120, 60.0 }, { -120.0, 69.99993 }, { -105, 69.99993 }, { -105, 60.0 } } });
        page = repos.search(searchKey, 100, crit);
        if (page.getTotalElements() != 1) {
            double precision = 0.0001;
            System.out.printf("Precision > %f ° (%f m)\n", precision,
                              FastMath.toRadians(precision) * GeoHelper.AUTHALIC_SPHERE_RADIUS);
        } else {
            double precision = 0.00007;
            System.out.printf("Precision < %f ° (%f m)\n", precision,
                              FastMath.toRadians(precision) * GeoHelper.AUTHALIC_SPHERE_RADIUS);
        }

    }

}
