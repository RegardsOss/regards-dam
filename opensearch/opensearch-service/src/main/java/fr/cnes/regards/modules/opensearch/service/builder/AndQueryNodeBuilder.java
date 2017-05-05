/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * @author Marc Sordi
 *
 */
public class AndQueryNodeBuilder implements ICriterionQueryBuilder {

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        final AndQueryNode andQueryNode = (AndQueryNode) pQueryNode;
        final List<QueryNode> children = andQueryNode.getChildren();

        // Build criterions
        final List<ICriterion> criterions = new ArrayList<>();
        if (children != null) {
            for (final QueryNode child : children) {
                final Object obj = child.getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);

                if (obj != null) {
                    criterions.add((ICriterion) obj);
                }
            }
        }
        return ICriterion.and(criterions);
    }

}
