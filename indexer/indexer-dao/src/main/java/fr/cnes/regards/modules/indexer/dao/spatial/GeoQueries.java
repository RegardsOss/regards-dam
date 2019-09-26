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
package fr.cnes.regards.modules.indexer.dao.spatial;

import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.locationtech.jts.geom.Coordinate;

import fr.cnes.regards.modules.indexer.domain.criterion.PolygonCriterion;

/**
 * Geo queries utility class
 * @author oroussel
 */
public final class GeoQueries {

    /**
     * ComputeShapeBuilder from polygon criterion depending on polygon nature
     */
    public static ShapeBuilder<?, ?> computeShapeBuilder(PolygonCriterion criterion) {
        // Only shell can be taken into account (external emprise)
        double[][] shell = GeoHelper.normalizePolygonAsArray(criterion.getCoordinates())[0];

        CoordinatesBuilder coordBuilder = new CoordinatesBuilder();
        for (double[] point : shell) {
            coordBuilder.coordinate(new Coordinate(point[0], point[1]));
        }

        return new PolygonBuilder(coordBuilder);
    }

}
