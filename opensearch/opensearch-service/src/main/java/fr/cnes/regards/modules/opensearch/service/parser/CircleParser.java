/*
 * LICENSE_PLACEHOLDER
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
