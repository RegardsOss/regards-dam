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
package fr.cnes.regards.modules.indexer.domain.criterion;

import java.util.ArrayList;
import java.util.List;

/**
 * ICriterion aggregator
 * @author oroussel
 */
public abstract class AbstractMultiCriterion implements ICriterion {

    /**
     * Criterions
     */
    protected List<ICriterion> criterions = new ArrayList<>();

    protected AbstractMultiCriterion() {
        super();
    }

    public List<ICriterion> getCriterions() {
        return criterions;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (criterions == null ? 0 : criterions.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        AbstractMultiCriterion other = (AbstractMultiCriterion) o;
        if (criterions == null) {
            if (other.criterions != null) {
                return false;
            }
        } else if (!criterions.equals(other.criterions)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return this.getClass().getName();
    }

}