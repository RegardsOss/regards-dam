package fr.cnes.regards.modules.entities.domain.geometry;

/**
 * Geometry object.
 * Json adapter is GeometryAdapter
 *
 * @param <T> Depends on the Geometry type: for a Point T is Double, for a LineString T is Double[], ...
 */
public abstract class Geometry<T> {

    private GeometryType type;

    private Crs crs;

    private T[] coordinates;

    protected Geometry(GeometryType pType) {
        this.type = pType;
    }

    protected Geometry(GeometryType pType, T[] coord) {
        this(pType);
        this.coordinates = coord;
    }

    public GeometryType getType() {
        return type;
    }

    public void setType(GeometryType pType) {
        type = pType;
    }

    public Crs getCrs() {
        return crs;
    }

    public void setCrs(Crs pCrs) {
        crs = pCrs;
    }

    public T[] getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(T[] pCoordinates) {
        coordinates = pCoordinates;
    }

    /**
     * Point Geometry
     */
    public static class Point extends Geometry<Double> {

        public Point() {
            super(GeometryType.POINT);
        }

        public Point(Double[] pCoord) {
            super(GeometryType.POINT, pCoord);
        }
    }

    /**
     * LineString Geometry
     */
    public static class LineString extends Geometry<Double[]> {

        public LineString() {
            super(GeometryType.LINE_STRING);
        }

        public LineString(Double[][] pCoord) {
            super(GeometryType.LINE_STRING, pCoord);
        }
    }

    /**
     * MultiPoint Geometry
     */
    public static class MultiPoint extends Geometry<Double[]> {

        public MultiPoint() {
            super(GeometryType.MULTI_POINT);
        }

        public MultiPoint(Double[][] pCoord) {
            super(GeometryType.MULTI_POINT, pCoord);
        }
    }

    /**
     * MultiLineString Geometry
     */
    public static class MultiLineString extends Geometry<Double[][]> {

        public MultiLineString() {
            super(GeometryType.MULTI_LINE_STRING);
        }

        public MultiLineString(Double[][][] pCoord) {
            super(GeometryType.MULTI_LINE_STRING, pCoord);
        }
    }

    /**
     * Polygon Geometry
     */
    public static class Polygon extends Geometry<Double[][]> {

        public Polygon() {
            super(GeometryType.POLYGON);
        }

        public Polygon(Double[][][] pCoord) {
            super(GeometryType.POLYGON, pCoord);
        }
    }

    /**
     * MultiPolygon Geometry
     */
    public static class MultiPolygon extends Geometry<Double[][][]> {

        public MultiPolygon() {
            super(GeometryType.MULTI_POLYGON);
        }

        public MultiPolygon(Double[][][][] pCoord) {
            super(GeometryType.MULTI_POLYGON, pCoord);
        }

    }
}
