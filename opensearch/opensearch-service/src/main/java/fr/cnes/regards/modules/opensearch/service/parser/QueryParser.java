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
package fr.cnes.regards.modules.opensearch.service.parser;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryParserHelper;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxParser;
import org.apache.lucene.queryparser.flexible.standard.processors.StandardQueryNodeProcessorPipeline;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.MultiValueMap;

import com.google.common.base.Strings;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.builder.RegardsQueryTreeBuilder;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Custom implementation of Lucene's {@link QueryParserHelper}.<br>
 * For more complex customizability, consider implementing {@link CommonQueryParserConfiguration}.<br>
 *
 * This {@link IParser} implementation only handles the the "q" part of the OpenSearch request.<br>
 *
 * Expects HTML-encoded string values. For example, <code>q=title(harrypotter OR starwars)</code> will fail, but
 * <code>q=title%3A%28harrypotter+OR+starwars%29</code> will work
 *
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
public class QueryParser extends QueryParserHelper implements IParser {

    /**
     * Class logger
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryParser.class);

    /**
     * The request parameter containing the OpenSearch query
     */
    public static final String QUERY_PARAMETER = "q";

    /**
     * Default field. Use a random string because we want to disable the lucene's default field behaviour.
     */
    private static final String DEFAULT_FIELD = "label";

    /**
     * Constructor
     * @param finder provides access to {@link AttributeModel}s with caching facilities
     */
    public QueryParser(IAttributeFinder finder) {
        super(new StandardQueryConfigHandler(), new StandardSyntaxParser(),
              new StandardQueryNodeProcessorPipeline(null), new RegardsQueryTreeBuilder(finder));
        setEnablePositionIncrements(true);
        setAllowLeadingWildcard(true);
        setLowercaseExpandedTerms(false);
    }

    @Override
    public String toString() {
        return "<QueryParser config=\"" + this.getQueryConfigHandler() + "\"/>";
    }

    @Override
    public ICriterion parse(MultiValueMap<String, String> parameters) throws OpenSearchParseException {

        String q = parameters.getFirst(QUERY_PARAMETER);

        // Check required query parameter
        if (Strings.isNullOrEmpty(q)) {
            return ICriterion.all();
        }
        try {
            return (ICriterion) super.parse(q, DEFAULT_FIELD);
        } catch (QueryNodeException e) {
            LOGGER.error("q parsing error", e);
            throw new OpenSearchParseException(e.getMessage(), e);
        }
    }

    /**
     * Set to <code>true</code> to allow leading wildcard characters.
     * <p>
     * When set, <code>*</code> or <code>?</code> are allowed as the first character of a PrefixQuery and WildcardQuery.
     * Note that this can produce very slow queries on big indexes.
     * <p>
     * Default: false.
     */
    public final void setAllowLeadingWildcard(final boolean allowLeadingWildcard) {
        getQueryConfigHandler().set(ConfigurationKeys.ALLOW_LEADING_WILDCARD, allowLeadingWildcard);
    }

    /**
     * Enable or disable lowercase regexp transformation
     */
    public final void setLowercaseExpandedTerms(final boolean lowercaseExpandedTerms) {
        getQueryConfigHandler().set(ConfigurationKeys.LOWERCASE_EXPANDED_TERMS, lowercaseExpandedTerms);
    }

    /**
     * Set to <code>true</code> to enable position increments in result query.
     * <p>
     * When set, result phrase and multi-phrase queries will be aware of position increments. Useful when e.g. a
     * StopFilter increases the position increment of the token that follows an omitted token.
     * <p>
     * Default: false.
     */
    public final void setEnablePositionIncrements(final boolean enabled) {
        getQueryConfigHandler().set(ConfigurationKeys.ENABLE_POSITION_INCREMENTS, enabled);
    }

}
