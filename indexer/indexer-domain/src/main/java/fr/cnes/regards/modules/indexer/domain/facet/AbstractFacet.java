/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.indexer.domain.facet;

/**
 * IFacet facility to manage attribute name
 *
 * @param <T> facet type
 * @author oroussel
 */
@SuppressWarnings("serial")
public abstract class AbstractFacet<T> implements IFacet<T> {

    /**
     * Concerned attribute name
     */
    private String attributeName;

    /**
     * Number of values not covered by facet
     */
    private final long others;

    public AbstractFacet(String attributeName) {
        this(attributeName, 0);
    }

    public AbstractFacet(String attName, long others) {
        this.attributeName = attName;
        this.others = others;
    }

    @Override
    public String getAttributeName() {
        return this.attributeName;
    }

    @Override
    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    /**
     * Number of values not covered by facet (0 by default, most of facets cover all values)
     */
    @Override
    public long getOthers() {
        return this.others;
    }
}