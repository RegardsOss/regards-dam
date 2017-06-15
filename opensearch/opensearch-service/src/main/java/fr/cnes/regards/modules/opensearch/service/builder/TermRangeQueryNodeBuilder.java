/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.builder;

import java.util.function.Function;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.Message;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableTable;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.models.domain.attributes.AttributeType;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;
import fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages;

/**
 * Builds a {@link RangeCriterion} from a {@link TermRangeQueryNode} object.
 *
 * @author Xavier-Alexandre Brochard
 */
public class TermRangeQueryNodeBuilder extends QueryTreeBuilder implements ICriterionQueryBuilder {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(TermRangeQueryNodeBuilder.class);

    /**
     * Service retrieving the up-to-date list of {@link AttributeModel}s. Autowired by Spring.
     */
    private final IAttributeFinder attributeFinder;

    // Define a static two-entries table storing the different criterion builders based on the type of attribute and the
    // type of comparison performed
    // @formatter:off
    private static final ImmutableTable<AttributeType, RangeComparison, Function<TermRangeQueryNodeFacade, ICriterion>> CRITERION_TABLE = new ImmutableTable.Builder<AttributeType, RangeComparison, Function<TermRangeQueryNodeFacade, ICriterion>>()
            .put(AttributeType.INTEGER, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
            .put(AttributeType.INTEGER, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
            .put(AttributeType.INTEGER, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
            .put(AttributeType.INTEGER, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
            .put(AttributeType.INTEGER, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDouble(), pFacade.getUpperBoundAsDouble()))

//            .put(AttributeType.INTEGER_ARRAY, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsInteger()))
//            .put(AttributeType.INTEGER_ARRAY, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsInteger()))
//            .put(AttributeType.INTEGER_ARRAY, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsInteger()))
//            .put(AttributeType.INTEGER_ARRAY, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsInteger()))
//            .put(AttributeType.INTEGER_ARRAY, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsInteger(), pFacade.getUpperBoundAsInteger()))
//
//            .put(AttributeType.INTEGER_INTERVAL, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsInteger()))
//            .put(AttributeType.INTEGER_INTERVAL, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsInteger()))
//            .put(AttributeType.INTEGER_INTERVAL, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsInteger()))
//            .put(AttributeType.INTEGER_INTERVAL, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsInteger()))
//            .put(AttributeType.INTEGER_INTERVAL, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsInteger(), pFacade.getUpperBoundAsInteger()))

            .put(AttributeType.DOUBLE, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
            .put(AttributeType.DOUBLE, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
            .put(AttributeType.DOUBLE, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
            .put(AttributeType.DOUBLE, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
            .put(AttributeType.DOUBLE, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDouble(), pFacade.getUpperBoundAsDouble()))

//            .put(AttributeType.DOUBLE_ARRAY, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
//            .put(AttributeType.DOUBLE_ARRAY, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
//            .put(AttributeType.DOUBLE_ARRAY, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
//            .put(AttributeType.DOUBLE_ARRAY, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
//            .put(AttributeType.DOUBLE_ARRAY, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDouble(), pFacade.getUpperBoundAsDouble()))
//
//            .put(AttributeType.DOUBLE_INTERVAL, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
//            .put(AttributeType.DOUBLE_INTERVAL, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
//            .put(AttributeType.DOUBLE_INTERVAL, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
//            .put(AttributeType.DOUBLE_INTERVAL, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
//            .put(AttributeType.DOUBLE_INTERVAL, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDouble(), pFacade.getUpperBoundAsDouble()))

            .put(AttributeType.LONG, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
            .put(AttributeType.LONG, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDouble()))
            .put(AttributeType.LONG, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
            .put(AttributeType.LONG, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDouble()))
            .put(AttributeType.LONG, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDouble(), pFacade.getUpperBoundAsDouble()))

//            .put(AttributeType.LONG_ARRAY, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsLong()))
//            .put(AttributeType.LONG_ARRAY, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsLong()))
//            .put(AttributeType.LONG_ARRAY, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsLong()))
//            .put(AttributeType.LONG_ARRAY, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsLong()))
//            .put(AttributeType.LONG_ARRAY, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsLong(), pFacade.getUpperBoundAsLong()))
//
//            .put(AttributeType.LONG_INTERVAL, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsLong()))
//            .put(AttributeType.LONG_INTERVAL, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsLong()))
//            .put(AttributeType.LONG_INTERVAL, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsLong()))
//            .put(AttributeType.LONG_INTERVAL, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsLong()))
//            .put(AttributeType.LONG_INTERVAL, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsLong(), pFacade.getUpperBoundAsLong()))

            .put(AttributeType.DATE_ISO8601, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDateTime()))
            .put(AttributeType.DATE_ISO8601, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDateTime()))
            .put(AttributeType.DATE_ISO8601, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDateTime()))
            .put(AttributeType.DATE_ISO8601, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDateTime()))
            .put(AttributeType.DATE_ISO8601, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDateTime(), pFacade.getUpperBoundAsDateTime()))

//            .put(AttributeType.DATE_ARRAY, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDateTime()))
//            .put(AttributeType.DATE_ARRAY, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDateTime()))
//            .put(AttributeType.DATE_ARRAY, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDateTime()))
//            .put(AttributeType.DATE_ARRAY, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDateTime()))
//            .put(AttributeType.DATE_ARRAY, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDateTime(), pFacade.getUpperBoundAsDateTime()))
//
//            .put(AttributeType.DATE_INTERVAL, RangeComparison.GE, pFacade -> ICriterion.ge(pFacade.getField(), pFacade.getLowerBoundAsDateTime()))
//            .put(AttributeType.DATE_INTERVAL, RangeComparison.GT, pFacade -> ICriterion.gt(pFacade.getField(), pFacade.getLowerBoundAsDateTime()))
//            .put(AttributeType.DATE_INTERVAL, RangeComparison.LE, pFacade -> ICriterion.le(pFacade.getField(), pFacade.getUpperBoundAsDateTime()))
//            .put(AttributeType.DATE_INTERVAL, RangeComparison.LT, pFacade -> ICriterion.lt(pFacade.getField(), pFacade.getUpperBoundAsDateTime()))
//            .put(AttributeType.DATE_INTERVAL, RangeComparison.BETWEEN, pFacade -> ICriterion.between(pFacade.getField(), pFacade.getLowerBoundAsDateTime(), pFacade.getUpperBoundAsDateTime()))

            .build();
    // @formatter:on

    /**
     * @param pAttributeModelCache
     *            Service retrieving the up-to-date list of {@link AttributeModel}s
     */
    public TermRangeQueryNodeBuilder(IAttributeFinder attributeFinder) {
        super();
        this.attributeFinder = attributeFinder;
    }

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        final TermRangeQueryNode rangeNode = (TermRangeQueryNode) pQueryNode;

        // Extract info for node
        TermRangeQueryNodeFacade wrapper = new TermRangeQueryNodeFacade(rangeNode);

        // Retrieve the corresponding model
        AttributeType attributeType;
        try {
            attributeType = attributeFinder.findByName(wrapper.getField()).getType();
        } catch (OpenSearchUnknownParameter e) {
            throw new QueryNodeException(
                    new MessageImpl(QueryParserMessages.FIELD_TYPE_UNDETERMINATED, e.getMessage(), e));
        }

        // Compute the type of range comparison: lower/greater than/equal or between
        RangeComparison rangeComparison = getRangeComparison(wrapper.getField(), wrapper.getLowerBound(),
                                                             wrapper.getUpperBound(), wrapper.isLowerInclusive(),
                                                             wrapper.isUpperInclusive());

        Function<TermRangeQueryNodeFacade, ICriterion> queryToCriterion = CRITERION_TABLE.get(attributeType,
                                                                                              rangeComparison);

        if (queryToCriterion == null) {
            Message message = new MessageImpl(QueryParserMessages.UNSUPPORTED_ATTRIBUTE_TYPE_FOR_RANGE_QUERY,
                    attributeType);
            LOGGER.error(message.getLocalizedMessage());
            throw new QueryNodeException(message);
        }
        return queryToCriterion.apply(wrapper);
    }

    /**
     * Return the range comparison type based on the lower/upper values and if they are inclusive/exclusive
     *
     * @param pLowerText
     * @param pUpperText
     * @param pIsLowerInclusive
     * @param pIsUpperInclusive
     * @return
     * @throws QueryNodeException
     */
    private RangeComparison getRangeComparison(String pField, String pLowerText, String pUpperText, // NOSONAR
            boolean pIsLowerInclusive, boolean pIsUpperInclusive) throws QueryNodeException {
        if (pLowerText.isEmpty() && pUpperText.isEmpty()) {
            throw new QueryNodeException(new MessageImpl(QueryParserMessages.RANGE_NUMERIC_CANNOT_BE_EMPTY, pField));
        } else if (pLowerText.isEmpty() && !pUpperText.isEmpty() && pIsUpperInclusive) {
            return RangeComparison.LE;
        } else if (pLowerText.isEmpty() && !pUpperText.isEmpty() && !pIsUpperInclusive) {
            return RangeComparison.LT;
        } else if (!pLowerText.isEmpty() && pUpperText.isEmpty() && pIsLowerInclusive) {
            return RangeComparison.GE;
        } else if (!pLowerText.isEmpty() && pUpperText.isEmpty() && !pIsLowerInclusive) {
            return RangeComparison.GT;
        } else {
            return RangeComparison.BETWEEN;
        }
    }

}
