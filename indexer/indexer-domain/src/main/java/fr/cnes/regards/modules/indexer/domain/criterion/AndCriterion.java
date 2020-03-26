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

import java.util.stream.Collectors;

import com.google.common.collect.Lists;

/**
 * Defines a list of mandatory criterions (logicaly AND)
 * @author oroussel
 */
public final class AndCriterion extends AbstractMultiCriterion implements ICriterion {

    AndCriterion(ICriterion... criteria) {
        this.criterions = Lists.newArrayList(criteria);
    }

    AndCriterion(Iterable<ICriterion> criteria) {
        this.criterions = Lists.newArrayList(criteria);
    }

    @Override
    public AndCriterion copy() {
        return new AndCriterion(this.criterions.stream().map(ICriterion::copy).collect(Collectors.toList()));
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitAndCriterion(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + "AND".hashCode();
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
        return super.equals(o);
    }
}
