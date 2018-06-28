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

import java.util.EnumMap;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Geo spatial utilities class
 * @author oroussel
 */
public class GeoHelper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GeoHelper.class);

    private static final Table<Crs, Crs, MathTransform> TRANSFORM_TABLE = HashBasedTable.create();

    /**
     * Geodetic calculator map
     */
    private static EnumMap<Crs, GeodeticCalculator> calcMap = new EnumMap<>(Crs.class);

    private static EnumMap<Crs, CoordinateReferenceSystem> crsMap = new EnumMap<>(Crs.class);

    /**
     * Geodetic calculator map initializer
     */
    static {
        for (Crs crs : Crs.values()) {
            try {
                CoordinateReferenceSystem coordinateReferenceSystem = CRS.parseWKT(crs.getWkt());
                crsMap.put(crs, coordinateReferenceSystem);
                calcMap.put(crs, new GeodeticCalculator(coordinateReferenceSystem));
            } catch (FactoryException e) {
                LOGGER.error("Bad WKT", e);
            }

        }
        for (Crs crs : Crs.values()) {
            for (Crs otherCrs : Crs.values()) {
                if (crs != otherCrs) {
                    try {
                        MathTransform transform = CRS.findMathTransform(crsMap.get(crs), crsMap.get(otherCrs), true);
                        TRANSFORM_TABLE.put(crs, otherCrs, transform);
                    } catch (FactoryException e) {
                        LOGGER.error("No math transform can be created for given source and destination", e);
                    }
                }
            }
        }
    }

    public static double getDistanceOnEarth(double[] lonLat1, double[] lonLat2) {
        return getDistance(lonLat1, lonLat2, Crs.WGS_84);
    }

    public static double getDistanceOnEarth(double lon1, double lat1, double lon2, double lat2) {
        return getDistance(lon1, lat1, lon2, lat2, Crs.WGS_84);
    }

    public static double getDistanceOnMars(double[] lonLat1, double[] lonLat2) {
        return getDistance(lonLat1, lonLat2, Crs.MARS_49900);
    }

    public static double getDistanceOnMars(double lon1, double lat1, double lon2, double lat2) {
        return getDistance(lon1, lat1, lon2, lat2, Crs.MARS_49900);
    }

    public static double getDistance(double[] lonLat1, double[] lonLat2, Crs crs) {
        return getDistance(lonLat1[0], lonLat1[1], lonLat2[0], lonLat2[1], crs);
    }

    public static double getDistance(double lon1, double lat1, double lon2, double lat2, Crs crs) {
        GeodeticCalculator calc = calcMap.get(crs);
        calc.setStartingGeographicPoint(lon1, lat1);
        calc.setDestinationGeographicPoint(lon2, lat2);
        return calc.getOrthodromicDistance();
    }

    public static double[] getPointAtDirectionOnEarth(double srcLon, double srcLat, double azimuth, double distance)
            throws TransformException {
        return getPointAtDirection(srcLon, srcLat, azimuth, distance, Crs.WGS_84);
    }

    public static double[] getPointAtDirectionOnEarth(double[] srcLonLat, double azimuth, double distance)
            throws TransformException {
        return getPointAtDirection(srcLonLat, azimuth, distance, Crs.WGS_84);
    }

    public static double[] getPointAtDirectionOnMars(double srcLon, double srcLat, double azimuth, double distance)
            throws TransformException {
        return getPointAtDirection(srcLon, srcLat, azimuth, distance, Crs.MARS_49900);
    }

    public static double[] getPointAtDirectionOnMars(double[] srcLonLat, double azimuth, double distance)
            throws TransformException {
        return getPointAtDirection(srcLonLat, azimuth, distance, Crs.MARS_49900);
    }

    public static double[] getPointAtDirection(double[] srcLonLat, double azimuth, double distance, Crs crs)
            throws TransformException {
        GeodeticCalculator calc = calcMap.get(crs);
        calc.setStartingGeographicPoint(srcLonLat[0], srcLonLat[1]);
        calc.setDirection(azimuth, distance);
        return calc.getDestinationPosition().getCoordinate();
    }


    public static double[] getPointAtDirection(double srcLon, double srcLat, double azimuth, double distance, Crs crs)
            throws TransformException {
        GeodeticCalculator calc = calcMap.get(crs);
        calc.setStartingGeographicPoint(srcLon, srcLat);
        calc.setDirection(azimuth, distance);
        return calc.getDestinationPosition().getCoordinate();
    }

    public static double[] transform(double[] fromPoint, Crs fromCrs, Crs toCrs) throws TransformException {
        MathTransform transform = TRANSFORM_TABLE.get(fromCrs, toCrs);
        DirectPosition srcPos = new DirectPosition2D(fromPoint[0], fromPoint[1]);
        DirectPosition destPos = transform.transform(srcPos, null);
        return destPos.getCoordinate();
    }
}
