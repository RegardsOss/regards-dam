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

import java.util.Set;

/**
 * A match criterion specifies how a value has to be matched and on which properties
 * @param <T> type of value
 * @author Marc SORDI
 *
 */
public abstract class AbstractMultiMatchCriterion<T> implements ICriterion {

    /**
     * Concerned property names
     */
    protected Set<String> names;

    /**
     * Matching type
     */
    protected MatchType type;

    /**
     * Value to be matched
     */
    protected T value;

    public AbstractMultiMatchCriterion(Set<String> names, MatchType type, T value) {
        this.names = names;
        this.type = type;
        this.value = value;
    }

    public Set<String> getNames() {
        return names;
    }

    public void setNames(Set<String> names) {
        this.names = names;
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

}
