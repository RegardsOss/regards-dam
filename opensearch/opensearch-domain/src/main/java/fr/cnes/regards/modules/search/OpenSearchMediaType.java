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
package fr.cnes.regards.modules.search;

import org.springframework.http.MediaType;

/**
 * Define opensearch specifics media types
 * @author Sébastien Binda
 */
public class OpenSearchMediaType {

    /**
     * Public constant media type for {@code application/geo+json}.
     * @see #APPLICATION_JSON_UTF8
     */
    public static final MediaType APPLICATION_OPENSEARCH_DESC;

    public static final MediaType APPLICATION_OPENSEARCH_DESC_UTF8;

    public static final String APPLICATION_OPENSEARCH_DESC_VALUE = "application/opensearchdescription+xml";

    /**
     * A String equivalent of {@link MediaType#APPLICATION_JSON_UTF8}.
     */
    public static final String APPLICATION_OPENSEARCH_DESC_VALUE_UTF8_VALUE = APPLICATION_OPENSEARCH_DESC_VALUE
            + ";charset=UTF-8";

    static {
        APPLICATION_OPENSEARCH_DESC = MediaType.valueOf(APPLICATION_OPENSEARCH_DESC_VALUE);
        APPLICATION_OPENSEARCH_DESC_UTF8 = MediaType.valueOf(APPLICATION_OPENSEARCH_DESC_VALUE_UTF8_VALUE);

    }

    private OpenSearchMediaType() {
    }

}
