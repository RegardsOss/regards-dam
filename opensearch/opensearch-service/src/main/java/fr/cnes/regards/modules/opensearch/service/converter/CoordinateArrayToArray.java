/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.converter;

import java.util.stream.Stream;

import org.springframework.core.convert.converter.Converter;

import com.vividsolutions.jts.geom.Coordinate;

/**
 *
 * @author Xavier-Alexandre Brochard
 */
public class CoordinateArrayToArray implements Converter<Coordinate[], Double[][]> {

    private static final CoordinateToArray COORDINATE_TO_ARRAY = new CoordinateToArray();

    /* (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public Double[][] convert(Coordinate[] pCoordinates) {
        return Stream.of(pCoordinates).map(COORDINATE_TO_ARRAY::convert).toArray(Double[][]::new);
    }

}
