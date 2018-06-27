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

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.stream.Stream;

import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilders;
import org.hipparchus.geometry.euclidean.threed.Vector3D;
import org.hipparchus.geometry.partitioning.RegionFactory;
import org.hipparchus.geometry.partitioning.SubHyperplane;
import org.hipparchus.geometry.spherical.oned.ArcsSet;
import org.hipparchus.geometry.spherical.oned.S1Point;
import org.hipparchus.geometry.spherical.twod.Circle;
import org.hipparchus.geometry.spherical.twod.Edge;
import org.hipparchus.geometry.spherical.twod.S2Point;
import org.hipparchus.geometry.spherical.twod.Sphere2D;
import org.hipparchus.geometry.spherical.twod.SphericalPolygonsSet;
import org.hipparchus.geometry.spherical.twod.SubCircle;
import org.hipparchus.geometry.spherical.twod.Vertex;
import org.hipparchus.util.FastMath;
import org.hipparchus.util.MathUtils;

import com.vividsolutions.jts.geom.Coordinate;
import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;

/**
 * Geo queries utility class
 * @author oroussel
 */
public final class GeoQueries {
    /**
     * Tolerance (also used as hyperplace thickness)
     * @see SphericalPolygonsSet
     */
    private static final double TOLERANCE = 1.0e-7;

    /**
     * Decimal number of digits
     */
    private static final int DECIMAL_PRECISION = 6;

    /**
     * Numerical operations rule settings
     */
    private static final MathContext mathContext = new MathContext(DECIMAL_PRECISION);

    /**
     * NORTH POLE is at latitude 90° (default longitude to 0°)
     */
    private static final S2Point NORTH_POLE = new S2Point(toTheta(0.0), toPhi(90.0));

    /**
     * SOUTH POLE is at latitude -90° (default longitude to 0°)
     */
    private static final S2Point SOUTH_POLE = new S2Point(toTheta(0.0), toPhi(-90.0));

    /**
     * The four sphere quarters reaching south and north poles
     */
    private static final SphericalPolygonsSet FIRST_QUARTER = new SphericalPolygonsSet(TOLERANCE, SOUTH_POLE,
                                                                                       toPoint(90, 0), NORTH_POLE,
                                                                                       toPoint(0, 0));

    private static final SphericalPolygonsSet SECOND_QUARTER = new SphericalPolygonsSet(TOLERANCE, SOUTH_POLE,
                                                                                        toPoint(180, 0), NORTH_POLE,
                                                                                        toPoint(90, 0));

    private static final SphericalPolygonsSet THIRD_QUARTER = new SphericalPolygonsSet(TOLERANCE, SOUTH_POLE,
                                                                                       toPoint(-90, 0), NORTH_POLE,
                                                                                       toPoint(-180, 0));

    private static final SphericalPolygonsSet FOURTH_QUARTER = new SphericalPolygonsSet(TOLERANCE, SOUTH_POLE,
                                                                                        toPoint(0, 0), NORTH_POLE,
                                                                                        toPoint(-90, 0));

    private static final SphericalPolygonsSet NORTH_HEMISPHERE = new SphericalPolygonsSet(NORTH_POLE.getVector(),
                                                                                          TOLERANCE);

    private static final SphericalPolygonsSet SOUTH_HEMISPHERE = new SphericalPolygonsSet(SOUTH_POLE.getVector(),
                                                                                          TOLERANCE);

    /**
     * All 4 quarters
     */
    private static final SphericalPolygonsSet[] QUARTERS = { FIRST_QUARTER, SECOND_QUARTER, THIRD_QUARTER,
            FOURTH_QUARTER };

    /**
     * 2D region factory permitting geometries on spherical polygons sets
     */
    private static final RegionFactory<Sphere2D> REGION_FACTORY = new RegionFactory<>();

    /**
     * Transform longitude into spherical theta angle
     * See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical coordinates</a>
     * @param lon longitude in degrees
     * @return theta in radians (0 = greenwich meridian)
     */
    public static double toTheta(double lon) {
        if (lon > 0) {
            return FastMath.toRadians(lon);
        }
        return FastMath.toRadians(lon) + MathUtils.TWO_PI;
    }

    /**
     * Transform latitude into spherical phi angle
     * See <a href="http://mathworld.wolfram.com/SphericalCoordinates.html">Spherical coordinates</a>
     * @param lat latitude in degrees
     * @return phi in radians (0 = north pole, 180 = south pole)
     */
    public static double toPhi(double lat) {
        return FastMath.toRadians(90.0 - lat);
    }

    /**
     * Transform longitude/latitude to S2Point
     * @param lon longitude in degrees
     * @param lat latitude in degrees
     * @return Spherical radians Point representation
     */
    public static S2Point toPoint(double lon, double lat) {
        return new S2Point(toTheta(lon), toPhi(lat));
    }

    private static Coordinate toCoordinate(S2Point point) {
        double theta = point.getTheta();
        double phi = point.getPhi();
        if (FastMath.abs(phi) < TOLERANCE) {
            phi = 0.0;
            theta = 0.0;
        } else if ((FastMath.abs(Math.PI - phi) < TOLERANCE)) {
            phi = FastMath.PI;
            theta = 0.0;
        }
        if (theta > FastMath.PI) {
            theta -= MathUtils.TWO_PI;
        }
        if (FastMath.abs(theta) < TOLERANCE) {
            theta = 0.0;
        }
        double phiPrime = Math.PI / 2 - phi;
        if (FastMath.abs(phiPrime) < TOLERANCE) {
            phiPrime = 0.0;
        }
        return new Coordinate(BigDecimal.valueOf(FastMath.toDegrees(theta)).round(mathContext).floatValue(),
                              BigDecimal.valueOf(FastMath.toDegrees(phiPrime)).round(mathContext).floatValue());

    }

    //    private static PolygonBuilder simplify()

    /**
     * Intersect both given spherical polygons
     * @return a PolygonBuilder or null if none intersection
     */
    private static PolygonBuilder intersect(SphericalPolygonsSet poly1, SphericalPolygonsSet poly2) {
        CoordinatesBuilder builder = new CoordinatesBuilder();
        SphericalPolygonsSet intersection = (SphericalPolygonsSet) REGION_FACTORY.intersection(poly1, poly2);
        // Only external boundary is taken into account
        if (intersection.getBoundaryLoops().isEmpty()) {
            return null;
        }
        int vertexCount = 0;
        Vertex firstVertex = intersection.getBoundaryLoops().get(0);
        Vertex vertex = firstVertex;
        while (vertex != null) {
            S2Point point = vertex.getLocation();
            vertexCount++;
            builder.coordinate(toCoordinate(point));
            vertex = vertex.getOutgoing().getEnd();
            if (intersectionWithAntiMeridian(vertex.getOutgoing()).count() > 0) {
                System.out.println(toCoordinate(vertex.getOutgoing().getStart().getLocation()) + " - " + toCoordinate(
                        vertex.getOutgoing().getEnd().getLocation()));
            }

            if (vertex.equals(firstVertex)) {
                vertex = null;
            }
        }
        if (vertexCount < 3) {
            return null;
        }
        // Close polygon
        builder.close();
        return ShapeBuilders.newPolygon(builder);
    }

    /**
     * Transform edge into sub-circle
     */
    private static SubCircle toSubCircle(Edge edge) {
        // Retrieve circle of which edge belongs to
        Circle c = edge.getCircle();
        // Retrieve start and end angles
        double start = c.getPhase(edge.getStart().getLocation().getVector());
        double end = start + edge.getLength();
        // Build sub-circle
        return new SubCircle(c, new ArcsSet(start, end, TOLERANCE));
    }

    private static Stream<S2Point> getPoints(SubCircle sc) {
        if (sc == null) {
            return Stream.empty();
        }
        Circle c = (Circle) sc.getHyperplane();
        Stream.Builder<S2Point> builder = Stream.builder();
        for (final double[] a : (ArcsSet) sc.getRemainingRegion()) {
            builder.accept(c.toSpace(new S1Point(a[0])));
            builder.accept(c.toSpace(new S1Point(a[1])));
        }
        return builder.build();
    }

    private static Stream<S2Point> intersectionWithAntiMeridian(Edge edge) {
        Circle antimeridian = new Circle(Vector3D.PLUS_J, TOLERANCE);
        SubCircle sc = toSubCircle(edge);
        SubHyperplane.SplitSubHyperplane<Sphere2D> split = sc.split(antimeridian);
        return Stream.concat(getPoints((SubCircle) split.getPlus()), getPoints((SubCircle) split.getMinus())
                .filter(p -> p.getVector().getX() < 0.0 && FastMath.abs(p.getVector().getY()) <= TOLERANCE));
    }

    /**
     * ComputeShapeBuilder from polygon criterion depending on polygon nature
     */
    public static ShapeBuilder computeShapeBuilder(PolygonCriterion criterion) {
        Double[][][] coordinates = criterion.getCoordinates();
        // Only shell can be taken into account (external emprise)
        Double[][] shell = coordinates[0];
        // Use SphericalPolygonsSet to model polygon (first shell coordinates are last too)
        S2Point[] shellPoints = new S2Point[shell.length - 1];
        for (int i = 0; i < shellPoints.length; i++) {
            shellPoints[i] = toPoint(shell[i][0], shell[i][1]);
        }

//        MultiPolygonBuilder multiPolygonBuilder = ShapeBuilders.newMultiPolygon();
//        SphericalPolygonsSet sphericalPolygonsSet = new SphericalPolygonsSet(TOLERANCE, shellPoints);
//        multiPolygonBuilder.polygon(intersect(sphericalPolygonsSet, NORTH_HEMISPHERE));
//        multiPolygonBuilder.polygon(intersect(sphericalPolygonsSet, SOUTH_HEMISPHERE));
//        System.out.println(multiPolygonBuilder.toString());

        //        SphericalPolygonsSet sphericalPolygonsSet = new SphericalPolygonsSet(TOLERANCE, shellPoints);
        //        if ((sphericalPolygonsSet.checkPoint(NORTH_POLE) == Region.Location.INSIDE) && (
        //                sphericalPolygonsSet.checkPoint(SOUTH_POLE) == Region.Location.INSIDE)) {
        //            System.out.println("PBBBBBBBBBB !!!!!");
        //        } else if ((sphericalPolygonsSet.checkPoint(NORTH_POLE) == Region.Location.INSIDE) || (
        //                sphericalPolygonsSet.checkPoint(SOUTH_POLE) == Region.Location.INSIDE)) {
        //            // it's a cap : polygon is around a pole
        //            // Intersect polygon with the 4 quarters reaching both poles and use it as a multi-polygon shape builder
        //            List<PolygonBuilder> polygonBuilders = Arrays.stream(QUARTERS)
        //                    .map(quarter -> intersect(sphericalPolygonsSet, quarter)).filter(Objects::nonNull)
        //                    .collect(Collectors.toList());
        //            if (polygonBuilders.size() == 1) {
        //                return polygonBuilders.get(0);
        //            } else {
        //                MultiPolygonBuilder multiPolygonBuilder = ShapeBuilders.newMultiPolygon();
        //                polygonBuilders.forEach(multiPolygonBuilder::polygon);
        //                return multiPolygonBuilder;
        //            }
        //        }
        // Third: no particular case polygon
        CoordinatesBuilder coordBuilder = new CoordinatesBuilder();
        for (Double[] point : shell) {
            coordBuilder.coordinate(new Coordinate(point[0], point[1]));
        }

        return ShapeBuilders.newPolygon(coordBuilder);
    }


}
