/*
 * Copyright 2017-2020 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

/**
 * Defines a NOT criterion
 * @author oroussel
 */
public class NotCriterion implements ICriterion {

    /**
     * Criterion that not be true
     */
    private ICriterion criterion;

    public NotCriterion(ICriterion criterion) {
        this.criterion = criterion;
    }

    public ICriterion getCriterion() {
        return criterion;
    }

    public void setCriterion(ICriterion criterion) {
        this.criterion = criterion;
    }

    @Override
    public NotCriterion copy() {
        return new NotCriterion(this.criterion.copy());
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitNotCriterion(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((criterion == null) ? 0 : criterion.hashCode());
        result = (prime * result) + "NOT".hashCode();
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        NotCriterion other = (NotCriterion) obj;
        if (criterion == null) {
            if (other.criterion != null) {
                return false;
            }
        } else if (!criterion.equals(other.criterion)) {
            return false;
        }
        return true;
    }

}
