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
package fr.cnes.regards.modules.dam.domain.datasources.plugins;

import java.sql.SQLException;

import fr.cnes.regards.framework.modules.plugins.annotations.PluginInterface;

/**
 * Database specialization of data source plugin
 * @author oroussel
 */
@PluginInterface(description = "Plugin to search in a database data source")
public interface IDBDataSourcePlugin extends IDataSourcePlugin {

    /**
     * Retrieve DB connection plugin used by the datasource plugin
     * @throws SQLException {@link java.sql.Connection} is not available
     */
    IDBConnectionPlugin getDBConnection() throws SQLException;

}
