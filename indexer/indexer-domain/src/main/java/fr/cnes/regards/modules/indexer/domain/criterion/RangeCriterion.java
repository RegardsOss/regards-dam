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

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A range criterion defines a range of value comparisons for a named property.<br/>
 * For example : property "toto" between 0 and 1 (ie toto range = > 0 and < 1).<br/>
 * This class is also to be used for only one comparison.
 * @param <T> value type
 * @author oroussel
 */
public class RangeCriterion<T extends Comparable<? super T>> extends AbstractPropertyCriterion implements ICriterion {

    /**
     * Set of comparisons (att > 0, att <= 25.34, etc...)
     */
    protected final Set<ValueComparison<T>> valueComparisons = new HashSet<>();

    protected RangeCriterion(String name) {
        super(name);
    }

    public void addValueComparison(ValueComparison<T> valueComparison) {
        valueComparisons.add(valueComparison);
    }

    public Set<ValueComparison<T>> getValueComparisons() {
        return valueComparisons;
    }

    @Override
    public RangeCriterion<T> copy() {
        RangeCriterion<T> copy = new RangeCriterion<>(super.name);
        copy.valueComparisons
                .addAll(this.valueComparisons.stream().map(ValueComparison::copy).collect(Collectors.toSet()));
        return copy;
    }

    @Override
    public <U> U accept(ICriterionVisitor<U> visitor) {
        return visitor.visitRangeCriterion(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass())) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }
        RangeCriterion<?> that = (RangeCriterion<?>) o;
        return Objects.equals(valueComparisons, that.valueComparisons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), valueComparisons);
    }
}
