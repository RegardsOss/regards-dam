/*
 * Copyright 2017 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.models.domain.attributes;

/**
 * List of available attribute types
 *
 * @author msordi
 */
public enum AttributeType {

    /**
     * Possible attribute type
     */
    STRING, INTEGER, DOUBLE, DATE_ISO8601, URL, BOOLEAN, STRING_ARRAY, INTEGER_ARRAY, DOUBLE_ARRAY, DATE_ARRAY, //
    INTEGER_INTERVAL, DOUBLE_INTERVAL, DATE_INTERVAL, LONG, LONG_INTERVAL, LONG_ARRAY
}
