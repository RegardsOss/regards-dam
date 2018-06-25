/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
