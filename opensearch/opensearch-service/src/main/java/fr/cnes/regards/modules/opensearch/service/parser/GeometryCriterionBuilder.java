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
package fr.cnes.regards.modules.opensearch.service.parser;

import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;
import fr.cnes.regards.modules.opensearch.service.converter.PolygonToArray;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Creates {@link ICriterion} from multiple geometry types.
 * @author Sébastien Binda
 */
public class GeometryCriterionBuilder {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryCriterionBuilder.class);

    /**
     * Read WKT format in given String to creates a Geometry criterion.
     * Only Polygons are handled here.
     * @param wktGeometry
     * @return
     * @throws OpenSearchParseException
     */
    public static ICriterion build(String wktGeometry) throws InvalidGeometryException {
        try {
            WKTReader wkt = new WKTReader();
            Geometry geometry = wkt.read(wktGeometry);

            if (geometry instanceof Polygon) {
                Polygon polygon = (Polygon) geometry;
                Converter<Polygon, double[][][]> converter = new PolygonToArray();
                double[][][] coordinates = converter.convert(polygon);
                return ICriterion.intersectsPolygon(coordinates);
            } else {
                // Only Polygons are handled for now
                throw new InvalidGeometryException("The passed WKT string does not reference a polygon");
            }
        } catch (ParseException e) {
            LOGGER.error("Geometry parsing error", e);
            throw new InvalidGeometryException(e.getMessage(), e);
        }
    }

    /**
     * Creates a circle geometry criterion from longitute, latitude and radius.
     * @param lonParam
     * @param latParam
     * @param radiusParam
     * @return {@link ICriterion}
     * @throws OpenSearchParseException
     */
    public static ICriterion build(String lonParam, String latParam, String radiusParam)
            throws InvalidGeometryException {
        // Check required query parameter
        if ((latParam == null) && (lonParam == null) && (radiusParam == null)) {
            return null;
        }

        if (latParam == null) {
            String errorMessage = String.format("Missing center latitude parameter to create circle geometry");
            throw new InvalidGeometryException(errorMessage);
        }

        if (lonParam == null) {
            String errorMessage = String.format("Missing center longitude parameter to create circle geometry");
            throw new InvalidGeometryException(errorMessage);
        }

        if (radiusParam == null) {
            String errorMessage = String.format("Missing radius parameter to create circle geometry");
            throw new InvalidGeometryException(errorMessage);
        }

        double[] center = { Double.parseDouble(lonParam), Double.parseDouble(latParam) };
        return ICriterion.intersectsCircle(center, radiusParam);
    }

    public static ICriterion buildBbox(String bbox) throws InvalidGeometryException {
        return ICriterion.intersectsBbox(bbox);
    }

}
