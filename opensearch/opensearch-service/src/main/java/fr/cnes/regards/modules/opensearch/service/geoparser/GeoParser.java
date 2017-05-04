/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.geoparser;

import java.util.Map;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.IParser;

/**
 * Parses the "lat"/"lon"/"r"/"g" part of an OpenSearch request.
 * @author Xavier-Alexandre Brochard
 */
public class GeoParser implements IParser {

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.opensearch.service.IParser#parse(java.util.Map)
     */
    @Override
    public ICriterion parse(Map<String, String> pParameters) {
        String g = pParameters.get(GeoParameters.G.getName());
        String lat = pParameters.get(GeoParameters.LAT.getName());
        String lon = pParameters.get(GeoParameters.LON.getName());
        String r = pParameters.get(GeoParameters.R.getName());
        return null;
    }

}
