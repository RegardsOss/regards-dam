/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
    public Double[][][] convert(Polygon polygon) {
        Double[][][] result = new Double[polygon.getNumInteriorRing() + 1][][];

        // Add the exterior ring
        Coordinate[] exteriorRingCoordinates = polygon.getExteriorRing().getCoordinates();
        Double[][] exteriorRingCoordinatesAsArray = COORDINATE_ARRAY_TO_ARRAY.convert(exteriorRingCoordinates);
        result[0] = exteriorRingCoordinatesAsArray;

        // Add all interior rings
        for (int i = 0; i < (polygon.getNumInteriorRing()); i++) {
            Coordinate[] coordinates = polygon.getInteriorRingN(i).getCoordinates();
            Double[][] asArray = COORDINATE_ARRAY_TO_ARRAY.convert(coordinates);
            result[i + 1] = asArray;
        }

        return result;
    }

}
