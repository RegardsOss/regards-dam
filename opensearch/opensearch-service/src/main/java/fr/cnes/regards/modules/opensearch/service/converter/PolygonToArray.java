/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.converter;

import org.springframework.core.convert.converter.Converter;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

/**
 *
 * @author Xavier-Alexandre Brochard
 */
public class PolygonToArray implements Converter<Polygon, Double[][][]> {

    private static final CoordinateArrayToArray COORDINATE_ARRAY_TO_ARRAY = new CoordinateArrayToArray();

    /* (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public Double[][][] convert(Polygon pPolygon) {
        Double[][][] result = new Double[pPolygon.getNumInteriorRing() + 1][][];

        // Add the exterior ring
        Coordinate[] exteriorRingCoordinates = pPolygon.getExteriorRing().getCoordinates();
        Double[][] exteriorRingCoordinatesAsArray = COORDINATE_ARRAY_TO_ARRAY.convert(exteriorRingCoordinates);
        result[0] = exteriorRingCoordinatesAsArray;

        // Add all interior rings
        for (int i = 0; i < (pPolygon.getNumInteriorRing()); i++) {
            Coordinate[] coordinates = pPolygon.getInteriorRingN(i).getCoordinates();
            Double[][] asArray = COORDINATE_ARRAY_TO_ARRAY.convert(coordinates);
            result[i + 1] = asArray;
        }

        return result;
    }

}
