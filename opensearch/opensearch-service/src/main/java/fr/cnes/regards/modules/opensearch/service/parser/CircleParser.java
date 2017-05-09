/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.parser;

import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * This {@link IParser} implementation only handles the the "lat"/"lon"/"r" part of the OpenSearch request, and returns an {@link ICriterion} describing a circle intersection.<br>
 * @author Xavier-Alexandre Brochard
 */
@Component
public class CircleParser implements IParser {

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.opensearch.service.parser.IParser#parse(java.util.Map)
     */
    @Override
    public ICriterion parse(Map<String, String> pParameters) throws OpenSearchParseException {
        Assert.notNull(pParameters.get("lat"));
        Assert.notNull(pParameters.get("lon"));
        Assert.notNull(pParameters.get("r"));

        Double[] center = { Double.parseDouble(pParameters.get("lon")), Double.parseDouble(pParameters.get("lat")) };
        return ICriterion.intersectsCircle(center, pParameters.get("r"));
    }

}
