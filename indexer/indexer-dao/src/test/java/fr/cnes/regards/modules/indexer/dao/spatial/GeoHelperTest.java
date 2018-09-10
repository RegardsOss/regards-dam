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
package fr.cnes.regards.modules.indexer.dao.spatial;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.opengis.referencing.operation.TransformException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringRunner;

import com.google.common.collect.Lists;
import fr.cnes.regards.framework.geojson.coordinates.Positions;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.LineString;
import fr.cnes.regards.framework.geojson.geometry.MultiLineString;
import fr.cnes.regards.framework.geojson.geometry.MultiPolygon;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.utils.spring.SpringContext;
import fr.cnes.regards.modules.indexer.dao.EsHelper;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;

/**
 * @author oroussel
 */
@RunWith(SpringRunner.class)
public class GeoHelperTest {

    @Autowired
    private ApplicationContext appContext;

    @Configuration
    static class GeoHelperConfiguration {

        @Bean
        public IProjectsClient getProjectsClient() {
            return Mockito.mock(IProjectsClient.class);
        }

        @Bean
        public ProjectGeoSettings getProjectGeoSettings() {
            ProjectGeoSettings settings = Mockito.mock(ProjectGeoSettings.class);
            Mockito.when(settings.getShouldManagePolesOnGeometries()).thenReturn(true);
            Mockito.when(settings.getCrs()).thenReturn(Crs.WGS_84);
            return settings;
        }

        @Bean
        public IRuntimeTenantResolver getTenantResolver() {
            return Mockito.mock(IRuntimeTenantResolver.class);
        }
    }

    /**
     * Creating SpringContext to avoid initializing all Spring configurations bullshits
     */
    @Before
    public void initSpringContext() throws IllegalAccessException, InstantiationException {
        // H4cker styleZ.... woooooooouh
        SpringContext springContext = SpringContext.class.newInstance();
        springContext.setApplicationContext(appContext);
    }

    @Test
    public void getDistanceOnMarsTest() {
        double distanceFromPoleToEquator = GeoHelper.getDistanceOnMars(0, 90, 0, 0);
        Assert.assertEquals(5319034.253283859, distanceFromPoleToEquator, 1);

        double demiCircumferenceAtEquator = GeoHelper.getDistanceOnMars(0, 0, 180, 0);
        Assert.assertEquals(1.0638068506567718E7, demiCircumferenceAtEquator, 1);
    }

    @Test
    public void marsToEarthProjectionTest() throws TransformException {
        double[] point45_45 = new double[] { 45, 45 };
        // 50 km -> North (on Mars)
        double[] point50kmToNorthOnMars = GeoHelper.getPointAtDirectionOnMars(point45_45, 0.0, 50_000);
        System.out.println(Arrays.toString(point50kmToNorthOnMars));
        // 50 km -> North (on Earth)
        double[] point50kmToNorthOnEarth = GeoHelper.getPointAtDirectionOnEarth(point45_45, 0.0, 50_000);
        System.out.println(Arrays.toString(point50kmToNorthOnEarth));
        // Same longitude
        Assert.assertEquals(point50kmToNorthOnEarth[0], point50kmToNorthOnMars[0], 0.001);
        // Mars latitude > Earth latitude
        Assert.assertTrue(point50kmToNorthOnMars[1] > point50kmToNorthOnEarth[1]);

        // 50 km -> East (on Mars)
        double[] point0_0 = new double[] { 0, 0 };
        double[] point50kmToEastOnMars = GeoHelper.getPointAtDirectionOnMars(point0_0, 90.0, 50_000);
        System.out.println(Arrays.toString(point50kmToEastOnMars));
        // 50 km -> East (on Earth)
        double[] point50kmToEastOnEarth = GeoHelper.getPointAtDirectionOnEarth(point0_0, 90.0, 50_000);
        System.out.println(Arrays.toString(point50kmToEastOnEarth));
        // Same latitude
        Assert.assertEquals(point50kmToEastOnEarth[1], point50kmToEastOnMars[1], 0.001);
        // Mars longitude > Earth longitude
        Assert.assertTrue(point50kmToEastOnMars[0] > point50kmToEastOnEarth[0]);

        double[] point60_60 = new double[] { 60.0, 60.0 };
        System.out.println(Arrays.toString(GeoHelper.transform(point45_45, Crs.MARS_49900, Crs.WGS_84)));
        System.out.println(Arrays.toString(GeoHelper.transform(point60_60, Crs.MARS_49900, Crs.WGS_84)));
        System.out.println(Arrays.toString(GeoHelper.transform(point0_0, Crs.MARS_49900, Crs.WGS_84)));
        double[] pointNorthPole = new double[] { 0.0, 90.0 };
        System.out.println(Arrays.toString(GeoHelper.transform(pointNorthPole, Crs.MARS_49900, Crs.WGS_84)));
    }

    @Test
    public void marsToEarthCircleOnNorthHemisphereProjectionTest() throws TransformException {
        double[] point_60_60_OnMars = new double[] { 60, 60 };
        double[] point50kmToSouthOnMars = GeoHelper.getPointAtDirectionOnMars(point_60_60_OnMars, 180.0, 50_000);
        double[] point50kmToNorthOnMars = GeoHelper.getPointAtDirectionOnMars(point_60_60_OnMars, 0.0, 50_000);

        double[] point_60_60_OnMarsProjOnEarth = GeoHelper.transform(point_60_60_OnMars, Crs.MARS_49900, Crs.WGS_84);
        double[] point50kmToSouthOnMarsProjOnEarth = GeoHelper
                .transform(point50kmToSouthOnMars, Crs.MARS_49900, Crs.WGS_84);
        double[] point50kmToNorthOnMarsProjOnEarth = GeoHelper
                .transform(point50kmToNorthOnMars, Crs.MARS_49900, Crs.WGS_84);
        // Distance to north > distance to south because of Mars flattening which is greatest than earth one
        Assert.assertTrue(GeoHelper.getDistanceOnEarth(point_60_60_OnMarsProjOnEarth, point50kmToSouthOnMarsProjOnEarth)
                                  > GeoHelper
                .getDistanceOnEarth(point_60_60_OnMarsProjOnEarth, point50kmToNorthOnMarsProjOnEarth));
    }

    @Test
    public void marsToEarthCircleOnSouthHemisphereProjectionTest() throws TransformException {
        double[] point_60_60_OnMars = new double[] { -60, -60 };
        double[] point50kmToSouthOnMars = GeoHelper.getPointAtDirectionOnMars(point_60_60_OnMars, 180.0, 50_000);
        double[] point50kmToNorthOnMars = GeoHelper.getPointAtDirectionOnMars(point_60_60_OnMars, 0.0, 50_000);

        double[] point_60_60_OnMarsProjOnEarth = GeoHelper.transform(point_60_60_OnMars, Crs.MARS_49900, Crs.WGS_84);
        double[] point50kmToSouthOnMarsProjOnEarth = GeoHelper
                .transform(point50kmToSouthOnMars, Crs.MARS_49900, Crs.WGS_84);
        double[] point50kmToNorthOnMarsProjOnEarth = GeoHelper
                .transform(point50kmToNorthOnMars, Crs.MARS_49900, Crs.WGS_84);
        // Distance to north > distance to south because of Mars flattening which is greatest than earth one
        Assert.assertTrue(GeoHelper.getDistanceOnEarth(point_60_60_OnMarsProjOnEarth, point50kmToSouthOnMarsProjOnEarth)
                                  < GeoHelper
                .getDistanceOnEarth(point_60_60_OnMarsProjOnEarth, point50kmToNorthOnMarsProjOnEarth));

    }

    @Test
    public void containsGeoCriterionTest() {
        ICriterion criterion = ICriterion.and(ICriterion.in("toto", "text1", "text2"), ICriterion.eq("count", 25),
                                              ICriterion.or(ICriterion.ge("altitude", 2552.36), ICriterion
                                                      .intersectsCircle(new double[] { 45, 45 }, "50m")));

        Assert.assertTrue(GeoHelper.containsCircleCriterion(criterion));
        Assert.assertFalse(GeoHelper.containsPolygonOrBboxCriterion(criterion));

        criterion = ICriterion.and(ICriterion.in("toto", "text1", "text2"), ICriterion.eq("count", 25), ICriterion
                .or(ICriterion.ge("altitude", 2552.36), ICriterion.intersectsPolygon(new double[][][] {})));

        Assert.assertTrue(GeoHelper.containsPolygonOrBboxCriterion(criterion));
        Assert.assertFalse(GeoHelper.containsCircleCriterion(criterion));
    }

    /**
     * Transform right ascendance in decimal hours to longitude in degrees
     */
    private double toLongitude(double rightAscendance) {
        double longitude_0_360 = (rightAscendance / 24.) * 360;
        return EsHelper.highScaled(((longitude_0_360 >= 180.) ? longitude_0_360 - 360.0 : longitude_0_360));
    }

    private static String displayGeoJson(Polygon polygon) {
        return "{" + "  \"type\": \"FeatureCollection\"," + "  \"features\": [" + "  {" + "      \"type\": \"Feature\","
                + "      \"properties\": {" + "      }," + "      \"geometry\": {" + "        \"type\": \"Polygon\",\n"
                + "        \"coordinates\": [" + "          [\n" + polygon.getCoordinates().getExteriorRing().toString()
                + "] ]\n" + "      }" + "  }" + "    ]" + "}";
    }

    private static String displayGeoJson(MultiPolygon multiPolygon) {
        return "{" + "  \"type\": \"FeatureCollection\"," + "  \"features\": [" + "  {" + "      \"type\": \"Feature\","
                + "      \"properties\": {" + "      }," + "      \"geometry\": {"
                + "        \"type\": \"MultiPolygon\",\n" + "        \"coordinates\": [" + "          [\n"
                + multiPolygon.getCoordinates().stream().map(p -> p.getExteriorRing().toString())
                .collect(Collectors.joining("], [", "[", "]")) + "] ]\n" + "      }" + "  }" + "    ]" + "}";
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundNorthPoleTest1() {
        // Ursa Minor polygon: around north pole, latitude of right border = latitude of left border
        // longitude amplitude : from 120° (8h) to 345° (23h)
        Polygon polygon = IGeometry.simplePolygon(toLongitude(13.00000), 77.00000, //
                                                  toLongitude(13.00000), 70.00000, //
                                                  toLongitude(14.00000), 70.00000, //
                                                  toLongitude(14.00000), 66.00000, //
                                                  toLongitude(15.66667), 66.00000, //
                                                  toLongitude(15.66667), 70.00000, //
                                                  toLongitude(16.53333), 70.00000, //
                                                  toLongitude(16.53333), 75.00000, //
                                                  toLongitude(17.50000), 75.00000, //
                                                  toLongitude(17.50000), 80.00000, //
                                                  toLongitude(18.00000), 80.00000, //
                                                  toLongitude(18.00000), 86.00000, //
                                                  toLongitude(21.00000), 86.00000, //
                                                  toLongitude(21.00000), 86.16666, //
                                                  toLongitude(23.00000), 86.16666, //
                                                  toLongitude(23.00000), 88.00000, //
                                                  toLongitude(8.00000), 88.00000, //
                                                  toLongitude(8.00000), 86.50000, //
                                                  toLongitude(14.50000), 86.50000, //
                                                  toLongitude(14.50000), 80.00000, //
                                                  toLongitude(13.58333), 80.00000, //
                                                  toLongitude(13.58333), 77.00000);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        System.out.println(displayGeoJson(polygon));
        Assert.assertEquals("POLYGON ( EXTERIOR ( [ 195.0, 77.0 ], [ 195.0, 70.0 ], [ 210.0, 70.0 ], [ 210.0, 66.0 ], "
                                    + "[ 235.00005, 66.0 ], [ 235.00005, 70.0 ], [ 247.99995, 70.0 ], [ 247.99995, 75.0 ], "
                                    + "[ 262.5, 75.0 ], [ 262.5, 80.0 ], [ 270.0, 80.0 ], [ 270.0, 86.0 ], [ 315.0, 86.0 ], "
                                    + "[ 315.0, 86.16666 ], [ 345.0, 86.16666 ], [ 345.0, 88.0 ], [ 359.999999999999, 88.0 ], "
                                    + "[ 359.999999999999, 90.0 ], [ 0.0, 90.0 ], [ 0.0, 88.0 ], [ 120.0, 88.0 ], [ 120.0, 86.5 ], "
                                    + "[ 217.5, 86.5 ], [ 217.5, 80.0 ], [ 203.74995, 80.0 ], [ 203.74995, 77.0 ], "
                                    + "[ 195.0, 77.0 ] ) )", polygon.toString());
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundNorthPoleTest11() {
        // Ursa Minor polygon: around north pole, latitude of right border = latitude of left border
        // longitude amplitude : from 120° (8h) to 345° (23h)
        // Cycling polygon points
        Polygon polygon = IGeometry.simplePolygon(toLongitude(8.00000), 88.00000, //
                                                  toLongitude(8.00000), 86.50000, //
                                                  toLongitude(14.50000), 86.50000, //
                                                  toLongitude(14.50000), 80.00000, //
                                                  toLongitude(13.58333), 80.00000, //
                                                  toLongitude(13.58333), 77.00000, //
                                                  toLongitude(13.00000), 77.00000, //
                                                  toLongitude(13.00000), 70.00000, //
                                                  toLongitude(14.00000), 70.00000, //
                                                  toLongitude(14.00000), 66.00000, //
                                                  toLongitude(15.66667), 66.00000, //
                                                  toLongitude(15.66667), 70.00000, //
                                                  toLongitude(16.53333), 70.00000, //
                                                  toLongitude(16.53333), 75.00000, //
                                                  toLongitude(17.50000), 75.00000, //
                                                  toLongitude(17.50000), 80.00000, //
                                                  toLongitude(18.00000), 80.00000, //
                                                  toLongitude(18.00000), 86.00000, //
                                                  toLongitude(21.00000), 86.00000, //
                                                  toLongitude(21.00000), 86.16666, //
                                                  toLongitude(23.00000), 86.16666, //
                                                  toLongitude(23.00000), 88.00000);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        System.out.println(displayGeoJson(polygon));
        Assert.assertEquals("POLYGON ( EXTERIOR ( [ 120.0, 88.0 ], [ 120.0, 86.5 ], [ 217.5, 86.5 ], [ 217.5, 80.0 ], "
                                    + "[ 203.74995, 80.0 ], [ 203.74995, 77.0 ], [ 195.0, 77.0 ], [ 195.0, 70.0 ], [ 210.0, 70.0 ], "
                                    + "[ 210.0, 66.0 ], [ 235.00005, 66.0 ], [ 235.00005, 70.0 ], [ 247.99995, 70.0 ], "
                                    + "[ 247.99995, 75.0 ], [ 262.5, 75.0 ], [ 262.5, 80.0 ], [ 270.0, 80.0 ], [ 270.0, 86.0 ], "
                                    + "[ 315.0, 86.0 ], [ 315.0, 86.16666 ], [ 345.0, 86.16666 ], [ 345.0, 88.0 ], "
                                    + "[ 359.999999999999, 88.0 ], [ 359.999999999999, 90.0 ], [ 0.0, 90.0 ], [ 0.0, 88.0 ], "
                                    + "[ 120.0, 88.0 ] ) )", polygon.toString());
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundNorthPoleTest12() {
        // Ursa Minor polygon: around north pole, latitude of right border = latitude of left border
        // longitude amplitude : from 120° (8h) to 345° (23h)
        // Cycling polygon points and 20° bottomer
        Polygon polygon = IGeometry.simplePolygon(toLongitude(23.00000), 68.00000, //
                                                  toLongitude(8.00000), 68.00000, //
                                                  toLongitude(8.00000), 66.50000, //
                                                  toLongitude(14.50000), 66.50000, //
                                                  toLongitude(14.50000), 60.00000, //
                                                  toLongitude(13.58333), 60.00000, //
                                                  toLongitude(13.58333), 57.00000, //
                                                  toLongitude(13.00000), 57.00000, //
                                                  toLongitude(13.00000), 50.00000, //
                                                  toLongitude(14.00000), 50.00000, //
                                                  toLongitude(14.00000), 46.00000, //
                                                  toLongitude(15.66667), 46.00000, //
                                                  toLongitude(15.66667), 50.00000, //
                                                  toLongitude(16.53333), 50.00000, //
                                                  toLongitude(16.53333), 55.00000, //
                                                  toLongitude(17.50000), 55.00000, //
                                                  toLongitude(17.50000), 60.00000, //
                                                  toLongitude(18.00000), 60.00000, //
                                                  toLongitude(18.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.16666, //
                                                  toLongitude(23.00000), 66.16666);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        System.out.println(displayGeoJson(polygon));
        Assert.assertEquals(
                "POLYGON ( EXTERIOR ( [ -15.0, 68.0 ], [ 120.0, 68.0 ], [ 120.0, 66.5 ], [ 217.5, 66.5 ], [ 217.5, 60.0 ], [ 203.74995, 60.0 ], [ 203.74995, 57.0 ], [ 195.0, 57.0 ], [ 195.0, 50.0 ], [ 210.0, 50.0 ], [ 210.0, 46.0 ], [ 235.00005, 46.0 ], [ 235.00005, 50.0 ], [ 247.99995, 50.0 ], [ 247.99995, 55.0 ], [ 262.5, 55.0 ], [ 262.5, 60.0 ], [ 270.0, 60.0 ], [ 270.0, 66.0 ], [ 315.0, 66.0 ], [ 315.0, 66.16666 ], [ 345.0, 66.16666 ], [ 180.0, 68.0 ], [ 180.0, 90.0 ], [ -180.0, 90.0 ], [ -180.0, 68.0 ], [ -15.0, 68.0 ] ) )",
                polygon.toString());
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundNorthPoleTest13() {
        // Ursa Minor polygon: around north pole, latitude of right border = latitude of left border
        // longitude amplitude : from 120° (8h) to 345° (23h)
        // Cycling polygon points and 20° bottomer
        Polygon polygon = IGeometry.simplePolygon(toLongitude(14.50000), 66.50000, //
                                                  toLongitude(14.50000), 60.00000, //
                                                  toLongitude(13.58333), 60.00000, //
                                                  toLongitude(13.58333), 57.00000, //
                                                  toLongitude(13.00000), 57.00000, //
                                                  toLongitude(13.00000), 50.00000, //
                                                  toLongitude(14.00000), 50.00000, //
                                                  toLongitude(14.00000), 46.00000, //
                                                  toLongitude(15.66667), 46.00000, //
                                                  toLongitude(15.66667), 50.00000, //
                                                  toLongitude(16.53333), 50.00000, //
                                                  toLongitude(16.53333), 55.00000, //
                                                  toLongitude(17.50000), 55.00000, //
                                                  toLongitude(17.50000), 60.00000, //
                                                  toLongitude(18.00000), 60.00000, //
                                                  toLongitude(18.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.16666, //
                                                  toLongitude(23.00000), 66.16666, //
                                                  toLongitude(23.00000), 68.00000, //
                                                  toLongitude(8.00000), 68.00000, //
                                                  toLongitude(8.00000), 66.50000 //
        );
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        System.out.println(displayGeoJson(polygon));
        Assert.assertEquals("POLYGON ( EXTERIOR ( [ 217.5, 66.5 ], [ 217.5, 60.0 ], [ 203.74995, 60.0 ], "
                                    + "[ 203.74995, 57.0 ], [ 195.0, 57.0 ], [ 195.0, 50.0 ], [ 210.0, 50.0 ], "
                                    + "[ 210.0, 46.0 ], [ 235.00005, 46.0 ], [ 235.00005, 50.0 ], [ 247.99995, 50.0 ],"
                                    + " [ 247.99995, 55.0 ], [ 262.5, 55.0 ], [ 262.5, 60.0 ], [ 270.0, 60.0 ],"
                                    + " [ 270.0, 66.0 ], [ 315.0, 66.0 ], [ 315.0, 66.16666 ], [ 345.0, 66.16666 ],"
                                    + " [ 345.0, 68.0 ], [ 359.999999999999, 68.0 ], [ 359.999999999999, 90.0 ], "
                                    + "[ 0.0, 90.0 ], [ 0.0, 68.0 ], [ 120.0, 68.0 ], [ 120.0, 66.5 ], "
                                    + "[ 217.5, 66.5 ] ) )", polygon.toString());
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundNorthPoleTest2() {
        // Ursa Minor modified polygon (20° bottomer): around north pole, latitude of right border > latitude of left border
        // longitude amplitude : from 120° (8h) to 345° (23h)
        Polygon polygon = IGeometry.simplePolygon(toLongitude(13.00000), 57.00000, //
                                                  toLongitude(13.00000), 50.00000, //
                                                  toLongitude(14.00000), 50.00000, //
                                                  toLongitude(14.00000), 46.00000, //
                                                  toLongitude(15.66667), 46.00000, //
                                                  toLongitude(15.66667), 50.00000, //
                                                  toLongitude(16.53333), 50.00000, //
                                                  toLongitude(16.53333), 55.00000, //
                                                  toLongitude(17.50000), 55.00000, //
                                                  toLongitude(17.50000), 60.00000, //
                                                  toLongitude(18.00000), 60.00000, //
                                                  toLongitude(18.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.16666, //
                                                  toLongitude(23.00000), 66.16666, //
                                                  toLongitude(23.00000), 75.00000, //
                                                  toLongitude(8.00000), 68.00000, //
                                                  toLongitude(8.00000), 66.50000, //
                                                  toLongitude(14.50000), 66.50000, //
                                                  toLongitude(14.50000), 60.00000, //
                                                  toLongitude(13.58333), 60.00000, //
                                                  toLongitude(13.58333), 57.00000);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        System.out.println(displayGeoJson(polygon));
        Assert.assertEquals("POLYGON ( EXTERIOR ( [ 195.0, 57.0 ], [ 195.0, 50.0 ], [ 210.0, 50.0 ], "
                                    + "[ 210.0, 46.0 ], [ 235.00005, 46.0 ], [ 235.00005, 50.0 ], [ 247.99995, 50.0 ], "
                                    + "[ 247.99995, 55.0 ], [ 262.5, 55.0 ], [ 262.5, 60.0 ], [ 270.0, 60.0 ], "
                                    + "[ 270.0, 66.0 ], [ 315.0, 66.0 ], [ 315.0, 66.16666 ], [ 345.0, 66.16666 ], "
                                    + "[ 345.0, 75.0 ], [ 359.999999999999, 74.22222222222227 ], "
                                    + "[ 359.999999999999, 90.0 ], [ 0.0, 90.0 ], [ 0.0, 74.22222222222227 ], "
                                    + "[ 120.0, 68.0 ], [ 120.0, 66.5 ], [ 217.5, 66.5 ], [ 217.5, 60.0 ], "
                                    + "[ 203.74995, 60.0 ], [ 203.74995, 57.0 ], [ 195.0, 57.0 ] ) )",
                            polygon.toString());
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundNorthPoleTest3() {
        // Ursa Minor modified polygon (20° bottomer): around north pole, latitude of right border < latitude of left border
        // longitude amplitude : from 120° (8h) to 345° (23h)
        Polygon polygon = IGeometry.simplePolygon(toLongitude(13.00000), 57.00000, //
                                                  toLongitude(13.00000), 50.00000, //
                                                  toLongitude(14.00000), 50.00000, //
                                                  toLongitude(14.00000), 46.00000, //
                                                  toLongitude(15.66667), 46.00000, //
                                                  toLongitude(15.66667), 50.00000, //
                                                  toLongitude(16.53333), 50.00000, //
                                                  toLongitude(16.53333), 55.00000, //
                                                  toLongitude(17.50000), 55.00000, //
                                                  toLongitude(17.50000), 60.00000, //
                                                  toLongitude(18.00000), 60.00000, //
                                                  toLongitude(18.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.00000, //
                                                  toLongitude(21.00000), 66.16666, //
                                                  toLongitude(23.00000), 66.16666, //
                                                  toLongitude(23.00000), 68.00000, //
                                                  toLongitude(8.00000), 75.00000, //
                                                  toLongitude(8.00000), 66.50000, //
                                                  toLongitude(14.50000), 66.50000, //
                                                  toLongitude(14.50000), 60.00000, //
                                                  toLongitude(13.58333), 60.00000, //
                                                  toLongitude(13.58333), 57.00000);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        System.out.println(displayGeoJson(polygon));
        Assert.assertEquals("POLYGON ( EXTERIOR ( [ 195.0, 57.0 ], [ 195.0, 50.0 ], [ 210.0, 50.0 ], "
                                    + "[ 210.0, 46.0 ], [ 235.00005, 46.0 ], [ 235.00005, 50.0 ], [ 247.99995, 50.0 ], "
                                    + "[ 247.99995, 55.0 ], [ 262.5, 55.0 ], [ 262.5, 60.0 ], [ 270.0, 60.0 ], "
                                    + "[ 270.0, 66.0 ], [ 315.0, 66.0 ], [ 315.0, 66.16666 ], [ 345.0, 66.16666 ], "
                                    + "[ 345.0, 68.0 ], [ 359.999999999999, 68.77777777777773 ], "
                                    + "[ 359.999999999999, 90.0 ], [ 0.0, 90.0 ], [ 0.0, 68.77777777777773 ], "
                                    + "[ 120.0, 75.0 ], [ 120.0, 66.5 ], [ 217.5, 66.5 ], [ 217.5, 60.0 ], "
                                    + "[ 203.74995, 60.0 ], [ 203.74995, 57.0 ], [ 195.0, 57.0 ] ) )",
                            polygon.toString());
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundSouthPoleTest1() {
        // Octans polygon: around south pole but passing through south pole (classic description of Octans)
        Polygon polygon = IGeometry.simplePolygon(toLongitude(0.00000), -90.00000, //
                                                  toLongitude(0.00000), -82.50000, //
                                                  toLongitude(3.50000), -82.50000, //
                                                  toLongitude(3.50000), -85.00000, //
                                                  toLongitude(7.66667), -85.00000, //
                                                  toLongitude(7.66667), -82.50000, //
                                                  toLongitude(13.66667), -82.50000, //
                                                  toLongitude(18.00000), -82.50000, //
                                                  toLongitude(18.00000), -75.00000, //
                                                  toLongitude(21.33333), -75.00000, //
                                                  toLongitude(23.33333), -75.00000, //
                                                  toLongitude(24.00000), -75.00000, //
                                                  toLongitude(24.00000), -90.00000, //
                                                  toLongitude(12.00000), -90.00000);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        Assert.assertEquals(
                "{  \"type\": \"FeatureCollection\",  \"features\": [  {      \"type\": \"Feature\",      \"properties\": {      },      \"geometry\": {        \"type\": \"Polygon\",\n"
                        + "        \"coordinates\": [          [\n"
                        + "[ 0.0, -90.0 ], [ 0.0, -82.5 ], [ 52.5, -82.5 ], [ 52.5, -85.0 ], [ 115.00005, -85.0 ], [ 115.00005, -82.5 ], [ 205.00005, -82.5 ], [ 270.0, -82.5 ], [ 270.0, -75.0 ], [ 319.99995, -75.0 ], [ 349.99995, -75.0 ], [ 359.999999999999, -75.0 ], [ 359.999999999999, -90.0 ], [ 180.0, -90.0 ], [ 0.0, -90.0 ]] ]\n"
                        + "      }  }    ]}", displayGeoJson(polygon));
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationAroundSouthPoleTest2() {
        // Octans polygon: around south pole but not passing through south pole (Beware: it is necessary to reverse
        // polygon description to have left hand inside polygon)
        Polygon polygon = IGeometry.simplePolygon(toLongitude(24.00000), -75.00000, //
                                                  toLongitude(23.33333), -75.00000, //
                                                  toLongitude(21.33333), -75.00000, //
                                                  toLongitude(18.00000), -75.00000, //
                                                  toLongitude(18.00000), -82.50000, //
                                                  toLongitude(13.66667), -82.50000, //
                                                  toLongitude(7.66667), -82.50000, //
                                                  toLongitude(7.66667), -85.00000, //
                                                  toLongitude(3.50000), -85.00000, //
                                                  toLongitude(3.50000), -82.50000, //
                                                  toLongitude(0.00000), -82.50000);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        Assert.assertEquals(
                "{  \"type\": \"FeatureCollection\",  \"features\": [  {      \"type\": \"Feature\",      \"properties\": {      },      \"geometry\": {        \"type\": \"Polygon\",\n"
                        + "        \"coordinates\": [          [\n"
                        + "[ 0.0, -75.0 ], [ -10.00005, -75.0 ], [ -40.00005, -75.0 ], [ -90.0, -75.0 ], [ -90.0, -82.5 ], [ 0.0, -82.5 ], [ 0.0, -90.0 ], [ 359.999999999999, -90.0 ], [ 359.999999999999, -82.5 ], [ 205.00005, -82.5 ], [ 115.00005, -82.5 ], [ 115.00005, -85.0 ], [ 52.5, -85.0 ], [ 52.5, -82.5 ], [ 0.0, -82.5 ], [ 0.0, -75.0 ]] ]\n"
                        + "      }  }    ]}", displayGeoJson(polygon));
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     * or geojson.io
     */
    @Test
    public void polygonNormalizationCrossingDatelineTest() {
        Polygon polygon = IGeometry.simplePolygon(toLongitude(11.00000), -35.00000, toLongitude(11.00000), -39.75000,
                                                  toLongitude(11.00000), -56.50000, toLongitude(11.25000), -56.50000,
                                                  toLongitude(11.25000), -64.00000, toLongitude(11.83333), -64.00000,
                                                  toLongitude(11.83333), -55.00000, toLongitude(12.83333), -55.00000,
                                                  toLongitude(12.83333), -64.00000, toLongitude(13.50000), -64.00000,
                                                  toLongitude(14.53333), -64.00000, toLongitude(14.53333), -55.00000,
                                                  toLongitude(14.16667), -55.00000, toLongitude(14.16667), -42.00000,
                                                  toLongitude(14.91667), -42.00000, toLongitude(14.91667), -29.50000,
                                                  toLongitude(12.58333), -29.50000, toLongitude(12.58333), -33.00000,
                                                  toLongitude(12.25000), -33.00000, toLongitude(12.25000), -35.00000);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        Assert.assertEquals(
                "{  \"type\": \"FeatureCollection\",  \"features\": [  {      \"type\": \"Feature\",      \"properties\": {      },      \"geometry\": {        \"type\": \"Polygon\",\n"
                        + "        \"coordinates\": [          [\n"
                        + "[ 165.0, -35.0 ], [ 165.0, -39.75 ], [ 165.0, -56.5 ], [ 168.75, -56.5 ], [ 168.75, -64.0 ], [ 177.49995, -64.0 ], [ 177.49995, -55.0 ], [ 192.49995, -55.0 ], [ 192.49995, -64.0 ], [ 202.5, -64.0 ], [ 217.99995, -64.0 ], [ 217.99995, -55.0 ], [ 212.50005, -55.0 ], [ 212.50005, -42.0 ], [ 223.75005, -42.0 ], [ 223.75005, -29.5 ], [ 188.74995, -29.5 ], [ 188.74995, -33.0 ], [ 183.75, -33.0 ], [ 183.75, -35.0 ], [ 165.0, -35.0 ]] ]\n"
                        + "      }  }    ]}", displayGeoJson(polygon));
    }

    @Test
    public void polygonAroundBothPolesTest() {
        Polygon polygon = IGeometry
                .simplePolygon(20, 0, 20, 80, 100, 80, 170, 80, -170, 80, -100, 80, 10, 80, 10, 0, 5, -80, -100, -80,
                               -170, -80, 170, -80, 100, -80, 15, -80);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        // Acceptable normalization (not perfect but this is a tricky case, it will be enough for now)
        Assert.assertEquals(
                "{  \"type\": \"FeatureCollection\",  \"features\": [  {      \"type\": \"Feature\",      \"properties\": {      },      \"geometry\": {        \"type\": \"Polygon\",\n"
                        + "        \"coordinates\": [          [\n"
                        + "[ 20.0, 0.0 ], [ 20.0, 80.0 ], [ 100.0, 80.0 ], [ 170.0, 80.0 ], [ 190.0, 80.0 ], [ 260.0, 80.0 ], [ 359.999999999999, 80.0 ], [ 359.999999999999, 90.0 ], [ 0.0, 90.0 ], [ 0.0, 80.0 ], [ 10.0, 80.0 ], [ 10.0, 0.0 ], [ 5.0, -80.0 ], [ -100.0, -80.0 ], [ 0.0, -80.0 ], [ 0.0, -90.0 ], [ 359.999999999999, -90.0 ], [ 359.999999999999, -80.0 ], [ 190.0, -80.0 ], [ 170.0, -80.0 ], [ 100.0, -80.0 ], [ 15.0, -80.0 ], [ 20.0, 0.0 ]] ]\n"
                        + "      }  }    ]}", displayGeoJson(polygon));
    }

    @Test
    public void multiPolygonNormalizationTest() {
        Polygon octansPolygon = IGeometry.simplePolygon(toLongitude(24.00000), -75.00000, //
                                                        toLongitude(23.33333), -75.00000, //
                                                        toLongitude(21.33333), -75.00000, //
                                                        toLongitude(18.00000), -75.00000, //
                                                        toLongitude(18.00000), -82.50000, //
                                                        toLongitude(13.66667), -82.50000, //
                                                        toLongitude(7.66667), -82.50000, //
                                                        toLongitude(7.66667), -85.00000, //
                                                        toLongitude(3.50000), -85.00000, //
                                                        toLongitude(3.50000), -82.50000, //
                                                        toLongitude(0.00000), -82.50000);
        Polygon ursaMinorPolygon = IGeometry.simplePolygon(toLongitude(14.50000), 66.50000, //
                                                           toLongitude(14.50000), 60.00000, //
                                                           toLongitude(13.58333), 60.00000, //
                                                           toLongitude(13.58333), 57.00000, //
                                                           toLongitude(13.00000), 57.00000, //
                                                           toLongitude(13.00000), 50.00000, //
                                                           toLongitude(14.00000), 50.00000, //
                                                           toLongitude(14.00000), 46.00000, //
                                                           toLongitude(15.66667), 46.00000, //
                                                           toLongitude(15.66667), 50.00000, //
                                                           toLongitude(16.53333), 50.00000, //
                                                           toLongitude(16.53333), 55.00000, //
                                                           toLongitude(17.50000), 55.00000, //
                                                           toLongitude(17.50000), 60.00000, //
                                                           toLongitude(18.00000), 60.00000, //
                                                           toLongitude(18.00000), 66.00000, //
                                                           toLongitude(21.00000), 66.00000, //
                                                           toLongitude(21.00000), 66.16666, //
                                                           toLongitude(23.00000), 66.16666, //
                                                           toLongitude(23.00000), 68.00000, //
                                                           toLongitude(8.00000), 68.00000, //
                                                           toLongitude(8.00000), 66.50000);
        MultiPolygon multiPolygon = IGeometry
                .multiPolygon(octansPolygon.getCoordinates(), ursaMinorPolygon.getCoordinates());
        System.out.println(displayGeoJson(multiPolygon));
        multiPolygon = (MultiPolygon) GeoHelper.normalize(multiPolygon);
        Assert.assertEquals(
                "{  \"type\": \"FeatureCollection\",  \"features\": [  {      \"type\": \"Feature\",      \"properties\": {      },      \"geometry\": {        \"type\": \"MultiPolygon\",\n"
                        + "        \"coordinates\": [          [\n"
                        + "[[ 0.0, -75.0 ], [ -10.00005, -75.0 ], [ -40.00005, -75.0 ], [ -90.0, -75.0 ], [ -90.0, -82.5 ], [ 0.0, -82.5 ], [ 0.0, -90.0 ], [ 359.999999999999, -90.0 ], [ 359.999999999999, -82.5 ], [ 205.00005, -82.5 ], [ 115.00005, -82.5 ], [ 115.00005, -85.0 ], [ 52.5, -85.0 ], [ 52.5, -82.5 ], [ 0.0, -82.5 ], [ 0.0, -75.0 ]], [[ 217.5, 66.5 ], [ 217.5, 60.0 ], [ 203.74995, 60.0 ], [ 203.74995, 57.0 ], [ 195.0, 57.0 ], [ 195.0, 50.0 ], [ 210.0, 50.0 ], [ 210.0, 46.0 ], [ 235.00005, 46.0 ], [ 235.00005, 50.0 ], [ 247.99995, 50.0 ], [ 247.99995, 55.0 ], [ 262.5, 55.0 ], [ 262.5, 60.0 ], [ 270.0, 60.0 ], [ 270.0, 66.0 ], [ 315.0, 66.0 ], [ 315.0, 66.16666 ], [ 345.0, 66.16666 ], [ 345.0, 68.0 ], [ 359.999999999999, 68.0 ], [ 359.999999999999, 90.0 ], [ 0.0, 90.0 ], [ 0.0, 68.0 ], [ 120.0, 68.0 ], [ 120.0, 66.5 ], [ 217.5, 66.5 ]]] ]\n"
                        + "      }  }    ]}", displayGeoJson(multiPolygon));
    }

    @Test
    public void lineStringNormalizationTest() {
        LineString lineString = IGeometry.lineString(10, 45, 20, 45, 30, 45);
        Assert.assertEquals(lineString, GeoHelper.normalize(lineString));

        lineString = IGeometry.lineString(350, 45, 20, 45, 30, 45);
        Assert.assertEquals(IGeometry.lineString(-10, 45, 20, 45, 30, 45), GeoHelper.normalize(lineString));

        lineString = IGeometry.lineString(-10, 45, 20, 45, 30, 45);
        Assert.assertEquals(lineString, GeoHelper.normalize(lineString));

        lineString = IGeometry.lineString(-170, 45, 20, 45, 30, 55);
        Assert.assertNotEquals(lineString, GeoHelper.normalize(lineString));
        Assert.assertTrue(GeoHelper.normalize(lineString) instanceof MultiLineString);
        MultiLineString multiLineString = (MultiLineString) GeoHelper.normalize(lineString);
        List<Positions> positionsList = multiLineString.getCoordinates();
        Assert.assertEquals(2, positionsList.size());
        Assert.assertEquals(IGeometry.lineString(-170, 45, -180, 45).getCoordinates(), positionsList.get(0));
        Assert.assertEquals(IGeometry.lineString(180, 45, 20, 45, 30, 55).getCoordinates(), positionsList.get(1));

        lineString = IGeometry.lineString(30, 55, 20, 45, -170, 45);
        Assert.assertNotEquals(lineString, GeoHelper.normalize(lineString));
        Assert.assertTrue(GeoHelper.normalize(lineString) instanceof MultiLineString);
        multiLineString = (MultiLineString) GeoHelper.normalize(lineString);
        positionsList = multiLineString.getCoordinates();
        Assert.assertEquals(2, positionsList.size());
        Assert.assertEquals(IGeometry.lineString(30, 55, 20, 45, 180, 45).getCoordinates(), positionsList.get(0));
        Assert.assertEquals(IGeometry.lineString(-180, 45, -170, 45).getCoordinates(), positionsList.get(1));

        lineString = IGeometry.lineString(90, 45, -90, -45);
        Assert.assertEquals(lineString, GeoHelper.normalize(lineString));

        lineString = IGeometry.lineString(100, 45, -100, -45);
        Assert.assertNotEquals(lineString, GeoHelper.normalize(lineString));
        Assert.assertTrue(GeoHelper.normalize(lineString) instanceof MultiLineString);
        multiLineString = (MultiLineString) GeoHelper.normalize(lineString);
        positionsList = multiLineString.getCoordinates();
        Assert.assertEquals(2, positionsList.size());
        Assert.assertEquals(IGeometry.lineString(100, 45, 180, 0).getCoordinates(), positionsList.get(0));
        Assert.assertEquals(IGeometry.lineString(-180, 0, -100, -45).getCoordinates(), positionsList.get(1));

        lineString = IGeometry.lineString(100, 50, -100, 0);
        Assert.assertNotEquals(lineString, GeoHelper.normalize(lineString));
        Assert.assertTrue(GeoHelper.normalize(lineString) instanceof MultiLineString);
        multiLineString = (MultiLineString) GeoHelper.normalize(lineString);
        positionsList = multiLineString.getCoordinates();
        Assert.assertEquals(2, positionsList.size());
        Assert.assertEquals(IGeometry.lineString(100, 50, 180, 25).getCoordinates(), positionsList.get(0));
        Assert.assertEquals(IGeometry.lineString(-180, 25, -100, 0).getCoordinates(), positionsList.get(1));

        lineString = IGeometry.lineString(100, 50, -100, 50, -100, -50, 100, -50);
        Assert.assertNotEquals(lineString, GeoHelper.normalize(lineString));
        Assert.assertTrue(GeoHelper.normalize(lineString) instanceof MultiLineString);
        multiLineString = (MultiLineString) GeoHelper.normalize(lineString);
        positionsList = multiLineString.getCoordinates();
        Assert.assertEquals(3, positionsList.size());
        Assert.assertEquals(IGeometry.lineString(100, 50, 180, 50).getCoordinates(), positionsList.get(0));
        Assert.assertEquals(IGeometry.lineString(-180, 50, -100, 50, -100, -50, -180, -50).getCoordinates(),
                            positionsList.get(1));
        Assert.assertEquals(IGeometry.lineString(180, -50, 100, -50).getCoordinates(), positionsList.get(2));
    }

    @Test
    public void multiLineStringNormalizationTest() {
        LineString lineString1 = IGeometry.lineString(100, 50, -100, 0);
        LineString lineString2 = IGeometry.lineString(100, 50, -100, 50, -100, -50, 100, -50);
        MultiLineString multiLineString = IGeometry
                .multiLineString(lineString1.getCoordinates(), lineString2.getCoordinates());
        MultiLineString normalized = (MultiLineString) GeoHelper.normalize(multiLineString);
        Assert.assertEquals(5, normalized.getCoordinates().size());
        List<Positions> positionsList = normalized.getCoordinates();
        Assert.assertEquals(IGeometry.lineString(100, 50, 180, 25).getCoordinates(), positionsList.get(0));
        Assert.assertEquals(IGeometry.lineString(-180, 25, -100, 0).getCoordinates(), positionsList.get(1));
        Assert.assertEquals(IGeometry.lineString(100, 50, 180, 50).getCoordinates(), positionsList.get(2));
        Assert.assertEquals(IGeometry.lineString(-180, 50, -100, 50, -100, -50, -180, -50).getCoordinates(),
                            positionsList.get(3));
        Assert.assertEquals(IGeometry.lineString(180, -50, 100, -50).getCoordinates(), positionsList.get(4));

    }

    /**
     * Utility method
     */
    @Test
    @Ignore
    public void polygonMoisiTest() throws IOException {
        String constellation =
                "15.08333 -03.25000 LIB  O\n" + "15.91667 -03.25000 LIB  O\n" + "15.91667 -08.00000 LIB  O\n"
                        + "15.91667 -20.00000 LIB  O\n" + "15.66667 -20.00000 LIB  O\n" + "15.66667 -29.50000 LIB  O\n"
                        + "14.91667 -29.50000 LIB  O\n" + "14.91667 -24.50000 LIB  O\n" + "14.25000 -24.50000 LIB  O\n"
                        + "14.25000 -22.00000 LIB  O\n" + "14.25000 -08.00000 LIB  O\n" + "14.66667 -08.00000 LIB  O\n"
                        + "14.66667  00.00000 LIB  O\n" + "15.08333  00.00000 LIB  O\n";
        List<Double> lonLatList = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new StringReader(constellation))) {
            String line = br.readLine();
            while (line != null) {
                String[] array = line.trim().split("\\s+");
                lonLatList.add(toLongitude(Double.parseDouble(array[0])));
                lonLatList.add(Double.parseDouble(array[1]));
                line = br.readLine();
            }
        }
        // Octans polygon: around north pole but passing through south pole (classic description of Octans)
        double[] lonLats = new double[lonLatList.size()];
        int i = 0;
        for (int j = lonLatList.size() - 2; j >= 0; j -= 2) {
            lonLats[i++] = lonLatList.get(j);
            lonLats[i++] = lonLatList.get(j + 1);
        }
        Polygon polygon = IGeometry.simplePolygon(lonLats);
        System.out.println(displayGeoJson(polygon));
        polygon = (Polygon) GeoHelper.normalize(polygon);
        System.out.println(displayGeoJson(polygon));
    }

    /**
     * Utility method
     */
    @Test
    @Ignore
    public void reverseConsetellationTest() throws IOException {
        String constellation =
                "15.08333  00.00000 LIB  O\n" + "14.66667  00.00000 LIB  O\n" + "14.66667 -08.00000 LIB  O\n"
                        + "14.25000 -08.00000 LIB  O\n" + "14.25000 -22.00000 LIB  O\n" + "14.25000 -24.50000 LIB  O\n"
                        + "14.91667 -24.50000 LIB  O\n" + "14.91667 -29.50000 LIB  O\n" + "15.66667 -29.50000 LIB  O\n"
                        + "15.66667 -20.00000 LIB  O\n" + "15.91667 -20.00000 LIB  O\n" + "15.91667 -08.00000 LIB  O\n"
                        + "15.91667 -03.25000 LIB  O\n" + "15.08333 -03.25000 LIB  O";
        List<String> lines = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new StringReader(constellation))) {
            String line = br.readLine();
            while (line != null) {
                lines.add(line);
                line = br.readLine();
            }
        }
        lines = Lists.reverse(lines);
        lines.forEach(l -> System.out.println(l));
    }

}
