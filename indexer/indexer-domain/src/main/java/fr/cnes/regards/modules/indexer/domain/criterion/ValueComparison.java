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

import com.google.common.base.Preconditions;

/**
 * Pair of comparison operator and value
 * @param <T> value type
 * @author oroussel
 */
public class ValueComparison<T extends Comparable<? super T>> implements Comparable<ValueComparison<T>> {

    /**
     * Operator
     */
    private ComparisonOperator operator;

    /**
     * Value to compare
     */
    private T value;

    public ValueComparison(ComparisonOperator operator, T value) {
        super();
        this.operator = operator;
        this.value = value;
    }

    public ComparisonOperator getOperator() {
        return operator;
    }

    public void setOperator(ComparisonOperator operator) {
        this.operator = operator;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public ValueComparison<T> copy() {
        return new ValueComparison<>(this.operator, this.value);
    }

    // hashCode() and equals() use only operator because ValueComparison is used
    // into a set and must not be encountered twice with same operator
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = (prime * result) + ((operator == null) ? 0 : operator.hashCode());
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
        ValueComparison<?> other = (ValueComparison<?>) obj;
        return (operator == other.operator);
    }

    @Override
    public String toString() {
        return "ValueComparison [operator=" + operator + ", value=" + value + "]";
    }

    @Override
    public int compareTo(ValueComparison<T> other) {
        Preconditions.checkNotNull(other);
        return value.compareTo(other.value);
    }

}
