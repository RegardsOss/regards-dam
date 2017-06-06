package fr.cnes.regards.modules.indexer.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.indexer.domain.IIndexable;
import fr.cnes.regards.modules.indexer.domain.facet.IFacet;

/**
 * Page implementation with facets values
 *
 * @param <T> the type of which the page consists.
 * @author oroussel
 */
public class FacetPage<T extends IIndexable> extends PageImpl<T> {

    /**
     * serialVersionUID field.
     *
     * @author CS
     * @since 1.0-SNAPSHOT
     */
    private static final long serialVersionUID = 2759694831627379041L;

    /**
     * Facet map. key is attribute name and value is of type {@link IFacet}
     */
    private Set<IFacet<?>> facets;

    public FacetPage(List<T> pContent, Set<IFacet<?>> pFacets, Pageable pPageable, long pTotal) {
        super(pContent, pPageable, pTotal);
        this.facets = pFacets;

    }

    public FacetPage(List<T> pContent, Set<IFacet<?>> pFacets) {
        super(pContent);
        this.facets = pFacets;
    }

    public Set<IFacet<?>> getFacets() {
        return facets;
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
