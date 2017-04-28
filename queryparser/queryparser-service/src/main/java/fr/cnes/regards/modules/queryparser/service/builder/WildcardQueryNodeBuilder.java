/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.queryparser.service.builder;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;

/**
 * Builds a {@link StringMatchCriterion} from a {@link WildcardQueryNode} object<br>
 * If the wildcard is leading (*example), use {@link MatchType#ENDS_WITH}<br>
 * If the wildcard is trailng (example*), use {@link MatchType#STARTS_WITH}<br>
 * If the wildcard is leading and trailng (*example*), use {@link MatchType#CONTAINS}<br>
 * A wildcard in the middle of the value (ex*mple) is not allowed
 *
 * @author Xavier-Alexandre Brochard
 */
public class WildcardQueryNodeBuilder implements ICriterionQueryBuilder {

    private static final String WILDCARD_STRING = "*";

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        WildcardQueryNode wildcardNode = (WildcardQueryNode) pQueryNode;

        String field = wildcardNode.getFieldAsString();
        String value = wildcardNode.getTextAsString();
        String val = value.replaceAll("[*]", "");

        if (value.endsWith(WILDCARD_STRING) && value.startsWith(WILDCARD_STRING)) {
            return ICriterion.contains(field, val);
        } else if (value.endsWith(WILDCARD_STRING)) {
            return ICriterion.endsWith(field, val);
        } else if (value.startsWith(WILDCARD_STRING)) {
            return ICriterion.startsWith(field, val);
        } else {
            throw new QueryNodeException(new MessageImpl(QueryParserMessages.LUCENE_QUERY_CONVERSION_ERROR,
                    pQueryNode.toQueryString(new EscapeQuerySyntaxImpl()), pQueryNode.getClass().getName()));
        }
    }

}
