/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.util.stream.Stream;

import org.locationtech.jts.geom.Coordinate;
import org.springframework.core.convert.converter.Converter;

/**
 * @author Xavier-Alexandre Brochard
 */
public class CoordinateArrayToArray implements Converter<Coordinate[], double[][]> {

    private static final CoordinateToArray COORDINATE_TO_ARRAY = new CoordinateToArray();

    @Override
    public double[][] convert(Coordinate[] coordinates) {
        return Stream.of(coordinates).map(COORDINATE_TO_ARRAY::convert).toArray(double[][]::new);
    }

}
