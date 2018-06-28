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

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;
import org.opengis.referencing.operation.TransformException;

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
}
