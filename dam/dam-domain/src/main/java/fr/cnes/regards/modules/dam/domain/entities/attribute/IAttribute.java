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
package fr.cnes.regards.modules.dam.domain.entities.attribute;

/**
 * @param <T> type of Attribute
 * @author lmieulet
 * @author Marc Sordi
 */
public interface IAttribute<T> extends Comparable<IAttribute<T>> {

    /**
     * Get attribute name
     * @return attribute name
     */
    String getName();

    /**
     * @return the attribute value
     */
    T getValue();

    @Override
    default int compareTo(IAttribute<T> o) {
        // name is not null (mandatory)
        return this.getName().compareToIgnoreCase(o.getName());
    }
}
