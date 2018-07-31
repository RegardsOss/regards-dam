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

import org.junit.Assert;
import org.junit.Test;
import org.opengis.referencing.operation.TransformException;

import com.google.common.collect.Lists;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.modules.indexer.dao.EsHelper;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;

/**
 * @author oroussel
 */
public class GeoHelperTest {

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

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
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
        polygon = GeoHelper.normalizePolygon(polygon);
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
        polygon = GeoHelper.normalizePolygon(polygon);
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
        polygon = GeoHelper.normalizePolygon(polygon);
        System.out.println(displayGeoJson(polygon));
        Assert.assertEquals("POLYGON ( EXTERIOR ( [ -15.0, 68.0 ], [ 120.0, 68.0 ], [ 120.0, 66.5 ], [ 217.5, 66.5 ], "
                                    + "[ 217.5, 60.0 ], [ 203.74995, 60.0 ], [ 203.74995, 57.0 ], [ 195.0, 57.0 ], [ 195.0, 50.0 ],"
                                    + " [ 210.0, 50.0 ], [ 210.0, 46.0 ], [ 235.00005, 46.0 ], [ 235.00005, 50.0 ],"
                                    + " [ 247.99995, 50.0 ], [ 247.99995, 55.0 ], [ 262.5, 55.0 ], [ 262.5, 60.0 ], "
                                    + "[ 270.0, 60.0 ], [ 270.0, 66.0 ], [ 315.0, 66.0 ], [ 315.0, 66.16666 ], [ 345.0, 66.16666 ],"
                                    + " [ 359.999999999999, 68.0 ], [ 359.999999999999, 90.0 ], [ -180.0, 90.0 ], [ -180.0, 68.0 ],"
                                    + " [ -15.0, 68.0 ] ) )", polygon.toString());
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
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
        polygon = GeoHelper.normalizePolygon(polygon);
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
        polygon = GeoHelper.normalizePolygon(polygon);
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
        polygon = GeoHelper.normalizePolygon(polygon);
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
     */
    @Test
    public void polygonNormalizationAroundSouthPoleTest1() {
        // Octans polygon: around north pole but passing through south pole (classic description of Octans)
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
        polygon = GeoHelper.normalizePolygon(polygon);
        System.out.println(displayGeoJson(polygon));
    }

    /**
     * Good tool for testing: http://jsfiddle.net/xbzxfx2L/543/
     */
    @Test
    public void polygonNormalizationAroundSouthPoleTest2() {
        // Octans polygon: around north pole but not passing through south pole (Beware: it is necessary to reverse
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
        polygon = GeoHelper.normalizePolygon(polygon);
        System.out.println(displayGeoJson(polygon));
    }

    /**
     * Goot tool for testing: http://jsfiddle.net/xbzxfx2L/543/
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
        polygon = GeoHelper.normalizePolygon(polygon);
        System.out.println(displayGeoJson(polygon));
    }

    @Test
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
        polygon = GeoHelper.normalizePolygon(polygon);
        System.out.println(displayGeoJson(polygon));
    }

    @Test
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
