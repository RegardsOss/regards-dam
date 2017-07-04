/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.models.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.opensearch.service.cache.attributemodel.IAttributeFinder;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;
import fr.cnes.regards.modules.opensearch.service.parser.CircleParser;
import fr.cnes.regards.modules.opensearch.service.parser.GeometryParser;
import fr.cnes.regards.modules.opensearch.service.parser.IParser;
import fr.cnes.regards.modules.opensearch.service.parser.QueryParser;

/**
 * Parses generic OpenSearch requests like <code>q={searchTerms}&lat={geo:lat?}&lon={geo:lon?}&r={geo:radius?}&g=POLYGON((0.582%2040.496%2C%200.231%2040.737%2C%200.736%2042.869%2C%203.351%2042.386%2C%203.263%2041.814%2C%202.164%2041.265%2C%200.978%20%20%2040.957%2C%200.802%2040.781%2C%200.978%2040.649%2C%200.582%2040.496))</code>
 * <p>
 * It is coded so that you can add as many parsers as you want, each handling a specific part of the request. You just need to implement a new {@link IParser}, and register it in the <code>aggregate</code> method.
 * It is thread safe
 * @author Xavier-Alexandre Brochard
 */
@Service
@Primary
public class OpenSearchServiceThreadSafe implements IOpenSearchService {

    private final List<IParser> parsers;

    /**
     * @param finder Provides access to {@link AttributeModel}s with caching facilities. Autowired by Spring. Must not be null.
     */
    public OpenSearchServiceThreadSafe(@Autowired IAttributeFinder finder) {
        parsers = new ArrayList<>();
        parsers.add(new QueryParser(finder));
        parsers.add(new GeometryParser());
        parsers.add(new CircleParser());
    }

    @Override
    public ICriterion parse(Map<String, String> queryParameters) throws OpenSearchParseException {

        List<ICriterion> criteria = new ArrayList<>();
        for (IParser parser : parsers) {
            // Parse parameters ... may return null if parser required parameter(s) not set
            ICriterion crit = parser.parse(queryParameters);
            if (crit != null) {
                criteria.add(crit);
            }
        }
        if (criteria.isEmpty()) {
            return null;
        } else {
            return ICriterion.and(criteria.toArray(new ICriterion[criteria.size()]));
        }
    }

}
