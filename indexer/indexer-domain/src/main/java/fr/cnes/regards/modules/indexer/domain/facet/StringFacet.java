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
    private final Map<String, Long> valueMap;

    /**
     * Number of values not covered by facet
     */
    private final long others;

    public StringFacet(String attName, Map<String, Long> valueMap, long others) {
        super(attName);
        this.valueMap = valueMap;
        this.others = others;
    }

    @Override
    public FacetType getType() {
        return FacetType.STRING;
    }

    @Override
    public Map<String, Long> getValues() {
        return this.valueMap;
    }

    @Override
    public long getOthers() {
        return others;
    }
}
