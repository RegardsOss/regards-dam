package fr.cnes.regards.modules.entities.domain.converter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import fr.cnes.regards.framework.gson.annotation.GsonTypeAdapter;
import fr.cnes.regards.modules.entities.domain.geometry.Crs;
import fr.cnes.regards.modules.entities.domain.geometry.Geometry;
import fr.cnes.regards.modules.entities.domain.geometry.GeometryType;

/**
 * Geometry Json adapter.
 * Geometry is an abstract type providing several concrete sub-classes so its TypeAdapter is a hair hairy
 * @author oroussel
 */
@GsonTypeAdapter(adapted = Geometry.class) // For reading...
public class GeometryAdapter<T extends Geometry<?>> extends TypeAdapter<T> {

    @SuppressWarnings("unchecked")
    @Override
    public void write(JsonWriter out, T geom) throws IOException {
        out.beginObject();
        out.name("type").value(geom.getType().toString());
        if (geom.getCrs() != null) {
            out.name("crs").value(geom.getCrs().toString());
        }
        out.name("coordinates").beginArray();
        switch (geom.getType()) {
            case POINT:
                Geometry<Double> point = (Geometry<Double>) geom;
                for (double val : point.getCoordinates()) {
                    out.value(val);
                }
                break;
            case MULTI_POINT:
            case LINE_STRING:
                Geometry<Double[]> multi = (Geometry<Double[]>) geom;
                for (Double[] line : multi.getCoordinates()) {
                    writeArray(out, line);
                }
                break;
            case MULTI_LINE_STRING:
            case POLYGON:
                Geometry<Double[][]> poly = (Geometry<Double[][]>) geom;
                for (Double[][] mat : poly.getCoordinates()) {
                    writeMatrix(out, mat);
                }
                break;
            case MULTI_POLYGON:
                Geometry<Double[][][]> multiPoly = (Geometry<Double[][][]>) geom;
                for (Double[][][] poly2 : multiPoly.getCoordinates()) {
                    out.beginArray();
                    for (Double[][] mat : poly2) {
                        writeMatrix(out, mat);
                    }
                    out.endArray();
                }
                break;
            default:

        }
        out.endArray();
        out.endObject();
    }

    private void writeMatrix(JsonWriter out, Double[][] mat) throws IOException {
        out.beginArray();
        for (Double[] array : mat) {
            writeArray(out, array);
        }
        out.endArray();
    }

    private void writeArray(JsonWriter out, Double[] array) throws IOException {
        out.beginArray();
        for (double val : array) {
            out.value(val);
        }
        out.endArray();
    }

    private Double[] readArray(JsonReader in) throws IOException {
        in.beginArray();
        List<Double> list = new ArrayList<>();
        while (in.hasNext()) {
            list.add(in.nextDouble());
        }
        in.endArray();
        return list.toArray(new Double[list.size()]);
    }

    private Double[][] readMatrix(JsonReader in) throws IOException {
        in.beginArray();
        List<Double[]> list = new ArrayList<>();
        while (in.hasNext()) {
            list.add(readArray(in));
        }
        in.endArray();
        return list.toArray(new Double[list.size()][]);
    }

    private Double[][][] readCubicMatrix(JsonReader in) throws IOException {
        in.beginArray();
        List<Double[][]> list = new ArrayList<>();
        while (in.hasNext()) {
            list.add(readMatrix(in));
        }
        in.endArray();
        return list.toArray(new Double[list.size()][][]);
    }

    private Double[][][][] readMatrix4d(JsonReader in) throws IOException {
        in.beginArray();
        List<Double[][][]> list = new ArrayList<>();
        while (in.hasNext()) {
            list.add(readCubicMatrix(in));
        }
        in.endArray();
        return list.toArray(new Double[list.size()][][][]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public T read(JsonReader in) throws IOException {
        in.beginObject();
        GeometryType type = null;
        Crs crs = null;
        Geometry<?> geometry = null;
        while (in.hasNext()) {
            switch (in.nextName()) {
                case "type":
                    type = GeometryType.fromString(in.nextString());
                    break;
                case "crs":
                    crs = Crs.valueOf(in.nextString());
                    break;
                case "coordinates":
                    if (type == null) {
                        throw new IOException("JSon malformed : type must be defined before coordinates.");
                    }
                    switch (type) {
                        case POINT:
                            geometry = new Geometry.Point(readArray(in));
                            break;
                        case MULTI_POINT:
                            geometry = new Geometry.MultiPoint(readMatrix(in));
                            break;
                        case LINE_STRING:
                            geometry = new Geometry.LineString(readMatrix(in));
                            break;
                        case MULTI_LINE_STRING:
                            geometry = new Geometry.MultiLineString(readCubicMatrix(in));
                            break;
                        case POLYGON:
                            geometry = new Geometry.Polygon(readCubicMatrix(in));
                            break;
                        case MULTI_POLYGON:
                            geometry = new Geometry.MultiPolygon(readMatrix4d(in));
                            break;
                        default:
                            break;
                    }
                default:
            }
        }
        if (geometry == null) {
            throw new IOException("JSon malformed : missing coordinates field.");
        }
        in.endObject();
        if (crs != null) {
            geometry.setCrs(crs);
        }
        return (T) geometry;
    }

    @GsonTypeAdapter(adapted = Geometry.Point.class) // For writing
    public static class PointAdapter extends GeometryAdapter<Geometry.Point> {

    }

    @GsonTypeAdapter(adapted = Geometry.MultiPoint.class) // For writing
    public static class MultiPointAdapter extends GeometryAdapter<Geometry.MultiPoint> {

    }

    @GsonTypeAdapter(adapted = Geometry.LineString.class) // For writing
    public static class LineStringAdapter extends GeometryAdapter<Geometry.LineString> {

    }

    @GsonTypeAdapter(adapted = Geometry.MultiLineString.class) // For writing
    public static class MultiLineStringAdapter extends GeometryAdapter<Geometry.MultiLineString> {

    }

    @GsonTypeAdapter(adapted = Geometry.Polygon.class) // For writing
    public static class PolygonAdapter extends GeometryAdapter<Geometry.Polygon> {

    }

    @GsonTypeAdapter(adapted = Geometry.MultiPolygon.class) // For writing
    public static class MultiPolygonAdapter extends GeometryAdapter<Geometry.MultiPolygon> {

    }

}
