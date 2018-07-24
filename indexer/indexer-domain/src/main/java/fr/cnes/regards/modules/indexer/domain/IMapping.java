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
package fr.cnes.regards.modules.indexer.domain;

/**
 * Mapping specific constants
 */
public interface IMapping {

    /**
     * Mapping name for range lower bound attribute
     */
    String RANGE_LOWER_BOUND = "lowerBound";

    /**
     * Mapping name for range upper bound attribute
     */
    String RANGE_UPPER_BOUND = "upperBound";

    /**
     * Mapping name for name
     */
    String NAME = "name";

    /**
     * Mapping value for value
     */
    String VALUE = "value";

    /**
     * Mapping name for geometry attribute, be careful, internally, only AbstractEntity.wgs84 attribute is used as
     * a geo_shape mapping attribute
     */
    String GEO_SHAPE_ATTRIBUTE = "wgs84";
}
