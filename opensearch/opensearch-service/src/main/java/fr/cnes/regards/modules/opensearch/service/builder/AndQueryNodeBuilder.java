/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
 *
 * This file is part of REGARDS.
 *
 * REGARDS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * REGARDS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REGARDS. If not, see <http://www.gnu.org/licenses/>.
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
    public ICriterion build(final QueryNode queryNode) throws QueryNodeException {
        final AndQueryNode andQueryNode = (AndQueryNode) queryNode;
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
