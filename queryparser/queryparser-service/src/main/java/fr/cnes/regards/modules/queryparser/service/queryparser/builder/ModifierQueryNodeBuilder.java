/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.queryparser.service.queryparser.builder;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * @author Marc Sordi
 *
 */
public class ModifierQueryNodeBuilder implements ICriterionQueryBuilder {

    @Override
    public ICriterion build(final QueryNode pQueryNode) throws QueryNodeException {
        final ModifierQueryNode modifierQueryNode = (ModifierQueryNode) pQueryNode;
        return (ICriterion) (modifierQueryNode).getChild().getTag(QueryTreeBuilder.QUERY_TREE_BUILDER_TAGID);
    }

}
