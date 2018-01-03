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

package fr.cnes.regards.modules.datasources.service;

import java.util.List;
import java.util.Map;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.DBConnection;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBConnectionPlugin;

/**
 * This class allows to the {@link PluginConfiguration} associated to the plugintype {@link IDBConnectionPlugin}.</br>
 * This type of plugin used a database connection pools.
 * @author Christophe Mertz
 */
public interface IDBConnectionService {

    /**
     * Get all the {@link PluginConfiguration} for the plugin types {@link IDBConnectionPlugin}.
     * @return all the {@link PluginConfiguration}.
     */
    List<PluginConfiguration> getAllDBConnections();

    /**
     * Create a {@link PluginConfiguration} for the plugin types {@link IDBConnectionPlugin}.
     * @param dbConnection the {@link DBConnection} to the database
     * @return the new {@link PluginConfiguration}
     * @throws ModuleException throw if an error occurs
     */
    PluginConfiguration createDBConnection(PluginConfiguration dbConnection) throws ModuleException;

    /**
     * Get the {@link PluginConfiguration}.
     * @param configurationId a {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException throw if an error occurs
     */
    PluginConfiguration getDBConnection(Long configurationId) throws ModuleException;

    /**
     * Update the {@link PluginConfiguration} linked to the {@link DBConnection}
     * @param dbConnection the {@link DBConnection} to update
     * @return the updated {@link PluginConfiguration}
     * @throws ModuleException throw if an error occurs
     */
    PluginConfiguration updateDBConnection(PluginConfiguration dbConnection) throws ModuleException;

    /**
     * Delete a {@link DBConnection}.
     * @param configurationId a {@link PluginConfiguration} identifier
     * @throws ModuleException throw if an error occurs
     */
    void deleteDBConnection(Long configurationId) throws ModuleException;

    /**
     * Querying the status of a database connection pools.
     * @param configurationId a {@link PluginConfiguration} identifier
     * @return true success to the connection to the database.</br>
     * false unable to connect to the database
     * @throws ModuleException throw if an error occurs
     */
    Boolean testDBConnection(Long configurationId) throws ModuleException;

    /**
     *
     * @param id
     * @return
     * @throws ModuleException
     */
    Map<String, Table> getTables(Long id) throws ModuleException;

    /**
     *
     * @param id
     * @param tableName
     * @return
     * @throws ModuleException
     */
    Map<String, Column> getColumns(Long id, String tableName) throws ModuleException;

}