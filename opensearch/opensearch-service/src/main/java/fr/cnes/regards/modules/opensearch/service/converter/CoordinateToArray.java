/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.converter;

import org.springframework.core.convert.converter.Converter;

import com.vividsolutions.jts.geom.Coordinate;

/**
 *
 * @author Xavier-Alexandre Brochard
 */
public class CoordinateToArray implements Converter<Coordinate, Double[]> {

    /* (non-Javadoc)
     * @see org.springframework.core.convert.converter.Converter#convert(java.lang.Object)
     */
    @Override
    public Double[] convert(Coordinate pSource) {
        return new Double[] { pSource.getOrdinate(0), pSource.getOrdinate(1) };
    }

}
