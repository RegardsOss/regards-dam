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

import java.util.Set;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.messages.QueryParserMessages;
import org.apache.lucene.queryparser.flexible.core.nodes.QueryNode;
import org.apache.lucene.queryparser.flexible.messages.MessageImpl;
import org.apache.lucene.queryparser.flexible.standard.nodes.WildcardQueryNode;
import org.apache.lucene.queryparser.flexible.standard.parser.EscapeQuerySyntaxImpl;

import com.sun.org.apache.bcel.internal.generic.IFEQ;
import fr.cnes.regards.modules.dam.domain.entities.criterion.IFeatureCriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.indexer.domain.criterion.MatchType;
import fr.cnes.regards.modules.indexer.domain.criterion.StringMatchCriterion;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchUnknownParameter;
import fr.cnes.regards.modules.opensearch.service.parser.QueryParser;

/**
 * Builds a {@link StringMatchCriterion} from a {@link WildcardQueryNode} object<br>
 * We cannot use Elasticsearch optimization with startsWith in case: harry* because of the following case: har*ry*. <br>
 *
 * @author Xavier-Alexandre Brochard
 */
public class WildcardQueryNodeBuilder implements ICriterionQueryBuilder {

    /**
     * Service retrieving the up-to-date list of {@link AttributeModel}s. Autowired by Spring.
     */
    private final IAttributeFinder finder;

    /**
     * @param finder Service permitting to retrieve up-to-date list of {@link AttributeModel}s
     */
    public WildcardQueryNodeBuilder(IAttributeFinder finder) {
        super();
        this.finder = finder;
    }

    @Override
    public ICriterion build(final QueryNode queryNode) throws QueryNodeException {
        WildcardQueryNode wildcardNode = (WildcardQueryNode) queryNode;

        String field = wildcardNode.getFieldAsString();
        String value = wildcardNode.getTextAsString();

        // Manage multisearch
        if (QueryParser.MULTISEARCH.equals(field)) {
            try {
                Set<AttributeModel> atts = MultiSearchHelper.discoverFields(finder, value);
                return IFeatureCriterion.multiMatchStartWith(atts, value);
            } catch (OpenSearchUnknownParameter e) {
                throw new QueryNodeException(new MessageImpl(
                        fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages.FIELD_TYPE_UNDETERMINATED,
                        e.getMessage()), e);
            }
        }

        AttributeModel attributeModel;
        try {
            attributeModel = finder.findByName(field);
        } catch (OpenSearchUnknownParameter e) {
            throw new QueryNodeException(new MessageImpl(
                    fr.cnes.regards.modules.opensearch.service.message.QueryParserMessages.FIELD_TYPE_UNDETERMINATED,
                    e.getMessage()), e);
        }

        value = value.replaceAll("\\*", ".*");
        return IFeatureCriterion.regexp(attributeModel, value);
    }

}
