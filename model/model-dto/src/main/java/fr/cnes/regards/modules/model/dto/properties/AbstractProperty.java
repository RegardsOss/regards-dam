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
package fr.cnes.regards.modules.model.dto.properties;

import javax.validation.constraints.NotNull;

/**
 * @param <T> attribute type
 * @author Marc Sordi
 */
public abstract class AbstractProperty<T> implements IProperty<T> {

    /**
     * Attribute name
     */
    @NotNull(message = "Property name is required")
    private String name;

    /**
     * Attribute value
     */
    private T value;

    @Override
    public T getValue() {
        return value;
    }

    public void setValue(T pValue) {
        value = pValue;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String pName) {
        name = pName;
    }

    @Override
    public String toString() {
        String display = value == null ? "null" : value.toString();
        return name + " : " + display;
    }

    public abstract boolean represents(PropertyType pAttributeType);

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (name == null ? 0 : name.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object pObj) {
        if (this == pObj) {
            return true;
        }
        if (pObj == null) {
            return false;
        }
        if (getClass() != pObj.getClass()) {
            return false;
        }
        AbstractProperty<?> other = (AbstractProperty<?>) pObj;
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        return true;
    }

}
