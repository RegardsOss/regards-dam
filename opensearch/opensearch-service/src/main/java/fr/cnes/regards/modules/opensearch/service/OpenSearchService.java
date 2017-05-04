/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;
import fr.cnes.regards.modules.opensearch.service.geoparser.GeoParser;
import fr.cnes.regards.modules.opensearch.service.queryparser.QueryParser;

/**
 * Parses generic OpenSearch requests like <code>q={searchTerms}&lat={geo:lat?}&lon={geo:lon?}&r={geo:radius?}&g=POLYGON((0.582%2040.496%2C%200.231%2040.737%2C%200.736%2042.869%2C%203.351%2042.386%2C%203.263%2041.814%2C%202.164%2041.265%2C%200.978%20%20%2040.957%2C%200.802%2040.781%2C%200.978%2040.649%2C%200.582%2040.496))</code>
 * <p>
 * It is coded so that you can add as many parsers as you want, each handling a specific part of the request. You just need to implement a new {@link IParser}, and register it in the <code>aggregate</code> method.
 * @author Xavier-Alexandre Brochard
 */
@Service
public class OpenSearchService implements IOpenSearchService {

    /**
     * The aggregated parser
     */
    private static IParser parser;

    /**
     * The parsers aggregation strategy. This one generates a parsers wich makes a "AndCriterion".
     */
    private static final IParserAggregator AGGREGATOR = new AndParserAggregator();

    /**
     * @param pQueryParser Parses the "q" part of an OpenSearch request. Autowired by Spring. Must not be null.
     * @param pGeoParser Parses the "lat"/"lon"/"r"/"g" part of an OpenSearch request. Autowired by Spring. Must not be null.
     */
    public OpenSearchService(@Autowired QueryParser pQueryParser, @Autowired GeoParser pGeoParser) {
        super();
        parser = AGGREGATOR.aggregate(pQueryParser, pGeoParser);
    }

    @Override
    public ICriterion parse(Map<String, String> pParameters) throws OpenSearchParseException {
        return parser.parse(pParameters);
    }

}
