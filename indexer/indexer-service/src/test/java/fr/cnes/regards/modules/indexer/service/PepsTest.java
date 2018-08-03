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
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.jillesvangurp.geo.GeoGeometry;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.indexer.dao.IEsRepository;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;
import fr.cnes.regards.modules.indexer.service.test.SearchConfiguration;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author oroussel
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = { SearchConfiguration.class })
@Ignore
public class PepsTest {

    @Autowired
    private IEsRepository repos;

    private static final String TENANT = "peps";

    private static final int BULK_SIZE = 800;

    private NumberFormat format = DecimalFormat.getInstance();

    @Autowired
    private Gson gson;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    private Model model;

    @Before
    public void setup() throws TransformException, SQLException, IOException {
        System.setProperty("https.proxyHost", "proxy2.si.c-s.fr");
        System.setProperty("https.proxyPort", "3128");

        tenantResolver.forceTenant(TENANT);
        model = new Model();
        model.setName("Wgs84 model");

        if (!repos.indexExists(TENANT)) {
            repos.createIndex(TENANT);
            repos.setGeometryMapping(TENANT, EntityType.DATA.toString());
            repos.setAutomaticDoubleMapping(TENANT, EntityType.DATA.toString());
            repos.setSettingsForBulk(TENANT);
            try {
                createDataFromPeps();
            } catch (Exception e) {
                e.printStackTrace();
                repos.deleteIndex(TENANT);
            }
        }
    }

    private void createDataFromPeps() throws IOException {
        // Select all data between parallels -80 and -60 from peps on S1
        List<DataObject> objects = selectFromPeps("-180.0,-80.0,180.0,-60.0", "2018-06-01", "2018-07-01", "S1");
        int savedCount = repos.saveBulk(TENANT, objects);
        System.out.printf("Saved %d/%d objects\n", savedCount, objects.size());

        // Select all data between parallels 60 and 80 from peps on S1
        objects = selectFromPeps("-180.0,60.0,180.0,80.0", "2017-12-01", "2018-04-01", "S1");
        savedCount = repos.saveBulk(TENANT, objects);
        System.out.printf("Saved %d/%d objects\n", savedCount, objects.size());

        // Select all data between parallels -10 and 10 from peps on S3
        objects = selectFromPeps("-180.0,-10.0,180.0,10.0", "2018-06-01", "2018-07-01", "S3");
        savedCount = repos.saveBulk(TENANT, objects);
        System.out.printf("Saved %d/%d objects\n", savedCount, objects.size());


        repos.unsetSettingsForBulk(TENANT);
        repos.refresh(TENANT);
    }

    /**
     * Select data from Peps giving a bbox
     * @param bbox format : min Longitude,min latitude,max longitude,max latitude
     * @return Data objects created from Peps (geometry and title as label)
     */
    private List<DataObject> selectFromPeps(String bbox, String startDate, String endDate, String sat) throws IOException {
        List<DataObject> objects = new ArrayList<>();
        boolean ended = false;
        int totalResults = 0;
        int page = 0;
        while (!ended) {
            page++;
            URL pepsRequestURL = new URL(String.format(sat.equals("S1") ?
                    "https://peps.cnes.fr/resto/api/collections/S1/search.json?box=%s"
                            + "&instrument=SAR-C+SAR&lang=fr&maxRecords=500&page=%d&platform=S1A&polarisation=HH"
                            + "&processingLevel=LEVEL1&productType=GRD&q="
                            + "&startDate=%sT00:00:00&completionDate=%sT00:00:00":
                    "https://peps.cnes.fr/resto/api/collections/S3/search.json?box=%s&maxRecords=100&page=%d"
                            + "&startDate=2018-06-01T00:00:00&completionDate=2018-07-01T00:00:00"
                            + "&lang=fr&q=", bbox, page, startDate, endDate));
            URLConnection ctx = pepsRequestURL.openConnection();
            ctx.setRequestProperty("Accept",
                                   "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            ctx.setRequestProperty("User-Agent", "Regards For Geo Validation Test");
            ctx.setRequestProperty("Accept-Encoding", "utf-8");
            ctx.setDoInput(true);
            ctx.connect();

            try (BufferedReader br = new BufferedReader(new InputStreamReader(ctx.getInputStream()))) {
                JsonObject root = gson.fromJson(br, JsonElement.class).getAsJsonObject();
                // Manage pagination
                JsonObject properties = root.getAsJsonObject("properties");
                totalResults = properties.get("totalResults").getAsInt();
                int startIndex = properties.get("startIndex").getAsInt();
                int itemsPerPage = properties.get("itemsPerPage").getAsInt();
                if (startIndex + itemsPerPage >= totalResults + 1) {
                    ended = true;
                }
                // Create data objects
                JsonArray features = root.getAsJsonArray("features");
                for (int i = 0; i < features.size(); i++) {
                    JsonObject feature = features.get(i).getAsJsonObject();
                    IGeometry geometry = gson.fromJson(feature.get("geometry"), IGeometry.class);
                    DataObject object = new DataObject(model, TENANT,
                                                       feature.get("properties").getAsJsonObject().get("title")
                                                               .getAsString());
                    object.setIpId(
                            new UniformResourceName(OAISIdentifier.SIP, EntityType.DATA, TENANT, UUID.randomUUID(), 1));
                    object.setGeometry(geometry);
                    object.setWgs84(geometry);
                    objects.add(object);
                }
            }
        }
        return objects;
    }

    private void test(double left, double bottom, double right, double top, String startDate, String endDate, String sat)
            throws InvalidGeometryException, IOException {
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(TENANT);

        if (right < 180) {
            double[][] bboxPolygon = new double[][] { { left, bottom }, { right, bottom }, { right, top },
                    { left, top }, { left, bottom } };
            ICriterion bboxCrit = ICriterion.intersectsBbox(left, bottom, right, top);
            ICriterion crit = ICriterion.and(ICriterion.startsWith("feature.label", sat), bboxCrit);
            // ES execution
            long start = System.currentTimeMillis();
            List<DataObject> objectsFromEs = repos.search(searchKey, 1000, crit).getContent();
            long durationEs = System.currentTimeMillis() - start;

            // PEPS execution
            start = System.currentTimeMillis();
            List<DataObject> objectsFromPeps = selectFromPeps(
                    String.format(Locale.ENGLISH, "%f,%f,%f,%f", left, bottom, right, top), startDate, endDate, sat);
            long durationPeps = System.currentTimeMillis() - start;

            checkResults(bboxPolygon, new ArrayList<>(objectsFromEs), objectsFromPeps);
            System.out
                    .println(String.format("Durations Peps - %d ms, Elasticsearch - %d ms", durationPeps, durationEs));
        } else { // left > 180 must cut bbox because Elasticsearch doesn't want a longitude > 180
            double[][] leftBboxPolygon = new double[][] { { left, bottom }, { 180, bottom }, { 180, top },
                    { left, top }, { left, bottom } };
            double[][] rightBboxPolygon = new double[][] { { -180, bottom }, { 360 - right, bottom },
                    { 360 - right, top }, { -180, top }, { -180, bottom } };
            ICriterion bboxCrit = ICriterion.intersectsBbox(left, bottom, right, top);
            ICriterion crit = ICriterion.and(ICriterion.startsWith("feature.label", sat), bboxCrit);
            // ES execution
            long start = System.currentTimeMillis();
            List<DataObject> objectsFromEs = repos.search(searchKey, 1000, crit).getContent();
            long durationEs = System.currentTimeMillis() - start;

            // PEPS execution
            start = System.currentTimeMillis();
            List<DataObject> objectsFromPeps = selectFromPeps(
                    String.format(Locale.ENGLISH, "%f,%f,%f,%f", left, bottom, right, top), startDate, endDate, sat);
            long durationPeps = System.currentTimeMillis() - start;

            checkResults(leftBboxPolygon, rightBboxPolygon, new ArrayList<>(objectsFromEs), objectsFromPeps);
            System.out
                    .println(String.format("Durations Peps - %d ms, Elasticsearch - %d ms", durationPeps, durationEs));

        }

    }

    private void testNegativeLatitude(double left, double bottom, double right, double top) throws InvalidGeometryException, IOException {
        test(left, bottom, right, top, "2018-06-01", "2018-07-01", "S1");
    }

    private void testPositiveLatitude(double left, double bottom, double right, double top) throws InvalidGeometryException, IOException {
        test(left, bottom, right, top, "2017-12-01", "2018-04-01", "S1");
    }

    private void testAroundLatitude0(double left, double bottom, double right, double top) throws InvalidGeometryException, IOException {
        test(left, bottom, right, top, "2018-06-01", "2018-07-01", "S3");
    }

    /**
     * Test on all negative latitude band that has been initially retrieved from PEPS
     */
    @Test
    public void testInitialNegativeLatitudeBbox() throws InvalidGeometryException, IOException {
        testNegativeLatitude(-180, -80, 180, -60);
    }

    @Test
    public void testInnerNegativeLatitudePositiveLontitudeBbox() throws InvalidGeometryException, IOException {
        testNegativeLatitude(15, -73, 93, -65);
    }

    @Test
    public void testInnerNegativeLatitudeOnDatelineLongitude1Bbox() throws InvalidGeometryException, IOException {
        testNegativeLatitude(160, -70, 200, -68);
    }

    @Test
    public void testInnerNegativeLatitudeOnDateline2Bbox() throws InvalidGeometryException, IOException {
        testNegativeLatitude(150, -75, 210, -65);
    }


    /**
     * Test on all positive latitude band that has been initially retrieved from PEPS
     */
    @Test
    public void testInitialPositiveLatitudeBbox() throws InvalidGeometryException, IOException {
        testPositiveLatitude(-180, 60, 180, 80);
    }

    @Test
    public void testInnerPositiveLatitudePositiveLontitudeBbox() throws InvalidGeometryException, IOException {
        testPositiveLatitude(15, 65, 93, 73);
    }

    @Test
    public void testInnerPositiveLatitudeOnDatelineLongitude1Bbox() throws InvalidGeometryException, IOException {
        testPositiveLatitude(160, 68, 200, 70);
    }

    @Test
    public void testInnerPositiveLatitudeOnDateline2Bbox() throws InvalidGeometryException, IOException {
        testPositiveLatitude(150, 65, 210, 75);
    }


    /**
     * Test on all positive latitude band that has been initially retrieved from PEPS
     */
    @Test
    public void testInitialAroundLatitude0Bbox() throws InvalidGeometryException, IOException {
        testAroundLatitude0(-180, -10, 180, 10);
    }

    @Test
    public void testInnerAroundLatitude0PositiveLontitudeBbox() throws InvalidGeometryException, IOException {
        testAroundLatitude0(15, -5, 93, 3);
    }

    @Test
    public void testInnerAroundLatitude0OnDatelineLongitude1Bbox() throws InvalidGeometryException, IOException {
        testAroundLatitude0(160, -2, 200, 0);
    }

    @Test
    public void testInnerAroundLatitude0OnDateline2Bbox() throws InvalidGeometryException, IOException {
        testAroundLatitude0(150, -1, 210, 1);
    }


    private void checkResults(double[][] bboxPolygon, List<DataObject> objectsFromEs,
            List<DataObject> objectsFromPeps) {
        // most polygons from Peps have an area betwwen 1e10 and 1e11
        // A polygon with an area > 1e12 means there is a problem (polygon crossing dateline)
        checkResults(o -> GeoGeometry.area(GeoUtil.toArray(o.getGeometry())) > 1.e12 || !GeoGeometry
                .overlap(GeoUtil.toArray(o.getGeometry()), bboxPolygon), objectsFromEs, objectsFromPeps);
    }

    private void checkResults(double[][] bbox1Polygon, double[][] bbox2Polygon, List<DataObject> objectsFromEs,
            List<DataObject> objectsFromPeps) {
        checkResults(o -> GeoGeometry.area(GeoUtil.toArray(o.getGeometry())) > 1.e12 || (
                !GeoGeometry.overlap(GeoUtil.toArray(o.getGeometry()), bbox1Polygon) && !GeoGeometry
                        .overlap(GeoUtil.toArray(o.getGeometry()), bbox2Polygon)), objectsFromEs, objectsFromPeps);
    }

    private void checkResults(Predicate<DataObject> intersectPredicate, List<DataObject> objectsFromEs,
            List<DataObject> objectsFromPeps) {
        // Check all objects from Peps
        Set<DataObject> badPepsResults = objectsFromPeps.stream().filter(intersectPredicate)
                .collect(Collectors.toSet());
        if (!badPepsResults.isEmpty()) {
            System.out.printf("Peps returned %d false positive data objects: %s\n", badPepsResults.size(),
                              badPepsResults.stream().map(o -> o.toString() + ", " + o.getGeometry().toString())
                                      .collect(Collectors.joining("\n")));
            objectsFromPeps.removeAll(badPepsResults);
        }

        // Check all objects from Elasticsearch
        Set<DataObject> badEsResults = objectsFromEs.stream().filter(intersectPredicate).collect(Collectors.toSet());
        if (!badEsResults.isEmpty()) {
            System.out.printf("Elasticsearch returned %d false positive data objects: %s\n", badEsResults.size(),
                              badEsResults.stream().map(o -> o.toString() + ", " + o.getGeometry().toString())
                                      .collect(Collectors.joining("\n")));
            objectsFromEs.removeAll(badEsResults);
        }
        // Check all bad objects from ES are also bad objects from Peps
        Set<String> badEsObjectLabels = badEsResults.stream().map(DataObject::getLabel).collect(Collectors.toSet());
        Set<String> badPepsObjectLabels = badPepsResults.stream().map(DataObject::getLabel).collect(Collectors.toSet());
        badEsObjectLabels.removeAll(badPepsObjectLabels);
        Assert.assertTrue(badEsObjectLabels.isEmpty());

        // Finally check that all Peps objects are also Es objects
        Set<String> esObjectLabels = objectsFromEs.stream().map(DataObject::getLabel).collect(Collectors.toSet());
        Set<String> pepsObjectLabels = objectsFromPeps.stream().map(DataObject::getLabel).collect(Collectors.toSet());
        pepsObjectLabels.removeAll(esObjectLabels);

        Assert.assertTrue(pepsObjectLabels.isEmpty());

        if (objectsFromEs.size() >= objectsFromPeps.size()) {
            System.out.println("Peps misses some data regarding Elasticsearch results");
        }
    }
}
