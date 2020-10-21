/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.opensearch.service.builder;

import java.util.function.Function;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.core.util.StringUtils;
import org.apache.lucene.queryparser.flexible.messages.Message;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.ImmutableTable;

import fr.cnes.regards.modules.dam.domain.entities.criterion.IFeatureCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.dto.properties.PropertyType;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;
import fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages;

/**
 * Builds a {@link RangeCriterion} from a {@link TermRangeQueryNode} object.
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
    private static final ImmutableTable<PropertyType, RangeComparison, Function<TermRangeQueryNodeFacade, ICriterion>> CRITERION_TABLE = new ImmutableTable.Builder<PropertyType, RangeComparison, Function<TermRangeQueryNodeFacade, ICriterion>>()
            .put(PropertyType.INTEGER, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
            .put(PropertyType.INTEGER, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
            .put(PropertyType.INTEGER, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
            .put(PropertyType.INTEGER, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
            .put(PropertyType.INTEGER, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble(), pFacade.isLowerInclusive() ,pFacade.getUpperBoundAsDouble(), pFacade.isUpperInclusive()))

//            .put(PropertyType.INTEGER_ARRAY, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsInteger()))
//            .put(PropertyType.INTEGER_ARRAY, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsInteger()))
//            .put(PropertyType.INTEGER_ARRAY, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsInteger()))
//            .put(PropertyType.INTEGER_ARRAY, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsInteger()))
//            .put(PropertyType.INTEGER_ARRAY, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsInteger(), pFacade.getUpperBoundAsInteger()))
//
//            .put(PropertyType.INTEGER_INTERVAL, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsInteger()))
//            .put(PropertyType.INTEGER_INTERVAL, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsInteger()))
//            .put(PropertyType.INTEGER_INTERVAL, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsInteger()))
//            .put(PropertyType.INTEGER_INTERVAL, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsInteger()))
//            .put(PropertyType.INTEGER_INTERVAL, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsInteger(), pFacade.getUpperBoundAsInteger()))

            .put(PropertyType.DOUBLE, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
            .put(PropertyType.DOUBLE, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
            .put(PropertyType.DOUBLE, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
            .put(PropertyType.DOUBLE, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
            .put(PropertyType.DOUBLE, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble(), pFacade.isLowerInclusive(), pFacade.getUpperBoundAsDouble(),pFacade.isUpperInclusive()))

//            .put(PropertyType.DOUBLE_ARRAY, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
//            .put(PropertyType.DOUBLE_ARRAY, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
//            .put(PropertyType.DOUBLE_ARRAY, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
//            .put(PropertyType.DOUBLE_ARRAY, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
//            .put(PropertyType.DOUBLE_ARRAY, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble(), pFacade.getUpperBoundAsDouble()))
//
//            .put(PropertyType.DOUBLE_INTERVAL, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
//            .put(PropertyType.DOUBLE_INTERVAL, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
//            .put(PropertyType.DOUBLE_INTERVAL, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
//            .put(PropertyType.DOUBLE_INTERVAL, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
//            .put(PropertyType.DOUBLE_INTERVAL, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble(), pFacade.getUpperBoundAsDouble()))

            .put(PropertyType.LONG, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
            .put(PropertyType.LONG, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble()))
            .put(PropertyType.LONG, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
            .put(PropertyType.LONG, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDouble()))
            .put(PropertyType.LONG, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDouble(), pFacade.isLowerInclusive(), pFacade.getUpperBoundAsDouble(),pFacade.isUpperInclusive()))

//            .put(PropertyType.LONG_ARRAY, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsLong()))
//            .put(PropertyType.LONG_ARRAY, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsLong()))
//            .put(PropertyType.LONG_ARRAY, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsLong()))
//            .put(PropertyType.LONG_ARRAY, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsLong()))
//            .put(PropertyType.LONG_ARRAY, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsLong(), pFacade.getUpperBoundAsLong()))
//
//            .put(PropertyType.LONG_INTERVAL, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsLong()))
//            .put(PropertyType.LONG_INTERVAL, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsLong()))
//            .put(PropertyType.LONG_INTERVAL, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsLong()))
//            .put(PropertyType.LONG_INTERVAL, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsLong()))
//            .put(PropertyType.LONG_INTERVAL, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsLong(), pFacade.getUpperBoundAsLong()))

            .put(PropertyType.DATE_ISO8601, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime()))
            .put(PropertyType.DATE_ISO8601, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime()))
            .put(PropertyType.DATE_ISO8601, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDateTime()))
            .put(PropertyType.DATE_ISO8601, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDateTime()))
            .put(PropertyType.DATE_ISO8601, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime(), pFacade.isLowerInclusive(), pFacade.getUpperBoundAsDateTime(),pFacade.isUpperInclusive()))

//            .put(PropertyType.DATE_ARRAY, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime()))
//            .put(PropertyType.DATE_ARRAY, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime()))
//            .put(PropertyType.DATE_ARRAY, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDateTime()))
//            .put(PropertyType.DATE_ARRAY, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDateTime()))
//            .put(PropertyType.DATE_ARRAY, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime(), pFacade.getUpperBoundAsDateTime()))
//
//            .put(PropertyType.DATE_INTERVAL, RangeComparison.GE, pFacade -> IFeatureCriterion.ge(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime()))
//            .put(PropertyType.DATE_INTERVAL, RangeComparison.GT, pFacade -> IFeatureCriterion.gt(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime()))
//            .put(PropertyType.DATE_INTERVAL, RangeComparison.LE, pFacade -> IFeatureCriterion.le(pFacade.getAttModel(), pFacade.getUpperBoundAsDateTime()))
//            .put(PropertyType.DATE_INTERVAL, RangeComparison.LT, pFacade -> IFeatureCriterion.lt(pFacade.getAttModel(), pFacade.getUpperBoundAsDateTime()))
//            .put(PropertyType.DATE_INTERVAL, RangeComparison.BETWEEN, pFacade -> IFeatureCriterion.between(pFacade.getAttModel(), pFacade.getLowerBoundAsDateTime(), pFacade.getUpperBoundAsDateTime()))

            .build();
    // @formatter:on

    /**
     * @param attributeFinder attribute finder
     */
    public TermRangeQueryNodeBuilder(IAttributeFinder attributeFinder) {
        super();
        this.attributeFinder = attributeFinder;
    }

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        TermRangeQueryNode rangeNode = (TermRangeQueryNode) pQueryNode;

        // Retrieve the corresponding model
        AttributeModel attModel;
        try {
            attModel = attributeFinder.findByName(StringUtils.toString(rangeNode.getField()));
        } catch (OpenSearchUnknownParameter e) {
            throw new QueryNodeException(
                    new MessageImpl(QueryParserMessages.FIELD_TYPE_UNDETERMINATED, e.getMessage(), e));
        }

        // Extract info for node
        TermRangeQueryNodeFacade wrapper = new TermRangeQueryNodeFacade(rangeNode, attModel);

        // Compute the type of range comparison: lower/greater than/equal or between
        RangeComparison rangeComparison = getRangeComparison(wrapper.getField(), wrapper.getLowerBound(),
                                                             wrapper.getUpperBound(), wrapper.isLowerInclusive(),
                                                             wrapper.isUpperInclusive());

        Function<TermRangeQueryNodeFacade, ICriterion> queryToCriterion = CRITERION_TABLE.get(attModel.getType(),
                                                                                              rangeComparison);

        if (queryToCriterion == null) {
            Message message = new MessageImpl(QueryParserMessages.UNSUPPORTED_ATTRIBUTE_TYPE_FOR_RANGE_QUERY,
                    attModel.getType());
            LOGGER.error(message.getLocalizedMessage());
            throw new QueryNodeException(message);
        }

        return queryToCriterion.apply(wrapper);
    }

    /**
     * Return the range comparison type based on the lower/upper values and if they are inclusive/exclusive
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
