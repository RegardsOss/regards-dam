/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service;

import java.util.ArrayList;
import java.util.List;

import fr.cnes.regards.modules.indexer.domain.criterion.AndCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * Generates a {@link IParser} which returns an {@link AndCriterion} above all generated {@link ICriterion}s.
 * @author Xavier-Alexandre Brochard
 */
public class AndParserAggregator implements IParserAggregator {

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.opensearch.service.IParserAggregator#aggregate(fr.cnes.regards.modules.opensearch.service.IParser)
     */
    @Override
    public IParser aggregate(IParser... pParsers) {
        return pParameters -> {
            List<ICriterion> criteria = new ArrayList<>();
            for (IParser parser : pParsers) {
                criteria.add(parser.parse(pParameters));
            }
            return ICriterion.and(criteria.toArray(new ICriterion[criteria.size()]));
        };
    }

}
