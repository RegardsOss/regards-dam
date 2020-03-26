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

import static fr.cnes.regards.modules.indexer.service.GeoUtil.toWgs84;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.Point;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.OaisUniformResourceName;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.dam.domain.entities.DataObject;
import fr.cnes.regards.modules.indexer.dao.EsHelper;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.indexer.service.test.SearchConfiguration;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * @author oroussel
 */
@Ignore
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SearchConfiguration.class })
public class SearchServiceTest {

    @Autowired
    private IEsRepository repos;

    @Value("${regards.tenant}")
    private String tenant;

    private final NumberFormat format = DecimalFormat.getInstance();

    @Before
    public void setTup() throws TransformException {
        if (!repos.indexExists(tenant)) {
            repos.createIndex(tenant);

            this.createPolygons();
            this.createTenthDegreesPoints();

        }
    }

    private void createTenthDegreesPoints() throws TransformException {
        Model model = new Model();
        model.setName("Data model");

        int count = 0;
        List<DataObject> dos = new ArrayList<>();
        for (double lon = 0; lon < 180; lon += 0.1) {
            for (double lat = 0; lat < 90; lat += 0.1) {
                String label = "POINT_" + format.format(lon) + "_" + format.format(lat);
                DataObject object = new DataObject(model, tenant, label, label);
                object.setIpId(new OaisUniformResourceName(OAISIdentifier.SIP, EntityType.DATA, tenant,
                        UUID.randomUUID(), 1));
                Point point = IGeometry.point(EsHelper.scaled(lon), EsHelper.scaled(lat))
                        .withCrs(Crs.MARS_49900.toString());
                object.setNormalizedGeometry(point);
                object.setWgs84(toWgs84(point));
                dos.add(object);
            }
            if (dos.size() > 10_000) {
                repos.saveBulk(tenant, dos);
                count += dos.size();
                System.out.println("Saved " + count);
                dos.clear();
            }
        }
    }

    private void createPolygons() throws TransformException {
        Model model = new Model();
        model.setName("Data model");

        int count = 0;
        List<DataObject> dos = new ArrayList<>();
        for (double lon = 0; lon < 179; lon += 2) {
            for (double lat = 0; lat < 89; lat += 2) {
                String label = "POLYGON_" + format.format(lon) + "_" + format.format(lat);
                DataObject object = new DataObject(model, tenant, label, label);
                object.setIpId(new OaisUniformResourceName(OAISIdentifier.SIP, EntityType.DATA, tenant,
                        UUID.randomUUID(), 1));
                Polygon polygon = IGeometry.simplePolygon(lon, lat, lon + 1, lat, lon + 1, lat + 1, lon, lat + 1)
                        .withCrs(Crs.MARS_49900.toString());
                object.setNormalizedGeometry(polygon);
                object.setWgs84(toWgs84(polygon));
                dos.add(object);
            }
            if (dos.size() > 1_000) {
                repos.saveBulk(tenant, dos);
                count += dos.size();
                System.out.println("Saved " + count);
                dos.clear();
            }
        }
    }

    private void computeCircleSymetricTest(double[] center, Double distance) {
        this.computeCircleTest("SYMETRIC", center, distance, ICriterion.intersectsCircle(center, distance.toString()));
    }

    private void computeCircleNotSymetricTest(double[] center, Double distance) {
        this.computeCircleTest("NOT SYMETRIC", center, distance,
                               ICriterion.intersectsCircle(center, distance.toString()));
    }

    private void computeCircleOnOnlyPolygonsSymetricTest(double[] center, Double distance) {
        this.computeCircleTest("SYMETRIC POLYGONS", center, distance,
                               ICriterion.and(ICriterion.startsWith("feature.label", "POLYGON"),
                                              ICriterion.intersectsCircle(center, distance.toString())));
    }

    private void computeCircleTest(String label, double[] center, Double distance, ICriterion criterion) {
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(tenant);
        searchKey.setCrs(Crs.MARS_49900);
        long duration = 0;
        long start = System.currentTimeMillis();
        Page<DataObject> page = repos.search(searchKey, 5000, criterion);
        duration += System.currentTimeMillis() - start;

        // Check if all result points are nearer from asked distance on Mars
        int errorCount = 0;
        double maxErrorDistance = 0;
        boolean ended = false;
        do {
            for (DataObject object : page.getContent()) {
                if (!GeoHelper.isNearer(object.getNormalizedGeometry(), center, distance, Crs.MARS_49900)) {
                    errorCount++;
                    if (object.getNormalizedGeometry() instanceof Point) {
                        Point point = object.getNormalizedGeometry();
                        double error = GeoHelper.getDistance(point.getCoordinates().getLongitude(),
                                                             point.getCoordinates().getLatitude(), center[0], center[1],
                                                             Crs.MARS_49900)
                                - distance;
                        maxErrorDistance = Math.max(maxErrorDistance, error);
                        System.out.printf("Error (%s): %f m \n", object.getLabel(), error);
                    } else {
                        System.out.printf("Error (%s)\n", object.getLabel());
                    }
                }
            }
            Pageable pageable = page.nextPageable();
            if (pageable != null) {
                start = System.currentTimeMillis();
                page = repos.search(searchKey, pageable, criterion);
                duration += System.currentTimeMillis() - start;
            } else {
                ended = true;
            }
        } while (!ended);
        System.out.printf("\n%s SEARCH (radius: %d km):\n", label, (int) (distance / 1000));
        System.out.println("---------------------------------");
        System.out
                .printf("Result count: %d, results in error: %d (%s %%), max distance error : %d (%s %%) (search only duration: %d ms)\n\n",
                        page.getTotalElements(), errorCount,
                        format.format((100. * errorCount) / page.getTotalElements()), (int) maxErrorDistance,
                        format.format((100. * maxErrorDistance) / distance), duration);
    }

    private void computePolygonTest(String label, ICriterion criterion, int expectedCount) {
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(tenant);
        searchKey.setCrs(Crs.MARS_49900);
        long duration = 0;
        long start = System.currentTimeMillis();
        Page<DataObject> page = repos.search(searchKey, 5000, criterion);
        duration += System.currentTimeMillis() - start;

        // Check if all result points are nearer from asked distance on Mars
        int errorCount = 0;
        double maxErrorDistance = 0;
        boolean ended = false;
        do {
            for (DataObject object : page.getContent()) {
                System.out.println(object.getFeature().getGeometry());
                //
                // if (!GeoHelper.isNearer(object.getNormalizedGeometry(), center, distance, Crs.MARS_49900)) {
                // errorCount++;
                // if (object.getNormalizedGeometry() instanceof Point) {
                // Point point = object.getNormalizedGeometry();
                // double error = GeoHelper.getDistance(point.getCoordinates().getLongitude(),
                // point.getCoordinates().getLatitude(), center[0], center[1],
                // Crs.MARS_49900) - distance;
                // maxErrorDistance = Math.max(maxErrorDistance, error);
                // System.out.printf("Error (%s): %f m \n", object.getLabel(), error);
                // } else {
                // System.out.printf("Error (%s)\n", object.getLabel());
                // }
                // }
            }
            Pageable pageable = page.nextPageable();
            if (pageable != null) {
                start = System.currentTimeMillis();
                page = repos.search(searchKey, pageable, criterion);
                duration += System.currentTimeMillis() - start;
            } else {
                ended = true;
            }
        } while (!ended);
        System.out.printf("\n%s SEARCH :\n", label);
        System.out.println("---------------------------------");
        // System.out
        // .printf("Result count: %d, results in error: %d (%s %%), max distance error : %d (%s %%) (search only
        // duration: %d ms)\n\n",
        // page.getTotalElements(), errorCount,
        // format.format(100. * errorCount / page.getTotalElements()), (int) maxErrorDistance,
        // format.format(100. * maxErrorDistance / distance), duration);
        System.out.printf("Result count: %d\n", page.getTotalElements());
        Assert.assertEquals(expectedCount, page.getTotalElements());
    }

    @Test
    public void testCircleSymetric100km() {
        double[] center = new double[] { 0, 0 };
        Double distance = 100_000.0;
        computeCircleSymetricTest(center, distance);
    }

    @Test
    public void testCircleSymetric50km() {
        double[] center = new double[] { 0, 0 };
        Double distance = 50_000.0;
        computeCircleSymetricTest(center, distance);
    }

    @Test
    public void testCircleSymetric1kmAroundNorthPole() {
        double[] center = new double[] { 0, 90 };
        Double distance = 1_000.0;
        computeCircleSymetricTest(center, distance);
    }

    @Test
    public void testCircleSymetric10kmAroundNorthPole() {
        double[] center = new double[] { 0, 90 };
        Double distance = 10_000.0;
        computeCircleSymetricTest(center, distance);
    }

    @Test
    public void testCircleSymetric50kmAroundNorthPole() {
        double[] center = new double[] { 0, 90 };
        Double distance = 50_000.0;
        computeCircleSymetricTest(center, distance);
    }

    @Test
    public void testCircleNotSymetric45_50km() {
        double[] center = new double[] { 45, 45 };
        Double distance = 50_000.0;
        computeCircleNotSymetricTest(center, distance);
    }

    @Test
    public void testCircleNotSymetric45_10km() {
        double[] center = new double[] { 45, 45 };
        Double distance = 10_000.0;
        computeCircleNotSymetricTest(center, distance);
    }

    @Test
    public void testCircleNotSymetric60_50km() {
        double[] center = new double[] { 60, 60 };
        Double distance = 50_000.0;
        computeCircleNotSymetricTest(center, distance);
    }

    @Test
    public void testCircleNotSymetric60_10km() {
        double[] center = new double[] { 60, 60 };
        Double distance = 10_000.0;
        computeCircleNotSymetricTest(center, distance);
    }

    @Test
    public void testCircleSymetricOnlyPolygons200kmAroundNorthPole() {
        double[] center = new double[] { 0, 90 };
        Double distance = 200_000.0;
        computeCircleOnOnlyPolygonsSymetricTest(center, distance);
    }

    @Test
    public void testCircleSymetricOnlyPolygons1000kmAroundNorthPole() {
        double[] center = new double[] { 0, 90 };
        Double distance = 1_000_000.0;
        computeCircleOnOnlyPolygonsSymetricTest(center, distance);
    }

    @Test
    public void testCircleSymetricOnlyPolygons10000kmAroundNorthPole() {
        double[] center = new double[] { 0, 90 };
        Double distance = 10_000_000.0;
        computeCircleOnOnlyPolygonsSymetricTest(center, distance);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorOnlyPolygonsInto() {
        ICriterion criterion = ICriterion
                .and(ICriterion.startsWith("feature.label", "POLYGON"), ICriterion.intersectsPolygon(new double[][][] {
                        { { 0.3, 0.3 }, { 0.6, 0.3 }, { 0.6, 0.6 }, { 0.3, 0.6 }, { 0.3, 0.3 } } }));
        computePolygonTest("INNER POLYGON", criterion, 1);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorOnlyPolygonsEqual() {
        ICriterion criterion = ICriterion
                .and(ICriterion.startsWith("feature.label", "POLYGON"), ICriterion.intersectsPolygon(new double[][][] {
                        { { 0., 0. }, { 1., 0. }, { 1., 1. }, { 0., 1. }, { 0., 0. } } }));
        computePolygonTest("EQUAL POLYGON", criterion, 1);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorOnlyPolygonsContains() {
        ICriterion criterion = ICriterion
                .and(ICriterion.startsWith("feature.label", "POLYGON"), ICriterion.intersectsPolygon(new double[][][] {
                        { { -0.3, -0.3 }, { 1.3, -0.3 }, { 1.3, 1.3 }, { -0.3, 1.3 }, { -0.3, -0.3 } } }));
        computePolygonTest("CONTAINS POLYGON", criterion, 1);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorOnlyPolygonsIntersects() {
        ICriterion criterion = ICriterion
                .and(ICriterion.startsWith("feature.label", "POLYGON"), ICriterion.intersectsPolygon(new double[][][] {
                        { { 0.6, 0.6 }, { 1.3, 0.6 }, { 1.3, 1.3 }, { 0.6, 1.3 }, { 0.6, 0.6 } } }));
        computePolygonTest("INTERSECT POLYGON", criterion, 1);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorInto() {
        ICriterion criterion = ICriterion.intersectsPolygon(new double[][][] {
                { { 0.3, 0.3 }, { 0.6, 0.3 }, { 0.6, 0.6 }, { 0.3, 0.6 }, { 0.3, 0.3 } } });
        // 4 * 4 points + 1 polygon
        computePolygonTest("INNER POLYGON", criterion, 17);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorEqual() {
        ICriterion criterion = ICriterion
                .intersectsPolygon(new double[][][] { { { 0., 0. }, { 1., 0. }, { 1., 1. }, { 0., 1. }, { 0., 0. } } });
        computePolygonTest("EQUAL POLYGON", criterion, 122);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorContains() {
        ICriterion criterion = ICriterion.intersectsPolygon(new double[][][] {
                { { -0.3, -0.3 }, { 1.3, -0.3 }, { 1.3, 1.3 }, { -0.3, 1.3 }, { -0.3, -0.3 } } });
        computePolygonTest("CONTAINS POLYGON", criterion, 197);
    }

    @Test
    public void testConvexSimplePolygonNearEquatorIntersects() {
        ICriterion criterion = ICriterion.intersectsPolygon(new double[][][] {
                { { 0.6, 0.6 }, { 1.3, 0.6 }, { 1.3, 1.3 }, { 0.6, 1.3 }, { 0.6, 0.6 } } });
        computePolygonTest("INTERSECT POLYGON", criterion, 65);
    }

    @Test
    public void testConcaveSimplePolygonNearEquatorOnlyPolygonsOuto() {
        ICriterion criterion = ICriterion.and(ICriterion.startsWith("feature.label", "POLYGON"),
                                              ICriterion.intersectsPolygon(new double[][][] { { { -0.3, -0.3 },
                                                      { 1.3, -0.3 }, { 1.3, 1.3 }, { -0.3, 1.3 }, { -0.3, 1.1 },
                                                      { 1.1, 1.1 }, { 1.1, -0.1 }, { -0.3, -0.1 }, { -0.3, -0.3 } } }));
        computePolygonTest("CONCAVE BANANA POLYGON", criterion, 0);
    }

    @Test
    public void testConcaveSimplePolygonNearEquator() {
        ICriterion criterion = ICriterion
                .intersectsPolygon(new double[][][] { { { -0.3, -0.3 }, { 1.3, -0.3 }, { 1.3, 1.3 }, { -0.3, 1.3 },
                        { -0.3, 1.1 }, { 1.1, 1.1 }, { 1.1, -0.1 }, { -0.3, -0.1 }, { -0.3, -0.3 } } });
        computePolygonTest("CONCAVE BANANA POLYGON", criterion, 75);
    }

    /*
     * @Test
     * public void testNorthPole() {
     * GeometryFactory gf = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING), 4326);
     * Coordinate[] containsNP = new Coordinate[5];
     * containsNP[0] = new Coordinate(-60, 85);
     * containsNP[1] = new Coordinate(160, 85);
     * containsNP[2] = new Coordinate(120, 85);
     * containsNP[3] = new Coordinate(-20, 85);
     * containsNP[4] = new Coordinate(-60, 85);
     * LinearRing exteriorNP = gf.createLinearRing(containsNP);
     * com.vividsolutions.jts.geom.Polygon npPolygon = gf.createPolygon(exteriorNP, new LinearRing[0]);
     *
     * Coordinate[] firstQuarterCoordinates = new Coordinate[5];
     * firstQuarterCoordinates[0] = new Coordinate(0, 0);
     * firstQuarterCoordinates[1] = new Coordinate(90, 0);
     * firstQuarterCoordinates[2] = new Coordinate(90, 90);
     * firstQuarterCoordinates[3] = new Coordinate(0, 90);
     * firstQuarterCoordinates[4] = new Coordinate(0, 0);
     * LinearRing firstQuarterLR = gf.createLinearRing(firstQuarterCoordinates);
     * com.vividsolutions.jts.geom.Polygon firstQuerterPolygon = gf.createPolygon(firstQuarterLR, new LinearRing[0]);
     *
     * Geometry interPoly = firstQuerterPolygon.intersection(npPolygon);
     *
     * System.out.println(interPoly);
     *
     * Envelope env = firstQuerterPolygon.getEnvelopeInternal();
     * System.out.println("Here is the envelope of the polygon that crosses 180");
     * System.out.println("Min Y: " + env.getMinY());
     * System.out.println("Max Y: " + env.getMaxY());
     * // ICriterion criterion = ICriterion.intersectsPolygon(
     * // new double[][][] { { { -90., 85. }, { 0., 85. }, { 90, 85. }, { 180., 85. }, { -90., 85. } } });
     * // computePolygonTest("INTERSECT POLYGON", criterion, 100);
     * }
     */
}
