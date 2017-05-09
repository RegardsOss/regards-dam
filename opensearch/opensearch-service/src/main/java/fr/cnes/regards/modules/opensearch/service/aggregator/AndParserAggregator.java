/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.aggregator;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.cnes.regards.modules.indexer.domain.criterion.AndCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.parser.IParser;

/**
 * Generates a {@link IParser} which returns an {@link AndCriterion} above all generated {@link ICriterion}s.
 * @author Xavier-Alexandre Brochard
 */
public class AndParserAggregator implements IParserAggregator {

    /**
     * Logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(AndParserAggregator.class);

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.opensearch.service.IParserAggregator#aggregate(fr.cnes.regards.modules.opensearch.service.IParser)
     */
    @Override
    public IParser aggregate(IParser... pParsers) {
        return pParameters -> {
            List<ICriterion> criteria = new ArrayList<>();
            for (IParser parser : pParsers) {
                try {
                    criteria.add(parser.parse(pParameters));
                } catch (IllegalArgumentException e) {
                    LOGGER.debug("Tried to aggregate parser " + parser + " but it cannot parse given parameters "
                            + pParameters + ", failing silently", e);
                }
            }
            return ICriterion.and(criteria.toArray(new ICriterion[criteria.size()]));
        };
    }

}
