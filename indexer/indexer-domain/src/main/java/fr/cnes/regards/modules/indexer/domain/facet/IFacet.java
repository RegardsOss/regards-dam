package fr.cnes.regards.modules.indexer.domain.facet;

import java.io.Serializable;

import fr.cnes.regards.framework.gson.annotation.Gsonable;

/**
 * Identifies a facet
 *
 * @param <T>
 *            values type (usually a map, it depends on IFacet implementation)
 */
@Gsonable
public interface IFacet<T> extends Serializable {

    /**
     * Returns concerned attribute name
     *
     * @return attribute name
     */
    String getAttributeName();

    FacetType getType();

    T getValues();
}
