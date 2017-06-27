/*
 * LICENSE_PLACEHOLDER
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
        StringFacet facet = new StringFacet("myattributename", values);

        AdaptedFacet adapted = new StringFacetSerializer().new AdaptedFacet(facet);
        String openSearchQuery = adapted.getValues().get(0).getOpenSearchQuery();

        Assert.assertEquals("myattributename:toto", openSearchQuery);
    }

    @Test
    public final void shouldAddQuotesForPhraseQueries() {
        Map<String, Long> values = new HashMap<>();
        values.put("Harry Potter", 1L);
        StringFacet facet = new StringFacet("myattributename", values);

        AdaptedFacet adapted = new StringFacetSerializer().new AdaptedFacet(facet);
        String openSearchQuery = adapted.getValues().get(0).getOpenSearchQuery();

        Assert.assertEquals("myattributename:\"Harry Potter\"", openSearchQuery);
    }

}
