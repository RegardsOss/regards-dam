/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.opensearch.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Parses a map of parameters into a {@link ICriterion}.
 * @author Xavier-Alexandre Brochard
 */
@FunctionalInterface
public interface IParser {

    /**
     * Parses the passed map of parameters into a {@link ICriterion}.<br>
     * For example, we expect
     * {
     *  q => "date:[* TO 2012-01-01]"
     * }
     * or
     * {
     *  lat => 43.25
     *  lon => -123.45
     *  r   => 10
     * }
     *
     * @param pParameters the map of parameters
     * @return the {@link ICriterion}
     * @throws OpenSearchParseException when an error occurs during parsing
     */
    ICriterion parse(Map<String, String> pParameters) throws OpenSearchParseException;

    /**
     * Parses the passed OpenSearch request string.<br>
     * For example, we expect
     * q="date:[* TO 2012-01-01]"<br>
     * or<br>
     * lat=43.25&lon=-123.45&r=10
     *
     * @param pParameters the string containing the parameters
     * @return the {@link ICriterion}
     */
    default ICriterion parse(String pParameters) throws OpenSearchParseException {
        try {
            List<NameValuePair> asList = URLEncodedUtils.parse(new URI("http://dummy?" + pParameters), "UTF-8");
            Map<String, String> asMap = new HashMap<>();
            asList.forEach(item -> asMap.put(item.getName(), item.getValue()));
            return parse(asMap);
        } catch (URISyntaxException e) {
            throw new OpenSearchParseException(e);
        }
    }
}
