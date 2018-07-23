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

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.function.Predicate;

import org.geotools.geometry.DirectPosition2D;
import org.geotools.referencing.CRS;
import org.geotools.referencing.GeodeticCalculator;
import org.hipparchus.util.FastMath;
import org.opengis.geometry.DirectPosition;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import fr.cnes.regards.framework.geojson.coordinates.Position;
import fr.cnes.regards.framework.geojson.geometry.GeometryCollection;
import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.geojson.geometry.IGeometryVisitor;
import fr.cnes.regards.framework.geojson.geometry.LineString;
import fr.cnes.regards.framework.geojson.geometry.MultiLineString;
import fr.cnes.regards.framework.geojson.geometry.MultiPoint;
import fr.cnes.regards.framework.geojson.geometry.MultiPolygon;
import fr.cnes.regards.framework.geojson.geometry.Point;
import fr.cnes.regards.framework.geojson.geometry.Polygon;
import fr.cnes.regards.framework.geojson.geometry.Unlocated;
import fr.cnes.regards.modules.indexer.domain.criterion.AbstractMultiCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BooleanMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.BoundaryBoxCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.DateRangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.EmptyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.FieldExistsCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterionVisitor;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.LongMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.NotCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchAnyCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.spatial.Crs;

/**
 * Geo spatial utilities class
 * @author oroussel
 */
public class GeoHelper {

    /**
     * Radius used by ASTRO projection (perfect sphere used, no flattening)
     */
    public static final double AUTHALIC_SPHERE_RADIUS = 6371007.0;

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

    public static double[][] transform(double[][] fromPoints, Crs fromCrs, Crs toCrs) throws TransformException {
        MathTransform transform = TRANSFORM_TABLE.get(fromCrs, toCrs);
        double[][] toPoints = new double[fromPoints.length][];
        for (int i = 0; i < fromPoints.length; i++) {
            double[] fromPoint = fromPoints[i];
            DirectPosition srcPos = new DirectPosition2D(fromPoint[0], fromPoint[1]);
            DirectPosition destPos = transform.transform(srcPos, null);
            toPoints[i] = destPos.getCoordinate();
        }
        return toPoints;
    }

    public static double[][][] transform(double[][][] fromPointsLines, Crs fromCrs, Crs toCrs)
            throws TransformException {
        MathTransform transform = TRANSFORM_TABLE.get(fromCrs, toCrs);
        double[][][] toPointsLines = new double[fromPointsLines.length][][];
        for (int i = 0; i < fromPointsLines.length; i++) {
            toPointsLines[i] = new double[fromPointsLines[i].length][];
            for (int j = 0; j < fromPointsLines[i].length; j++) {
                double[] fromPoint = fromPointsLines[i][j];
                DirectPosition srcPos = new DirectPosition2D(fromPoint[0], fromPoint[1]);
                DirectPosition destPos = transform.transform(srcPos, null);
                toPointsLines[i][j] = destPos.getCoordinate();
            }
        }
        return toPointsLines;
    }

    /**
     * Does criterion tree contain a CircleCriterion ?
     */
    public static boolean containsCircleCriterion(ICriterion criterion) {
        PredicateCriterionVisitor visitor = new PredicateCriterionVisitor(crit -> crit instanceof CircleCriterion);
        return criterion.accept(visitor);
    }

    /**
     * Find the (first) CircleCriterion from a ICriterion tree
     */
    public static CircleCriterion findCircleCriterion(ICriterion criterion) {
        return criterion.accept(new FinderCriterionVisitor<>(CircleCriterion.class));
    }

    /**
     * Find the (first) PolygonCriterion from a ICriterion tree
     */
    public static PolygonCriterion findPolygonCriterion(ICriterion criterion) {
        return criterion.accept(new FinderCriterionVisitor<>(PolygonCriterion.class));
    }


    /**
     * Does criterion tree contain a PolygonCriterion or a BoundaryBoxCriterion ?
     */
    public static boolean containsPolygonOrBboxCriterion(ICriterion criterion) {
        PredicateCriterionVisitor visitor = new PredicateCriterionVisitor(
                crit -> crit instanceof PolygonCriterion || crit instanceof BoundaryBoxCriterion);
        return criterion.accept(visitor);
    }

    /**
     * Determine wether or not given shape is nearer than distance from point into given Crs.
     * As a simplification, 9 more points are added into each polygon segments to be used to determine minimum distance
     * between a point and a segment
     */
    public static boolean isNearer(IGeometry shape, double[] point, double distance, Crs crs) {
        NearestFromDistanceGeometryVisitor visitor = new NearestFromDistanceGeometryVisitor(point, distance, crs);
        return shape.accept(visitor);
    }

    /**
     * Compute distance between given shape and point into given Crs.
     * As a simplification, 9 more points are added into each polygon segments to compute minimum distance between a
     * point and a segment
     */
    public static double getDistance(IGeometry shape, double[] point, Crs crs) {
        DistanceToPointGeometryVisitor visitor = new DistanceToPointGeometryVisitor(point, crs);
        return shape.accept(visitor);
    }

    /**
     * Return a list of ten positions between two points
     */
    private static List<Position> createIntermediatePositions(Position firstPos, Position lastPos) {
        List<Position> positions = new ArrayList<>();
        double latitudeStep = (lastPos.getLatitude() - firstPos.getLatitude()) / 9.0;
        double longitudeStep = (lastPos.getLongitude() - firstPos.getLongitude()) / 9.0;
        positions.add(firstPos);
        for (int n = 1; n < 9; n++) {
            positions.add(new Position(firstPos.getLongitude() + n * longitudeStep,
                                       firstPos.getLatitude() + n * latitudeStep));
        }
        positions.add(lastPos);
        return positions;
    }

    private static double distToDateline(double lon) {
        if (lon > 0) {
            if (lon < 180) {
                return 180 - lon;
            } else {
                return lon - 180;
            }
        } else { // lon < 0
            return -(-180 - lon);
        }
    }

    private static double distTo0(double lon) {
        if (lon > 0) {
            if (lon < 180) {
                return lon;
            } else {
                return 360 - lon;
            }
        } else { // lon < 0
            return - lon;
        }
    }

    public static boolean normalizeNextCoordinate(double[] previous, double[] next) {
        boolean updatePrevious = false;
        // Using only longitude values
        double lonP = previous[0];
        double lonN = next[0];
        LOGGER.trace("IN ({}, {})", lonP, lonN);
        // Previous longitude < 180
        if ((lonP >= 0) && (lonP < 180.0)) {
            // -180 <= next.longitude < 0
            if ((lonN < 0) && (lonN >= -180.0)) {
                // Minimum distance through dateline ?
                if (distTo0(lonP) + distTo0(lonN) > distToDateline(lonP) + distToDateline(lonN)) {
                    // In this case, use longitude > 180 numeric
                    next[0] += 360.0;
                }
            } else if ((lonN > 180) && (lonN < 360.0)) { // 180 <= next.longitude < 360
                // Minimum distance through 0 ?
                if (distTo0(lonP) + distTo0(lonN) < distToDateline(lonP) + distToDateline(lonN)) {
                    // In this case, use longitude < -180 numeric
                    next[0] -= 360.0;
                }
            }
        } else if (lonP >= 180.0) { // Previous longitude >= 180
            // -180 <= next.longitude < 0
            if ((lonN < 0) && (lonN >= -180.0)) {
                // Continue to use longitude > 180 numeric
                next[0] += 360.0;
            } else if (lonN == 0.0) {
                // Mac Gyver hack when previous longitude is >= 180 and next is 0
                // 0 makes Elasticsearch thinking polygon go from 180 to 0 through 90
                // To make it go from 180 to 0 through 270 we stop at 359.9999...
                // TODO this case is when latitude is -90 or 90. In theory it will be a good idea to test if it is the
                // case here
                next[0] = 359.999999;
            }
        } else { // Previous longitude < 0
            // Next longitude > 180
            if (lonN > 180) {
                // This is unusual, next has certainly been updated because of next case, so update previous accordingly
                // (helpful for recursivity)
                previous[0] += 360.0;
                updatePrevious = true;
            } else if ((lonN >= 0) && (lonN < 180)) { // 0 <= next longitude < 180
                // Minimum distance through dateline ?
                if (distToDateline(lonP) + distToDateline(lonN) < distTo0(lonP) + distTo0(lonN)) {
                    // THIS TIME UPDATE previous !!!!
                    previous[0] += 360.0;
                    updatePrevious = true;
                }
            }
        }
        LOGGER.trace(" -> OUT ({}, {})", previous[0], next[0]);
        return updatePrevious;
    }

    /**
     * ICriterion visitor applying specified predicate on all ICriterion tree nodes. Not leaf nodes return true if one
     * of their childs has respond true (Permit to find if tree contains a certain type of ICriterion)
     */
    private static class PredicateCriterionVisitor implements ICriterionVisitor<Boolean> {

        private Predicate<ICriterion> predicate;

        public PredicateCriterionVisitor(Predicate<ICriterion> predicate) {
            this.predicate = predicate;
        }

        @Override
        public Boolean visitEmptyCriterion(EmptyCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitAndCriterion(AbstractMultiCriterion criterion) {
            return criterion.getCriterions().stream().anyMatch(c -> c.accept(this)) || predicate.test(criterion);
        }

        @Override
        public Boolean visitOrCriterion(AbstractMultiCriterion criterion) {
            return criterion.getCriterions().stream().anyMatch(c -> c.accept(this)) || predicate.test(criterion);
        }

        @Override
        public Boolean visitNotCriterion(NotCriterion criterion) {
            return criterion.getCriterion().accept(this) || predicate.test(criterion);
        }

        @Override
        public Boolean visitStringMatchCriterion(StringMatchCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitIntMatchCriterion(IntMatchCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitLongMatchCriterion(LongMatchCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitDateMatchCriterion(DateMatchCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public <U extends Comparable<? super U>> Boolean visitRangeCriterion(RangeCriterion<U> criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitDateRangeCriterion(DateRangeCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitBooleanMatchCriterion(BooleanMatchCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitPolygonCriterion(PolygonCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitBoundaryBoxCriterion(BoundaryBoxCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitCircleCriterion(CircleCriterion criterion) {
            return predicate.test(criterion);
        }

        @Override
        public Boolean visitFieldExistsCriterion(FieldExistsCriterion criterion) {
            return predicate.test(criterion);
        }
    }

    /**
     * ICriterionVisitor permitting to find a certain type of ICriterion from a ICriterion tree (a CircleCriterion for
     * example)
     * @param <T> type of Icriterion to find
     */
    private static class FinderCriterionVisitor<T extends ICriterion> implements ICriterionVisitor<T> {

        private Class<T> clazz;

        public FinderCriterionVisitor(Class<T> clazz) {
            this.clazz = clazz;
        }

        @Override
        public T visitEmptyCriterion(EmptyCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        private T visitMultiCriterion(AbstractMultiCriterion criterion) {
            if (this.clazz.isInstance(criterion)) {
                return (T) criterion;
            }
            for (ICriterion c : criterion.getCriterions()) {
                if (c.accept(this) != null) {
                    return (T) c;
                }
            }
            return null;
        }

        @Override
        public T visitAndCriterion(AbstractMultiCriterion criterion) {
            return visitMultiCriterion(criterion);
        }

        @Override
        public T visitOrCriterion(AbstractMultiCriterion criterion) {
            return visitMultiCriterion(criterion);
        }

        @Override
        public T visitNotCriterion(NotCriterion criterion) {
            if (this.clazz.isInstance(criterion)) {
                return (T) criterion;
            }
            return criterion.getCriterion().accept(this);
        }

        @Override
        public T visitStringMatchCriterion(StringMatchCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitStringMatchAnyCriterion(StringMatchAnyCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitIntMatchCriterion(IntMatchCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitLongMatchCriterion(LongMatchCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitDateMatchCriterion(DateMatchCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public <U extends Comparable<? super U>> T visitRangeCriterion(RangeCriterion<U> criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitDateRangeCriterion(DateRangeCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitBooleanMatchCriterion(BooleanMatchCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitPolygonCriterion(PolygonCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitBoundaryBoxCriterion(BoundaryBoxCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitCircleCriterion(CircleCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }

        @Override
        public T visitFieldExistsCriterion(FieldExistsCriterion criterion) {
            return (this.clazz.isInstance(criterion)) ? (T) criterion : null;
        }
    }

    /**
     * ICriterionVisitor permitting to know if geometry is nearest from distance to a point
     */
    private static class NearestFromDistanceGeometryVisitor implements IGeometryVisitor<Boolean> {

        private Crs crs;

        private double[] point;

        private double distance;

        public NearestFromDistanceGeometryVisitor(double[] point, double distance, Crs crs) {
            this.crs = crs;
            this.point = point;
            this.distance = distance;
        }

        @Override
        public Boolean visitGeometryCollection(GeometryCollection geometry) {
            return null;
        }

        @Override
        public Boolean visitLineString(LineString geometry) {
            return null;
        }

        @Override
        public Boolean visitMultiLineString(MultiLineString geometry) {
            return null;
        }

        @Override
        public Boolean visitMultiPoint(MultiPoint geometry) {
            return null;
        }

        @Override
        public Boolean visitMultiPolygon(MultiPolygon geometry) {
            return null;
        }

        @Override
        public Boolean visitPoint(Point geometry) {
            return (GeoHelper.getDistance(point[0], point[1], geometry.getCoordinates().getLongitude(),
                                          geometry.getCoordinates().getLatitude(), crs) < distance);
        }

        @Override
        public Boolean visitPolygon(Polygon geometry) {
            System.err.printf("Distance from point (%f, %f): %f\n", point[0], point[1], distance);
            ArrayList<Position> positions = geometry.getCoordinates().getExteriorRing();
            Position lastPosition = null;
            for (Position position : positions) {
                if (lastPosition != null) {
                    System.err.println("--");
                    boolean found = createIntermediatePositions(lastPosition, position).stream().peek(p -> System.err
                            .printf("point (%f, %f): %f\n", p.getLongitude(), p.getLatitude(),
                                    GeoHelper.getDistance(point[0], point[1], p.getLongitude(), p.getLatitude(), crs)))
                            .anyMatch(p -> GeoHelper
                                    .getDistance(point[0], point[1], p.getLongitude(), p.getLatitude(), crs)
                                    < distance);
                    if (found) {
                        return found;
                    }
                }
                lastPosition = position;
            }
            return false;
            //            return geometry.getCoordinates().getExteriorRing().stream().anyMatch(
            //                    p -> GeoHelper.getDistance(point[0], point[1], p.getLongitude(), p.getLatitude(), crs) < distance);
        }

        @Override
        public Boolean visitUnlocated(Unlocated geometry) {
            return null;
        }
    }

    /**
     * GeometryVisitor permitting to compute distance between a geometry and a point
     */
    private static class DistanceToPointGeometryVisitor implements IGeometryVisitor<Double> {

        private Crs crs;

        private double[] point;

        public DistanceToPointGeometryVisitor(double[] point, Crs crs) {
            this.crs = crs;
            this.point = point;
        }

        @Override
        public Double visitGeometryCollection(GeometryCollection geometry) {
            return null;
        }

        @Override
        public Double visitLineString(LineString geometry) {
            return null;
        }

        @Override
        public Double visitMultiLineString(MultiLineString geometry) {
            return null;
        }

        @Override
        public Double visitMultiPoint(MultiPoint geometry) {
            return null;
        }

        @Override
        public Double visitMultiPolygon(MultiPolygon geometry) {
            return null;
        }

        @Override
        public Double visitPoint(Point geometry) {
            return GeoHelper.getDistance(point[0], point[1], geometry.getCoordinates().getLongitude(),
                                         geometry.getCoordinates().getLatitude(), crs);
        }

        @Override
        public Double visitPolygon(Polygon geometry) {
            ArrayList<Position> positions = geometry.getCoordinates().getExteriorRing();
            Position lastPosition = null;
            double distance = Double.POSITIVE_INFINITY;
            for (Position position : positions) {
                if (lastPosition != null) {
                    System.err.println("--");
                     distance = FastMath.min(distance, createIntermediatePositions(lastPosition, position).stream().peek(p -> System.err
                            .printf("point (%f, %f): %f\n", p.getLongitude(), p.getLatitude(),
                                    GeoHelper.getDistance(point[0], point[1], p.getLongitude(), p.getLatitude(), crs)))
                            .mapToDouble(p -> GeoHelper.getDistance(point[0], point[1], p.getLongitude(), p.getLatitude(), crs)).min().getAsDouble());
                }
                lastPosition = position;
            }
            return distance;
            //            return geometry.getCoordinates().getExteriorRing().stream().anyMatch(
            //                    p -> GeoHelper.getDistance(point[0], point[1], p.getLongitude(), p.getLatitude(), crs) < distance);

        }

        @Override
        public Double visitUnlocated(Unlocated geometry) {
            return null;
        }
    }
}
