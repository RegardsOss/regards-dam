/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.util.Map;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * This {@link IParser} implementation only handles the the "lat"/"lon"/"r" part of the OpenSearch request, and returns an {@link ICriterion} describing a circle intersection.<br>
 * @author Xavier-Alexandre Brochard
 */
public class CircleParser implements IParser {

    private static final String CENTER_LAT = "lat";

    private static final String CENTER_LON = "lon";

    private static final String RADIUS = "r";

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.opensearch.service.parser.IParser#parse(java.util.Map)
     */
    @Override
    public ICriterion parse(Map<String, String> parameters) throws OpenSearchParseException {

        String latParam = parameters.get(CENTER_LAT);
        String lonParam = parameters.get(CENTER_LON);
        String rParam = parameters.get(RADIUS);

        // Check required query parameter
        if ((latParam == null) && (lonParam == null) && (rParam == null)) {
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

        if (rParam == null) {
            String errorMessage = String.format("Missing radius parameter :  : %s", RADIUS);
            throw new OpenSearchParseException(errorMessage);
        }

        Double[] center = { Double.parseDouble(lonParam), Double.parseDouble(latParam) };
        return ICriterion.intersectsCircle(center, rParam);
    }
}
