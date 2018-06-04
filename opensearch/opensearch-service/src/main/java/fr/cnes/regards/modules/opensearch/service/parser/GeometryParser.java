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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.util.MultiValueMap;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.converter.PolygonToArray;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * This {@link IParser} implementation only handles the "lat"/"lon"/"r" part of the OpenSearch request and returns an
 * {@link ICriterion} describing a Geometry intersection.<br>
 * @author Xavier-Alexandre Brochard
 */
public class GeometryParser implements IParser {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(GeometryParser.class);

    @Override
    public ICriterion parse(MultiValueMap<String, String> parameters) throws OpenSearchParseException {

        String geoParam = parameters.getFirst("g");

        // Check required query parameter
        if (geoParam == null) {
            return null;
        }

        try {
            WKTReader wkt = new WKTReader();
            Geometry geometry = wkt.read(geoParam);

            if ("Polygon".equals(geometry.getGeometryType())) {
                Polygon polygon = (Polygon) geometry;
                Converter<Polygon, Double[][][]> converter = new PolygonToArray();
                Double[][][] coordinates = converter.convert(polygon);
                return ICriterion.intersectsPolygon(coordinates);
            } else {
                // Only Polygons are handled for now
                throw new OpenSearchParseException("The passed WKT string does not reference");
            }
        } catch (ParseException e) {
            LOGGER.error("Geometry parsing error", e);
            throw new OpenSearchParseException(e.getMessage(), e);
        }
    }

}
