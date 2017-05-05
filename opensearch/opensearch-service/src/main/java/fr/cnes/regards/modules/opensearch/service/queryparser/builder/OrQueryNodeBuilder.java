/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.queryparser.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.OrCriterion;

/**
 * Builds a {@link OrCriterion} from a {@link OrQueryNode} object.
 *
 * @author Xavier-Alexandre Brochard
 */
public class OrQueryNodeBuilder implements ICriterionQueryBuilder {

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        final OrQueryNode orQueryNode = (OrQueryNode) pQueryNode;
        final List<QueryNode> children = orQueryNode.getChildren();

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
        return ICriterion.or(criterions);
    }

}
