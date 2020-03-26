/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import fr.cnes.regards.modules.indexer.domain.criterion.ICriterion;
import fr.cnes.regards.modules.opensearch.service.exception.OpenSearchParseException;

/**
 * Parses a map of parameters into a {@link ICriterion}.
 * @author Xavier-Alexandre Brochard
 */
public interface IParser {

    /**
     * Parses the passed map of parameters into a {@link ICriterion}.<br>
     * For example, we expect
     * {
     * q => "date:[* TO 2012-01-01]"
     * }
     * or
     * {
     * lat => 43.25
     * lon => -123.45
     * r => 10
     * }
     * @param parameters the map of parameters
     * @return the {@link ICriterion}
     * @throws OpenSearchParseException when an error occurs during parsing
     */
    ICriterion parse(MultiValueMap<String, String> parameters) throws OpenSearchParseException;

    /**
     * Parses the passed OpenSearch request string.<br>
     * For example, we expect
     * q="date:[* TO 2012-01-01]"<br>
     * or<br>
     * lat=43.25&lon=-123.45&r=10
     * @param parameters the string containing the parameters
     * @return the {@link ICriterion}
     */
    default ICriterion parse(String parameters) throws OpenSearchParseException {
        try {
            List<NameValuePair> nameValues = URLEncodedUtils.parse(new URI("http://dummy?" + parameters),
                                                                   Charset.forName("UTF-8"));
            MultiValueMap<String, String> paramMap = new LinkedMultiValueMap<>();
            nameValues.forEach(nvp -> paramMap.add(nvp.getName(), nvp.getValue()));
            return parse(paramMap);
        } catch (URISyntaxException e) {
            throw new OpenSearchParseException(e);
        }
    }
}
