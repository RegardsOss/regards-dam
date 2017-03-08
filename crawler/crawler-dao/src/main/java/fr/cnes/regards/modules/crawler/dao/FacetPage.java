package fr.cnes.regards.modules.crawler.dao;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.crawler.domain.facet.IFacet;

/**
 * Page implementation with facets values
 * @param <T> the type of which the page consists.
 * @author oroussel
 */
public class FacetPage<T> extends PageImpl<T> {

    /**
     * Facet map.
     * key is attribute name and value is of type {@link IFacet}
     */
    private Map<String, IFacet<?>> facetMap;

    public FacetPage(List<T> pContent, Map<String, IFacet<?>> pFacetMap, Pageable pPageable, long pTotal) {
        super(pContent, pPageable, pTotal);
        this.facetMap = pFacetMap;

    }

    public FacetPage(List<T> pContent, Map<String, IFacet<?>> pFacetMap) {
        super(pContent);
        this.facetMap = pFacetMap;
    }

    public Map<String, IFacet<?>> getFacetMap() {
        return facetMap;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        return super.equals(obj);
    }

}
