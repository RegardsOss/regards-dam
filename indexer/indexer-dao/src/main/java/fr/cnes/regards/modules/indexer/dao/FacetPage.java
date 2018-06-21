package fr.cnes.regards.modules.indexer.dao;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import fr.cnes.regards.modules.indexer.domain.facet.IFacet;

/**
 * Page implementation with facets values
 *
 * @param <T> the type of which the page consists.
 * @author oroussel
 */
@SuppressWarnings("serial")
public class FacetPage<T> extends PageImpl<T> {

    private final Set<IFacet<?>> facets;

    private final Pageable pageable;

    public FacetPage(List<T> content, Set<IFacet<?>> facets, Pageable pageable, long total) {
        super(content, pageable, total);
        this.pageable = pageable;
        this.facets = facets;

    }

    public FacetPage(List<T> content, Set<IFacet<?>> facets) {
        super(content);
        this.pageable = null;
        this.facets = facets;
    }

    public Set<IFacet<?>> getFacets() {
        return facets;
    }

    public Pageable getPageable() {
        return pageable;
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
