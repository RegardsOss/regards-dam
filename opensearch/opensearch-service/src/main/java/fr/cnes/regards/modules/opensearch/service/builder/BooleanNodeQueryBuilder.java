package fr.cnes.regards.modules.opensearch.service.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages;

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

        if (criterions.size() > 1) {
            StringJoiner joiner = new StringJoiner(", ");
            criterions.forEach(c -> joiner.add(c.toString()));
            throw new QueryNodeException(
                    new MessageImpl(QueryParserMessages.INDETERMINATED_RELATIONSHIP, joiner.toString()));
        }
        return criterions.get(0);
    }

}
