package fr.cnes.regards.modules.opensearch.service.builder;

import java.util.Set;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;

import fr.cnes.regards.framework.gson.adapters.OffsetDateTimeAdapter;
import fr.cnes.regards.modules.dam.domain.entities.criterion.IFeatureCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;
import fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages;
import fr.cnes.regards.modules.opensearch.service.parser.QueryParser;

/**
 * @author Sylvain VISSIERE-GUERINET
 */
public class QuotedFieldQueryNodeBuilder implements ICriterionQueryBuilder {

    private final IAttributeFinder attributeFinder;

    public QuotedFieldQueryNodeBuilder(IAttributeFinder attributeFinder) {
        this.attributeFinder = attributeFinder;
    }

    @Override
    public ICriterion build(QueryNode queryNode) throws QueryNodeException {
        FieldQueryNode fieldNode = (FieldQueryNode) queryNode;

        String field = fieldNode.getFieldAsString();
        String value = fieldNode.getValue().toString();

        // Manage multisearch
        if (QueryParser.MULTISEARCH.equals(field)) {
            try {
                Set<AttributeModel> atts = MultiSearchHelper.discoverFields(attributeFinder, value);
                return IFeatureCriterion.multiMatch(atts, value);
            } catch (OpenSearchUnknownParameter e) {
                throw new QueryNodeException(
                        new MessageImpl(QueryParserMessages.FIELD_TYPE_UNDETERMINATED, e.getMessage()), e);
            }
        }

        AttributeModel attributeModel;
        try {
            attributeModel = attributeFinder.findByName(field);
        } catch (OpenSearchUnknownParameter e) {
            throw new QueryNodeException(new MessageImpl(QueryParserMessages.FIELD_TYPE_UNDETERMINATED, e.getMessage()),
                    e);
        }

        switch (attributeModel.getType()) {
            case INTEGER:
            case INTEGER_ARRAY:
                // Important :
                // We have to do it because the value of the criterion returned by Elasticsearch is always a double
                // value,
                // even if the value is an integer value.
                // For example, it did not work, then the open search criterion was : "property:26.0"
                int val;
                try {
                    val = Integer.parseInt(value);
                } catch (NumberFormatException ex) {
                    Double doubleValue = Double.parseDouble(value);
                    val = doubleValue.intValue();
                }
                return IFeatureCriterion.eq(attributeModel, val);
            case DOUBLE:
            case DOUBLE_ARRAY:
                Double asDouble = Double.parseDouble(value);
                return IFeatureCriterion.eq(attributeModel, asDouble, asDouble - Math.nextDown(asDouble));
            case LONG:
            case LONG_ARRAY:
                // Important :
                // We have to do it because the value of the criterion returned by Elasticsearch is always a double
                // value,
                // even if the value is a long value.
                // For example, it did not work, then the open search criterion was : "property:26.0"
                long valL;
                try {
                    valL = Long.parseLong(value);
                } catch (NumberFormatException ex) {
                    Double doubleValue = Double.parseDouble(value);
                    valL = doubleValue.longValue();
                }
                return IFeatureCriterion.eq(attributeModel, valL);
            case STRING:
            case STRING_ARRAY:
                return IFeatureCriterion.eq(attributeModel, value);
            case DATE_ISO8601:
                return IFeatureCriterion.eq(attributeModel, OffsetDateTimeAdapter.parse(value));
            case BOOLEAN:
                return IFeatureCriterion.eq(attributeModel, Boolean.valueOf(value));
            default:
                throw new QueryNodeException(new MessageImpl(QueryParserMessages.UNSUPPORTED_ATTRIBUTE_TYPE, field));
        }
    }
}
