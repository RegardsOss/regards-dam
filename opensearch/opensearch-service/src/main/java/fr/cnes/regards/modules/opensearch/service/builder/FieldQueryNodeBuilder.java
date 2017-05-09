/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.builder;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.PointQueryNode;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.IntMatchCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeModelCache;
import fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages;

/**
 * Builds a {@link StringMatchCriterion} from a {@link FieldQueryNode} object when the value is a String.<br>
 * Builds a {@link IntMatchCriterion} from a {@link PointQueryNode} object when the value is an Integer.<br>
 * Builds a {@link RangeCriterion} from a {@link PointQueryNode} object when the value is a double.<br>
 *
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
public class FieldQueryNodeBuilder implements ICriterionQueryBuilder {

    /**
     * Service retrieving the up-to-date list of {@link AttributeModel}s. Autowired by Spring.
     */
    private final IAttributeModelCache attributeModelCache;

    /**
     * @param pAttributeModelCache
     *            Service retrieving the up-to-date list of {@link AttributeModel}s
     */
    public FieldQueryNodeBuilder(IAttributeModelCache pAttributeModelCache) {
        super();
        attributeModelCache = pAttributeModelCache;
    }

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException { // NOSONAR
        final FieldQueryNode fieldNode = (FieldQueryNode) pQueryNode;

        final String field = fieldNode.getFieldAsString();
        final String value = fieldNode.getValue().toString();

        AttributeModel attributeModel;
        try {
            attributeModel = attributeModelCache.findByName(field);
        } catch (EntityNotFoundException e) {
            throw new QueryNodeException(new MessageImpl(QueryParserMessages.FIELD_TYPE_UNDETERMINATED, field),
                    e);
        }

        switch (attributeModel.getType()) {
            case INTEGER:
            case INTEGER_ARRAY:
                return ICriterion.eq(field, Integer.parseInt(value));
            case DOUBLE:
            case DOUBLE_ARRAY:
                Double asDouble = Double.parseDouble(value);
                return ICriterion.eq(field, asDouble, asDouble - Math.nextDown(asDouble));
            case LONG:
            case LONG_ARRAY:
                Long asLong = Long.parseLong(value);
                return ICriterion.eq(field, asLong, asLong - Math.nextDown(asLong));
            case STRING:
                return ICriterion.eq(field, value);
            case STRING_ARRAY:
                return ICriterion.contains(field, value);
            default:
                throw new QueryNodeException(
                        new MessageImpl(QueryParserMessages.UNSUPPORTED_ATTRIBUTE_TYPE, field));
        }
    }

}
