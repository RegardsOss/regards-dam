package fr.cnes.regards.modules.opensearch.service.builder;

import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

public class BooleanNodeQueryBuilder implements ICriterionQueryBuilder {

    @Override
    public ICriterion build(QueryNode queryNode) throws QueryNodeException {
        BooleanQueryNode booleanNode = (BooleanQueryNode) queryNode;
        List<QueryNode> children = booleanNode.getChildren();

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
        return criterions.get(0);
    }

}
