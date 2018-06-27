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
package fr.cnes.regards.modules.indexer.dao.builder;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.elasticsearch.client.transport.NoNodeAvailableException;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.framework.jpa.json.GsonUtil;
import fr.cnes.regards.modules.indexer.dao.EsRepository;
import fr.cnes.regards.modules.indexer.dao.spatial.GeoHelper;
import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.SimpleSearchKey;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * @author oroussel
 */
public class GeoQueryOnPolygonsTest {

    private static final String INDEX = "test_geo";

    private static final String TYPE = "geo";

    private static Gson gson;

    private static EsRepository repository;

    private static SimpleSearchKey<PolygonItem> searchKey;

    @BeforeClass
    public static void setUp() throws Exception {
        Map<String, String> propMap = Maps.newHashMap();
        // By now, repository try to connect localhost:9200 for ElasticSearch
        boolean repositoryOK = true;
        try {
            gson = new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new OffsetDateTimeAdapter().nullSafe())
                    .create();
            repository = new EsRepository(gson, null, "localhost", 9200,
                                          new AggregationBuilderFacetTypeVisitor(100, 5));

            // This test is not intended to be executed on integration serveur but better locally to test
            // functionnalities during development phase
            //            repository = new EsRepository(gson, null, propMap.get("regards.elasticsearch.address"),
            //                    Integer.parseInt(propMap.get("regards.elasticsearch.http.port")), new AggregationBuilderFacetTypeVisitor(100, 5));
        } catch (NoNodeAvailableException e) {
            repositoryOK = false;
        }
        // Do not launch tests is Elasticsearch is not available
        Assume.assumeTrue(repositoryOK);

        GsonUtil.setGson(gson);

        if (repository.indexExists(INDEX)) {
            repository.deleteIndex(INDEX);
        }
        repository.createIndex(INDEX);
        repository.setGeometryMapping(INDEX, TYPE);

        PolygonItem p1 = new PolygonItem("P1", -10, 0, 0, -10, 10, 0, 0, 10);
        PolygonItem pn = new PolygonItem("PN", 0, 90, 0, 80, 90, 80);
//        PolygonItem northPole = new PolygonItem("NORTH_POLE", 0.0, 90.0);
//        PolygonItem southPole = new PolygonItem("SOUTH_POLE", 0.0, -90.0);
//        PolygonItem eastPole = new PolygonItem("EAST_POLE", 180.0, 0.0);
//        PolygonItem westPole = new PolygonItem("WEST_POLE", -180.0, 0.0);
//        PolygonItem point_180_20 = new PolygonItem("P1", 180.0, 20.0);
//        PolygonItem point_90_20 = new PolygonItem("P2", 90.0, 20.0);
//        PolygonItem point_0_0 = new PolygonItem("0_0", 0.0, 0.0);

//        repository.saveBulk(INDEX, northPole, southPole, point_180_20, point_90_20, eastPole, westPole, point_0_0);
        repository.saveBulk(INDEX, p1, pn);
        repository.refresh(INDEX);

        searchKey = new SimpleSearchKey<PolygonItem>(TYPE, PolygonItem.class);
        searchKey.setSearchIndex(INDEX);
    }

    private Double[] point(Integer... lonLats) {
        assert (lonLats.length == 2);
        return new Double[] { lonLats[0].doubleValue(), lonLats[1].doubleValue() };
    }

    private static Double[][][] simplePolygon(Integer... lonLats) {
        assert (lonLats.length >= 6);
        assert (lonLats.length % 2 == 0);
        Double[][] shell = new Double[lonLats.length / 2 + 1][];
        for (int i = 0; i < lonLats.length; i += 2) {
            shell[i / 2] = new Double[] { lonLats[i].doubleValue(), lonLats[i + 1].doubleValue() };
        }
        shell[shell.length - 1] = new Double[] { lonLats[0].doubleValue(), lonLats[1].doubleValue() };
        return new Double[][][] { shell };
    }

    @Test
    public void testCircleOn0_0() {
        CircleCriterion criterion = (CircleCriterion) ICriterion.intersectsCircle(point(0, 0), "300km");

        List<PolygonItem> result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("P1", result.get(0).getDocId());

        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(0, 90), "1m");

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("PN", result.get(0).getDocId());

        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(0, -90), "1117km");

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("PS", result.get(0).getDocId());
    }

    @Test
    public void testCircleNearNorthPole() {
        CircleCriterion criterion = (CircleCriterion) ICriterion.intersectsCircle(point(60, 89), "112km");

        List<PolygonItem> result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("NORTH_POLE", result.get(0).getDocId());

        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(60, 89), "110km");

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testCircleOnSouthPole() {
        CircleCriterion criterion = (CircleCriterion) ICriterion.intersectsCircle(point(0, -90), "10m");

        List<PolygonItem> result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("SOUTH_POLE", result.get(0).getDocId());

        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(0, -90), "15000km");

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(6, result.size());
        Assert.assertEquals("SOUTH_POLE", result.get(0).getDocId());

        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(0, -90), "20000km");

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(7, result.size());
        Assert.assertEquals("NORTH_POLE", result.get(0).getDocId());
        Assert.assertEquals("SOUTH_POLE", result.get(1).getDocId());
    }

    @Test
    public void testCircleNearSouthPole() {
        CircleCriterion criterion = (CircleCriterion) ICriterion.intersectsCircle(point(60, -89), "112km");

        List<PolygonItem> result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(1, result.size());
        Assert.assertEquals("SOUTH_POLE", result.get(0).getDocId());

        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(60, -89), "110km");

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertTrue(result.isEmpty());
    }

    @Test
    public void testCircleOnEastPole() {
        CircleCriterion criterion = (CircleCriterion) ICriterion.intersectsCircle(point(180, 00), "10m");

        List<PolygonItem> result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("EAST_POLE", result.get(0).getDocId());
        Assert.assertEquals("WEST_POLE", result.get(1).getDocId());
    }

    @Test
    public void testCircleOnWestPole() {
        CircleCriterion criterion = (CircleCriterion) ICriterion.intersectsCircle(point(-180, 0), "10m");

        List<PolygonItem> result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("EAST_POLE", result.get(0).getDocId());
        Assert.assertEquals("WEST_POLE", result.get(1).getDocId());

        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(-180, 0), "2229.85km");

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(3, result.size());
        Assert.assertEquals("EAST_POLE", result.get(0).getDocId());
        Assert.assertEquals("WEST_POLE", result.get(1).getDocId());
        Assert.assertEquals("P1", result.get(2).getDocId());
        System.out.println();

        Integer d = 2211_170;
        criterion = (CircleCriterion) ICriterion.intersectsCircle(point(-180, 0), d.toString());

        System.out.printf("Error : %d m\n", (int)(GeoHelper.getDistanceOnEarth(point(-180, 0), point(180, 20)) - d));

        result = repository.search(searchKey, 1000, criterion).getContent();
        Assert.assertEquals(2, result.size());
        Assert.assertEquals("EAST_POLE", result.get(0).getDocId());
        Assert.assertEquals("WEST_POLE", result.get(1).getDocId());
    }

    @Test
    public void testPrecisionFor1degree() {
        System.out.println(GeoHelper.getDistanceOnEarth(point(180, 20), point(180, 21)));
        System.out.println(GeoHelper.getDistanceOnEarth(point(0, 89), point(0, 90)));

        Integer d1 = 111_252;
        CircleCriterion criterion = criterion = (CircleCriterion) ICriterion.intersectsCircle(point(180, 21), d1.toString());

        List<PolygonItem> result = repository.search(searchKey, 1000, criterion).getContent();

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("P1", result.get(0).getDocId());
        System.out.printf("Error near equator : %d m\n", (int)GeoHelper.getDistanceOnEarth(point(180, 20), point(180, 21)) - d1);

        Integer d2 = 110_708;
        criterion = criterion = (CircleCriterion) ICriterion.intersectsCircle(point(0, 89), d2.toString());

        result = repository.search(searchKey, 1000, criterion).getContent();

        Assert.assertEquals(1, result.size());
        Assert.assertEquals("NORTH_POLE", result.get(0).getDocId());
        System.out.printf("Error near pole : %d m\n", (int)GeoHelper.getDistanceOnEarth(point(0, 89), point(0, 90)) - d2);
    }

    private static class Item<T> implements IIndexable {

        private String id;

        private Geometry<T> geometry;

        public Item(String id, Geometry<T> geometry) {
            this.id = id;
            this.geometry = geometry;
        }

        @Override
        public String getDocId() {
            return id;
        }

        @Override
        public String getType() {
            return TYPE;
        }

        public Geometry<T> getGeometry() {
            return geometry;
        }

        public void setGeometry(Geometry<T> geometry) {
            this.geometry = geometry;
        }
    }

    private static class PolygonItem extends Item<Double[][][]> {

        public PolygonItem(String id, Integer... coordinates) {
            super(id, new Polygon(simplePolygon(coordinates)));
        }
    }

    private enum GeometryType {
        Polygon
    }

    private static class Geometry<T> {

        private GeometryType type;

        private T coordinates;

        private String crs;

        public Geometry() {

        }

        public Geometry(GeometryType type, T coordinates) {
            this.type = type;
            this.coordinates = coordinates;
        }

        public GeometryType getType() {
            return type;
        }

        public void setType(GeometryType type) {
            this.type = type;
        }

        public T getCoordinates() {
            return coordinates;
        }

        public void setCoordinates(T coordinates) {
            this.coordinates = coordinates;
        }

        public String getCrs() {
            return crs;
        }

        public void setCrs(String crs) {
            this.crs = crs;
        }
    }

     private static class Polygon extends Geometry<Double[][][]> {
        public Polygon() {
            super(GeometryType.Polygon, null);
        }

        public Polygon(Double[][][] coordinates) {
            super(GeometryType.Polygon, coordinates);
        }
    }
}
