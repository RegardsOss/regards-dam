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
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
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
        // Select all data between parallels -80 and -60 from peps
        List<DataObject> objects = selectFromPeps("-180.0,-80.0,180.0,-60.0");
        int savedCount = repos.saveBulk(TENANT, objects);
        System.out.printf("Saved %d/%d objects\n", savedCount, objects.size());
        repos.unsetSettingsForBulk(TENANT);
        repos.refresh(TENANT);
    }

    /**
     * Select data from Peps giving a bbox
     * @param bbox format : min Longitude,min latitude,max longitude,max latitude
     * @return Data objects created from Peps (geometry and title as label)
     */
    private List<DataObject> selectFromPeps(String bbox) throws IOException {
        List<DataObject> objects = new ArrayList<>();
        boolean ended = false;
        int totalResults = 0;
        int page = 0;
        while (!ended) {
            page++;
            URL pepsRequestURL = new URL(String.format(
                    "https://peps.cnes.fr/resto/api/collections/S1/search.json?box=%s"
                            + "&instrument=SAR-C+SAR&lang=fr&maxRecords=500&page=%d&platform=S1A&polarisation=HH"
                            + "&processingLevel=LEVEL1&productType=GRD&q="
                            + "&startDate=2018-06-01T00:00:00&completionDate=2018-07-01T00:00:00", bbox, page));
            URLConnection ctx = pepsRequestURL
                    //                .openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy2.si.c-s.fr", 3128)));
                    .openConnection();
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

    @Test
    public void testInitialBbox() throws InvalidGeometryException, IOException {
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(TENANT);

        String bbox = "-180.0,-80.0,180.0,-60.0";
        ICriterion bboxCrit = ICriterion.intersectsBbox(bbox);
        long start = System.currentTimeMillis();
        List<DataObject> objectsFromEs = repos.search(searchKey, 1000, bboxCrit).getContent();
        long durationEs = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        List<DataObject> objectsFromPeps = selectFromPeps(bbox);
        long durationPeps = System.currentTimeMillis() - start;

        if (objectsFromPeps.size() != objectsFromEs.size()) {
            Set<String> esLabels = objectsFromEs.stream().map(o -> o.getFeature().getLabel()).collect(Collectors.toSet());
            Set<DataObject> missingObjects = objectsFromPeps.stream().filter(o -> !esLabels.contains(o.getLabel())).collect(Collectors.toSet());
            System.out.println("Missing data objects :");
            missingObjects.forEach(o -> {
                System.out.println(o);
                System.out.println(o.getWgs84().toString());
            });
            // 2 data objects from PEPS results are in error :  S1A_IW_GRDH_1SSH_20180612T153554_20180612T153623_022325_026AA9_847C
            // and S1A_IW_GRDH_1SSH_20180624T153555_20180624T153624_022500_026FDE_46DB
            // Remove them from data to validate results
            for (Iterator<DataObject> i = missingObjects.iterator(); i.hasNext(); ) {
                DataObject object = i.next();
                // Remove objects that are not the two in error
                switch (object.getLabel()) {
                    case "S1A_IW_GRDH_1SSH_20180612T153554_20180612T153623_022325_026AA9_847C":
                    case "S1A_IW_GRDH_1SSH_20180624T153555_20180624T153624_022500_026FDE_46DB":
                        break;
                    default:
                        i.remove();

                }
            }
            // remove all objects in error from Peps results
            objectsFromPeps.removeAll(missingObjects);
        }
        System.out.println(String.format("Durations Peps - %d ms, Elasticsearch - %d ms", durationPeps, durationEs));
        Assert.assertEquals(objectsFromPeps.size(), objectsFromEs.size());
    }

    @Test
    public void testInnerBbox() throws InvalidGeometryException, IOException {
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(TENANT);

        String bbox = "15.0,-73.0,93.0,-65.0";
        ICriterion bboxCrit = ICriterion.intersectsBbox(bbox);
        long start = System.currentTimeMillis();
        List<DataObject> objectsFromEs = repos.search(searchKey, 1000, bboxCrit).getContent();
        long durationEs = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        List<DataObject> objectsFromPeps = selectFromPeps(bbox);
        long durationPeps = System.currentTimeMillis() - start;

        if (objectsFromPeps.size() != objectsFromEs.size()) {
            Set<String> esLabels = objectsFromEs.stream().map(o -> o.getFeature().getLabel()).collect(Collectors.toSet());
            Set<DataObject> missingObjects = objectsFromPeps.stream().filter(o -> !esLabels.contains(o.getLabel())).collect(Collectors.toSet());
            System.out.println("Missing data objects :");
            missingObjects.forEach(o -> {
                System.out.println(o);
                System.out.println(o.getWgs84().toString());
            });
            // 2 data objects from PEPS results are in error :  S1A_IW_GRDH_1SSH_20180612T153554_20180612T153623_022325_026AA9_847C
            // and S1A_IW_GRDH_1SSH_20180624T153555_20180624T153624_022500_026FDE_46DB
            // Remove them from data to validate results
            for (Iterator<DataObject> i = missingObjects.iterator(); i.hasNext(); ) {
                DataObject object = i.next();
                // Remove objects that are not the two in error
                switch (object.getLabel()) {
                    case "S1A_IW_GRDH_1SSH_20180612T153554_20180612T153623_022325_026AA9_847C":
                    case "S1A_IW_GRDH_1SSH_20180624T153555_20180624T153624_022500_026FDE_46DB":
                        break;
                    default:
                        i.remove();

                }
            }
            // remove all objects in error from Peps results
            objectsFromPeps.removeAll(missingObjects);
        }
        System.out.println(String.format("Durations Peps - %d ms, Elasticsearch - %d ms", durationPeps, durationEs));
        Assert.assertEquals(objectsFromPeps.size(), objectsFromEs.size());
    }

    @Test
    public void testInnerPositiveLonBbox() throws InvalidGeometryException, IOException {
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(TENANT);

        String bbox = "15.0,-73.0,93.0,-65.0";
        ICriterion bboxCrit = ICriterion.intersectsBbox(bbox);
        long start = System.currentTimeMillis();
        List<DataObject> objectsFromEs = repos.search(searchKey, 1000, bboxCrit).getContent();
        long durationEs = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        List<DataObject> objectsFromPeps = selectFromPeps(bbox);
        long durationPeps = System.currentTimeMillis() - start;

        if (objectsFromPeps.size() != objectsFromEs.size()) {
            Set<String> esLabels = objectsFromEs.stream().map(o -> o.getFeature().getLabel()).collect(Collectors.toSet());
            Set<DataObject> missingObjects = objectsFromPeps.stream().filter(o -> !esLabels.contains(o.getLabel())).collect(Collectors.toSet());
            System.out.println("Missing data objects :");
            missingObjects.forEach(o -> {
                System.out.println(o);
                System.out.println(o.getWgs84().toString());
            });
            // 2 data objects from PEPS results are in error :  S1A_IW_GRDH_1SSH_20180612T153554_20180612T153623_022325_026AA9_847C
            // and S1A_IW_GRDH_1SSH_20180624T153555_20180624T153624_022500_026FDE_46DB
            // Remove them from data to validate results
            for (Iterator<DataObject> i = missingObjects.iterator(); i.hasNext(); ) {
                DataObject object = i.next();
                // Remove objects that are not the two in error
                switch (object.getLabel()) {
                    case "S1A_IW_GRDH_1SSH_20180612T153554_20180612T153623_022325_026AA9_847C":
                    case "S1A_IW_GRDH_1SSH_20180624T153555_20180624T153624_022500_026FDE_46DB":
                        break;
                    default:
                        i.remove();

                }
            }
            // remove all objects in error from Peps results
            objectsFromPeps.removeAll(missingObjects);
        }
        System.out.println(String.format("Durations Peps - %d ms, Elasticsearch - %d ms", durationPeps, durationEs));
        Assert.assertEquals(objectsFromPeps.size(), objectsFromEs.size());
    }

    @Test
    public void testInnerOnDatelineBbox() throws InvalidGeometryException, IOException {
        SimpleSearchKey<DataObject> searchKey = Searches.onSingleEntity(EntityType.DATA);
        searchKey.setSearchIndex(TENANT);

        String bbox = "160.0,-70.0,200.0,-68.0";
        ICriterion bboxCrit = ICriterion.intersectsBbox(bbox);
        long start = System.currentTimeMillis();
        List<DataObject> objectsFromEs = repos.search(searchKey, 1000, bboxCrit).getContent();
        long durationEs = System.currentTimeMillis() - start;

        start = System.currentTimeMillis();
        List<DataObject> objectsFromPeps = selectFromPeps(bbox);
        long durationPeps = System.currentTimeMillis() - start;

        if (objectsFromPeps.size() != objectsFromEs.size()) {
            // Print data objects into Peps but not Elasticsearch
            Set<String> esLabels = objectsFromEs.stream().map(o -> o.getFeature().getLabel()).collect(Collectors.toSet());
            Set<DataObject> missingEsObjects = objectsFromPeps.stream().filter(o -> !esLabels.contains(o.getLabel())).collect(Collectors.toSet());
            System.out.println("Missing data objects into ElasticSearch results :");
            missingEsObjects.forEach(o -> {
                System.out.println(o);
                System.out.println(o.getWgs84().toString());
            });

            // Print data objects into Elasticsearch but not Peps
            Set<String> pepsLabels = objectsFromPeps.stream().map(o -> o.getFeature().getLabel()).collect(Collectors.toSet());
            Set<DataObject> missingPepsObjects = objectsFromEs.stream().filter(o -> !pepsLabels.contains(o.getLabel())).collect(Collectors.toSet());
            System.out.println("Missing data objects into Peps results :");
            missingPepsObjects.forEach(o -> {
                System.out.println(o);
                System.out.println(o.getWgs84().toString());
            });
        }
        System.out.println(String.format("Durations Peps - %d ms, Elasticsearch - %d ms", durationPeps, durationEs));
        Assert.assertEquals(objectsFromPeps.size(), objectsFromEs.size());
    }
}
