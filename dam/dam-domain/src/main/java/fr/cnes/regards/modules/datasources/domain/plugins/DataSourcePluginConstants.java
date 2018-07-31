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

package fr.cnes.regards.modules.datasources.domain.plugins;

import fr.cnes.regards.modules.entities.domain.StaticProperties;

public final class DataSourcePluginConstants {

    /**
     * Model mapping parameter name
     * <B>Beware : false friend parameter name, it corresponds to Json model mapping object</B>
     */
    public static final String MODEL_MAPPING_PARAM = "mapping";

    /**
     * From clause to apply to the SQL request parameter name
     */
    public static final String FROM_CLAUSE = "fromClause";

    /**
     * Connection parameter name
     */
    public static final String CONNECTION_PARAM = "connection";

    /**
     * Ingestion refresh rate parameter name
     */
    public static final String REFRESH_RATE = "refreshRate";

    /**
     * Ingestion static tags parameter name
     */
    public static final String TAGS = "tags";

    /**
     * Ingestion refresh rate default value in seconds
     */
    public static final Integer REFRESH_RATE_DEFAULT_VALUE = 86400;

    public static final String MODEL_NAME_PARAM = "modelName";

    public static final String DOT = ".";

    /**
    * Prefix for all model properties
    */
    public static final String PROPERTY_PREFIX = StaticProperties.FEATURE_PROPERTIES + DOT;

    /**
     * Interval mapping convention suffix
     */
    public static final String LOWER_BOUND = "lowerBound";

    public static final String UPPER_BOUND = "upperBound";

    public static final String LOWER_BOUND_SUFFIX = DOT + LOWER_BOUND;

    public static final String UPPER_BOUND_SUFFIX = DOT + UPPER_BOUND;

    public static final String BINDING_MAP = "binding map";

    public static final String MODEL_ATTR_FILE_SIZE = "attribute file size";

    public static final String SUBSETTING_TAGS = "subsettingTags";

    /**
     * The table parameter name
     */
    public static final String TABLE_PARAM = "table";
}
