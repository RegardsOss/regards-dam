/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.PointQueryNode;

import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;
import fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages;

/**
 * Builds a {@link StringMatchCriterion} from a {@link FieldQueryNode} object when the value is a String.<br>
 * Builds a {@link IntMatchCriterion} from a {@link PointQueryNode} object when the value is an Integer.<br>
 * Builds a {@link RangeCriterion} from a {@link PointQueryNode} object when the value is a double.<br>
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
public class FieldQueryNodeBuilder implements ICriterionQueryBuilder {

    /**
     * Service retrieving the up-to-date list of {@link AttributeModel}s. Autowired by Spring.
     */
    private final IAttributeFinder finder;

    /**
     * @param finder Service permitting to retrieve up-to-date list of {@link AttributeModel}s
     */
    public FieldQueryNodeBuilder(IAttributeFinder finder) {
        super();
        this.finder = finder;
    }

    @Override
    public ICriterion build(final QueryNode queryNode) throws QueryNodeException { // NOSONAR
        final FieldQueryNode fieldNode = (FieldQueryNode) queryNode;

        final String field = fieldNode.getFieldAsString();
        final String value = fieldNode.getValue().toString();

        AttributeModel attributeModel;
        try {
            attributeModel = finder.findByName(field);
        } catch (OpenSearchUnknownParameter e) {
            throw new QueryNodeException(new MessageImpl(QueryParserMessages.FIELD_TYPE_UNDETERMINATED, e.getMessage()),
                                         e);
        }

        switch (attributeModel.getType()) {
            case INTEGER:
            case INTEGER_ARRAY:
                // Important :
                // We have to do it because the value of the criterion returned by Elasticsearch is always a double value,
                // even if the value is an integer value.
                // For example, it did not work, then the open search criterion was : "property:26.0"
                int val;
                try {
                    val = Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    Double doubleValue = Double.parseDouble(value);
                    val = doubleValue.intValue();
                }
                return ICriterion.eq(field, val);
            case DOUBLE:
            case DOUBLE_ARRAY:
                Double asDouble = Double.parseDouble(value);
                return ICriterion.eq(field, asDouble, asDouble - Math.nextDown(asDouble));
            case LONG:
            case LONG_ARRAY:
                // Important :
                // We have to do it because the value of the criterion returned by Elasticsearch is always a double value,
                // even if the value is a long value.
                // For example, it did not work, then the open search criterion was : "property:26.0"
                long valL;
                try {
                    valL = Long.parseLong(value);
                } catch (NumberFormatException ex) {
                    Double doubleValue = Double.parseDouble(value);
                    valL = doubleValue.longValue();
                }
                return ICriterion.eq(field, valL);
            case STRING:
                return ICriterion.eq(field, value);
            case STRING_ARRAY:
                return ICriterion.contains(field, value);
            case DATE_ISO8601:
                return ICriterion.eq(field, OffsetDateTimeAdapter.parse(value));
            case BOOLEAN:
                return ICriterion.eq(field, Boolean.valueOf(value));
            default:
                throw new QueryNodeException(new MessageImpl(QueryParserMessages.UNSUPPORTED_ATTRIBUTE_TYPE, field));
        }
    }

}
