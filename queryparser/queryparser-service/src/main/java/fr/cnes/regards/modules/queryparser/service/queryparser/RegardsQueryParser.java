/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.queryparser.service.queryparser;

import java.util.Map;

import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.core.QueryParserHelper;
import org.apache.lucene.queryparser.flexible.standard.CommonQueryParserConfiguration;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler;
import org.apache.lucene.queryparser.flexible.standard.config.StandardQueryConfigHandler.ConfigurationKeys;
import org.apache.lucene.queryparser.flexible.standard.parser.StandardSyntaxParser;
import org.apache.lucene.queryparser.flexible.standard.processors.StandardQueryNodeProcessorPipeline;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.queryparser.service.IParser;
import fr.cnes.regards.modules.queryparser.service.exception.OpenSearchParseException;
import fr.cnes.regards.modules.queryparser.service.queryparser.builder.RegardsQueryTreeBuilder;
import fr.cnes.regards.modules.queryparser.service.queryparser.cache.attributemodel.IAttributeModelCache;

/**
 * Custom implementation of Lucene's {@link QueryParserHelper}.<br>
 * For more complex customizability, consider implementing {@link CommonQueryParserConfiguration}.<br>
 *
 * This {@link IParser} implementation only handles the the "q" part of the OpenSearch request.
 *
 * @author Marc Sordi
 * @author Xavier-Alexandre Brochard
 */
@Component
public class RegardsQueryParser extends QueryParserHelper implements IParser {

    /**
     * The request parameter containing the OpenSearch query
     */
    private static final String QUERY_PARAMETER = "q";

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

    /* (non-Javadoc)
     * @see fr.cnes.regards.modules.queryparser.service.IParser#parse(java.util.Map)
     */
    @Override
    public ICriterion parse(Map<String, String> pParameters) throws OpenSearchParseException {
        String q = pParameters.get(QUERY_PARAMETER);
        Assert.notNull(q);
        try {
            return (ICriterion) super.parse(q, DEFAULT_FIELD);
        } catch (QueryNodeException e) {
            throw new OpenSearchParseException(e);
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
