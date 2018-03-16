package fr.cnes.regards.modules.indexer.domain.facet;

import java.io.Serializable;

/**
 * Identifies a facet
 * @param <T> values type (usually a map, it depends on IFacet implementation)
 */
public interface IFacet<T> extends Serializable {

    /**
     * Returns concerned attribute name
     * @return attribute name
     */
    String getAttributeName();

    FacetType getType();

    T getValues();

    /**
     * Number of values not covered by facet (0 by default, most of facets cover all values)
     */
    default long getOthers() {
        return 0;
    }
}
