/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service.builder;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;

/**
 * @author Marc Sordi
 */
@FunctionalInterface
public interface ICriterionQueryBuilder extends QueryBuilder {

    @Override
    ICriterion build(QueryNode pQueryNode) throws QueryNodeException;
}
