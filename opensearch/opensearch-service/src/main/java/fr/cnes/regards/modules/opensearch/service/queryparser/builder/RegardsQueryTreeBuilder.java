/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.queryparser.builder;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.queryparser.cache.attributemodel.IAttributeModelCache;

/**
 * Define the REGARDS specific query nodes building strategies.
 *
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
public class RegardsQueryTreeBuilder extends QueryTreeBuilder implements ICriterionQueryBuilder {

    /**
     * Constructor
     * @param pAttributeModelCache provides access to attribute models with caching facilities
     */
    public RegardsQueryTreeBuilder(IAttributeModelCache pAttributeModelCache) {

        // Register builder
        setBuilder(FieldQueryNode.class, new FieldQueryNodeBuilder(pAttributeModelCache));
        setBuilder(AndQueryNode.class, new AndQueryNodeBuilder());
        setBuilder(OrQueryNode.class, new OrQueryNodeBuilder());
        setBuilder(ModifierQueryNode.class, new ModifierQueryNodeBuilder());
        setBuilder(TermRangeQueryNode.class, new TermRangeQueryNodeBuilder(pAttributeModelCache));
        setBuilder(WildcardQueryNode.class, new WildcardQueryNodeBuilder());
        setBuilder(GroupQueryNode.class, new GroupQueryNodeBuilder());
        setBuilder(FuzzyQueryNode.class, new UnsupportedQueryNodeBuilder());
    }

    @Override
    public ICriterion build(final QueryNode queryNode) throws QueryNodeException {
        return (ICriterion) super.build(queryNode);
    }

}
