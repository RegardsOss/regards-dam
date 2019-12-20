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
package fr.cnes.regards.modules.indexer.domain.facet.adapters.gson;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import fr.cnes.regards.modules.indexer.domain.facet.StringFacet;
import fr.cnes.regards.modules.indexer.domain.facet.adapters.gson.StringFacetSerializer.AdaptedFacet;

/**
 * Unit test for {@link StringFacetSerializer}
 *
 * @author Xavier-Alexandre Brochard
 */
public class StringFacetSerializerTest {

    @Test
    public final void shouldBuilProperOpenSearchQuery() {
        Map<String, Long> values = new HashMap<>();
        values.put("toto", 1L);
        StringFacet facet = new StringFacet("myattributename", values, 12);

        AdaptedFacet adapted = new StringFacetSerializer.AdaptedFacet(facet);
        String openSearchQuery = adapted.getValues().get(0).getOpenSearchQuery();

        Assert.assertEquals("myattributename:\"toto\"", openSearchQuery);
    }

    @Test
    public final void shouldAddQuotesForPhraseQueries() {
        Map<String, Long> values = new HashMap<>();
        values.put("Harry Potter", 1L);
        StringFacet facet = new StringFacet("myattributename", values, 23);

        AdaptedFacet adapted = new StringFacetSerializer.AdaptedFacet(facet);
        String openSearchQuery = adapted.getValues().get(0).getOpenSearchQuery();

        Assert.assertEquals("myattributename:\"Harry Potter\"", openSearchQuery);
    }

}
