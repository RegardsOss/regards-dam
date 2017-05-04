/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.queryparser.builder;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * Set this generic builder in a {@link QueryTreeBuilder} constructor for disabling specific functionalities.
 *
 * @author Xavier-Alexandre Brochard
 */
public class UnsupportedQueryNodeBuilder implements ICriterionQueryBuilder {

    /**
     * Systematicly throw an error
     */
    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        throw new QueryNodeException(new MessageImpl(QueryParserMessages.LUCENE_QUERY_CONVERSION_ERROR,
                pQueryNode.toQueryString(new EscapeQuerySyntaxImpl()), pQueryNode.getClass().getName()));
    }

}
