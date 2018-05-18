/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.builders.QueryTreeBuilder;
import org.apache.lucene.queryparser.flexible.core.nodes.AndQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.BooleanQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FieldQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.FuzzyQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.GroupQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.ModifierQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.OrQueryNode;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.TermRangeQueryNode;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;

/**
 * Define the REGARDS specific query nodes building strategies.
 *
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
public class RegardsQueryTreeBuilder extends QueryTreeBuilder implements ICriterionQueryBuilder {

    /**
     * Constructor
     * @param finder attribute finder
     */
    public RegardsQueryTreeBuilder(IAttributeFinder finder) {

        // Register builder
        setBuilder(FieldQueryNode.class, new FieldQueryNodeBuilder(finder));
        setBuilder(AndQueryNode.class, new AndQueryNodeBuilder());
        setBuilder(OrQueryNode.class, new OrQueryNodeBuilder());
        setBuilder(ModifierQueryNode.class, new ModifierQueryNodeBuilder());
        setBuilder(TermRangeQueryNode.class, new TermRangeQueryNodeBuilder(finder));
        setBuilder(WildcardQueryNode.class, new WildcardQueryNodeBuilder());
        setBuilder(GroupQueryNode.class, new GroupQueryNodeBuilder());
        setBuilder(FuzzyQueryNode.class, new UnsupportedQueryNodeBuilder());
        setBuilder(BooleanQueryNode.class, new BooleanNodeQueryBuilder());
    }

    @Override
    public ICriterion build(final QueryNode queryNode) throws QueryNodeException {
        return (ICriterion) super.build(queryNode);
    }

}
