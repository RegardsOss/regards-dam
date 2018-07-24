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
package fr.cnes.regards.modules.indexer.domain.criterion;

/**
 * A match criterion specifies how a value have to be matched
 * @param <T> type of value
 * @author oroussel
 */
public abstract class AbstractMatchCriterion<T> extends AbstractPropertyCriterion implements ICriterion {

    /**
     * Matching type
     */
    protected MatchType type;

    /**
     * Value to be matched
     */
    protected T value;

    private AbstractMatchCriterion(String name) {
        super(name);
    }

    public AbstractMatchCriterion(String name, MatchType type, T value) {
        this(name);
        this.type = type;
        this.value = value;
    }

    public MatchType getType() {
        return type;
    }

    public void setType(MatchType type) {
        this.type = type;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = (prime * result) + ((type == null) ? 0 : type.hashCode());
        result = (prime * result) + ((value == null) ? 0 : value.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!super.equals(o)) {
            return false;
        }
        if (getClass() != o.getClass()) {
            return false;
        }
        AbstractMatchCriterion<?> other = (AbstractMatchCriterion<?>) o;
        if (type != other.type) {
            return false;
        }
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        return true;
    }

}
