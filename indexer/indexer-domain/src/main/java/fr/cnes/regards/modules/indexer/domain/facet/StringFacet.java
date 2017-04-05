package fr.cnes.regards.modules.indexer.domain.facet;

import java.util.Map;

import com.google.gson.annotations.JsonAdapter;

import fr.cnes.regards.modules.indexer.domain.facet.adapters.gson.StringFacetSerializer;

/**
 * String facet. It represents a String cloud ie most common terms and associated occurrence counts.
 *
 * @author oroussel
 */
@JsonAdapter(value = StringFacetSerializer.class)
public class StringFacet extends AbstractFacet<Map<String, Long>> {

    /**
     * Facet values. Key is String value, value is occurrence count of the key
     */
    //    @JsonAdapter(value = StringFacetValuesSerializer.class)
    private final Map<String, Long> valueMap;

    public StringFacet(String pAttributeName, Map<String, Long> pValueMap) {
        super(pAttributeName);
        valueMap = pValueMap;
    }

    @Override
    public FacetType getType() {
        return FacetType.STRING;
    }

    @Override
    public Map<String, Long> getValues() {
        return this.valueMap;
    }

}
