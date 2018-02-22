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
package fr.cnes.regards.modules.datasources.plugins.interfaces;

import java.sql.SQLException;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 * Database specialization of data source plugin
 * @author oroussel
 */
@PluginInterface(description = "Plugin to search in a database data source")
public interface IDBDataSourcePlugin extends IDataSourcePlugin {

    /**
     * Model mapping parameter name
     * <B>Beware : false friend parameter name, it corresponds to Json model mapping object</B>
     */
    String MODEL_MAPPING_PARAM = "mapping";

    /**
     * From clause to apply to the SQL request parameter name
     */
    String FROM_CLAUSE = "fromClause";

    /**
     * Connection parameter name
     */
    String CONNECTION_PARAM = "connection";

    /**
     * Retrieve DB connection plugin used by the datasource plugin
     * @throws SQLException {@link java.sql.Connection} is not available
     */
    IDBConnectionPlugin getDBConnection() throws SQLException;

}
