/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.builder;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.PointRangeQueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.RangeCriterion;

/**
 * Builds a {@link RangeCriterion} from a {@link PointRangeQueryNode} object.
 *
 * @see org.apache.lucene.queryparser.flexible.standard.builders.PointRangeQueryNodeBuilder#build(QueryNode)
 * @author Xavier-Alexandre Brochard
 */
public class GroupQueryNodeBuilder implements ICriterionQueryBuilder {

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        final GroupQueryNode groupNode = (GroupQueryNode) pQueryNode;

        return (ICriterion) (groupNode).getChild().getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
    }

}
