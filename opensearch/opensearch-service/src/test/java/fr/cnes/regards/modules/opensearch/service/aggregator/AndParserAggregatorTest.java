/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.aggregator;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.modules.indexer.domain.criterion.AndCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.CircleCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;
import fr.cnes.regards.modules.opensearch.service.parser.CircleParser;
import fr.cnes.regards.modules.opensearch.service.parser.GeometryParser;
import fr.cnes.regards.modules.opensearch.service.parser.IParser;

/**
 * Unit test for {@link AndParserAggregator}
 * @author Xavier-Alexandre Brochard
 */
public class AndParserAggregatorTest {

    /**
     * Class under test
     */
    private final AndParserAggregator AGGREGATOR = new AndParserAggregator();

    /**
     * Check that the aggregator builds a parser returning null if it has no parsers to aggregate or only parsers returning null
     * @throws OpenSearchParseException
     */
    @Test
    public final void testAggregate_shouldReturnParserReturningNull() throws OpenSearchParseException {
        IParser parser = AGGREGATOR.aggregate();
        Assert.assertNull(parser.parse("q=toto:salut"));
    }

    /**
     * Check that the aggregator builds a parser returning a {@link AndCriterion} around sub-criterions
     * @throws OpenSearchParseException
     */
    @Test
    public final void testAggregate_shouldReturnParserReturningAndCriterion() throws OpenSearchParseException {
        IParser geometryParser = new GeometryParser();
        IParser circleParser = new CircleParser();
        IParser parser = AGGREGATOR.aggregate(circleParser, geometryParser);

        ICriterion criterion = parser.parse("lat=154&lon=748&r=124");
        Assert.assertNotNull(criterion);
        Assert.assertTrue(criterion instanceof AndCriterion);

        AndCriterion crit = (AndCriterion) criterion;
        Assert.assertTrue(crit.getCriterions().get(0) instanceof CircleCriterion);
    }

}
