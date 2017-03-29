package fr.cnes.regards.modules.indexer.domain.facet;

import java.util.Map;

/**
 * String facet. It represents a String cloud ie most common terms and associated occurrence counts.
 * 
 * @author oroussel
 */
public class StringFacet extends AbstractFacet<Map<String, Long>> {

    /**
     * Serial
     */
    private static final long serialVersionUID = 1205741862561256034L;

    /**
     * Facet values. Key is String value, value is occurrence count of the key
     */
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
