/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.queryparser.service;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryParserHelper;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxParser;
import org.apache.lucene.queryparser.flexible.standard.processors.StandardQueryNodeProcessorPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.queryparser.service.builder.RegardsQueryTreeBuilder;
import fr.cnes.regards.modules.queryparser.service.cache.attributemodel.IAttributeModelCache;

/**
 * Custom implementation of Lucene's {@link QueryParserHelper}.
 * For more complex customizability, consider implementing {@link CommonQueryParserConfiguration}.
 *
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
@Component
public class RegardsQueryParser extends QueryParserHelper {

    /**
     * Default field. Use a random string because we want to disable the lucene's default field behaviour.
     */
    private static final String DEFAULT_FIELD = "defaultField";

    /**
     * Constructor
     * @param pAttributeModelCache provides access to {@link AttributeModel}s with caching facilities
     */
    public RegardsQueryParser(@Autowired IAttributeModelCache pAttributeModelCache) {
        super(new StandardQueryConfigHandler(), new StandardSyntaxParser(),
              new StandardQueryNodeProcessorPipeline(null), new RegardsQueryTreeBuilder(pAttributeModelCache));
        setEnablePositionIncrements(true);
        setAllowLeadingWildcard(true);
    }

    @Override
    public String toString() {
        return "<RegardsQueryParser config=\"" + this.getQueryConfigHandler() + "\"/>";
    }

    /**
     * Call pase with the default field.
     *
     * @param query
     *            the query string
     * @return the object built from the query
     *
     * @throws QueryNodeException
     *             if something wrong happens along the three phases
     */
    public ICriterion parse(final String query) throws QueryNodeException {
        return (ICriterion) super.parse(query, DEFAULT_FIELD);
    }

    /**
     * Set to <code>true</code> to allow leading wildcard characters.
     * <p>
     * When set, <code>*</code> or <code>?</code> are allowed as the first character of a PrefixQuery and WildcardQuery.
     * Note that this can produce very slow queries on big indexes.
     * <p>
     * Default: false.
     */
    public void setAllowLeadingWildcard(final boolean allowLeadingWildcard) {
        getQueryConfigHandler().set(ConfigurationKeys.ALLOW_LEADING_WILDCARD, allowLeadingWildcard);
    }

    /**
     * Set to <code>true</code> to enable position increments in result query.
     * <p>
     * When set, result phrase and multi-phrase queries will be aware of position increments. Useful when e.g. a
     * StopFilter increases the position increment of the token that follows an omitted token.
     * <p>
     * Default: false.
     */
    public void setEnablePositionIncrements(final boolean enabled) {
        getQueryConfigHandler().set(ConfigurationKeys.ENABLE_POSITION_INCREMENTS, enabled);
    }

}
