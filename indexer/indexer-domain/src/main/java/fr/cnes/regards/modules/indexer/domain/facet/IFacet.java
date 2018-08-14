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

    /**
     * Allows to mutate attribute name after facet computation to reset it to a shorter one
     */
    void setAttributeName(String attributeName);

    FacetType getType();

    T getValues();

    long getOthers();
}
