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
package fr.cnes.regards.modules.opensearch.service.parser;

import org.springframework.util.MultiValueMap;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.exception.InvalidGeometryException;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * This {@link IParser} implementation only handles the "lat"/"lon"/"r" part of the OpenSearch request, and returns
 * an {@link ICriterion} describing a circle intersection.<br>
 * @author Xavier-Alexandre Brochard
 */
public class CircleParser implements IParser {

    private static final String CENTER_LAT = "lat";

    private static final String CENTER_LON = "lon";

    private static final String RADIUS = "r";

    @Override
    public ICriterion parse(MultiValueMap<String, String> parameters) throws OpenSearchParseException {
        String latParam = parameters.getFirst(CENTER_LAT);
        String lonParam = parameters.getFirst(CENTER_LON);
        String radiusParam = parameters.getFirst(RADIUS);

        // Check required query parameter
        if ((latParam == null) && (lonParam == null) && (radiusParam == null)) {
            return null;
        }

        if (latParam == null) {
            String errorMessage = String.format("Missing center latitude parameter : %s", CENTER_LAT);
            throw new OpenSearchParseException(errorMessage);
        }

        if (lonParam == null) {
            String errorMessage = String.format("Missing center longitude parameter :  : %s", CENTER_LON);
            throw new OpenSearchParseException(errorMessage);
        }

        if (radiusParam == null) {
            String errorMessage = String.format("Missing radius parameter :  : %s", RADIUS);
            throw new OpenSearchParseException(errorMessage);
        }

        try {
            return GeometryCriterionBuilder.build(lonParam, latParam, radiusParam);
        } catch (InvalidGeometryException e) {
            throw new OpenSearchParseException(e);
        }
    }
}
