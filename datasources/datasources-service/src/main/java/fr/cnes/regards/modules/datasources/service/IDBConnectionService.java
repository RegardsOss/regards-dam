/*
 * LICENSE_PLACEHOLDER
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
 *
 * This class allows to the {@link PluginConfiguration} associated to the plugintype {@link IDBConnectionPlugin}.</br>
 * This type of plugin used a database connection pools.
 * 
 * 
 * @author Christophe Mertz
 */
public interface IDBConnectionService {

    /**
     *
     * Get all the {@link PluginConfiguration} for the plugin types {@link IDBConnectionPlugin}.
     *
     * @return all the {@link PluginConfiguration}.
     */
    List<PluginConfiguration> getAllDBConnections();

    /**
     *
     * Create a {@link PluginConfiguration} for the plugin types {@link IDBConnectionPlugin}.
     *
     * @param pDbConnection
     *            the {@link DBConnection} to the database
     * @return the new {@link PluginConfiguration}
     * @throws ModuleException
     *             throw if an error occurs
     */
    PluginConfiguration createDBConnection(DBConnection pDbConnection) throws ModuleException;

    /**
     *
     * Get the {@link PluginConfiguration}.
     *
     * @param pConfigurationId
     *            a {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws ModuleException
     *             throw if an error occurs
     */
    PluginConfiguration getDBConnection(Long pConfigurationId) throws ModuleException;

    /**
     *
     * Update the {@link PluginConfiguration} linked to the {@link DBConnection}
     *
     * @param pDbConnection
     *            the {@link DBConnection} to update
     * @return the updated {@link PluginConfiguration}
     * @throws ModuleException
     *             throw if an error occurs
     */
    PluginConfiguration updateDBConnection(DBConnection pDbConnection) throws ModuleException;

    /**
     * Delete a {@link DBConnection}.
     * 
     * @param pConfigurationId
     *            a {@link PluginConfiguration} identifier
     * @throws ModuleException
     *             throw if an error occurs
     */
    void deleteDBConnection(Long pConfigurationId) throws ModuleException;

    /**
     * Querying the status of a database connection pools.
     * 
     * @param pConfigurationId
     *            a {@link PluginConfiguration} identifier
     * @return true success to the connection to the database.</br>
     *         false unable to connect to the database
     * @throws ModuleException
     *             throw if an error occurs
     */
    Boolean testDBConnection(Long pConfigurationId) throws ModuleException;

    /**
     * 
     * @param pId
     * @return
     * @throws ModuleException
     */
    Map<String, Table> getTables(Long pId) throws ModuleException;

    /**
     * 
     * @param pId
     * @param pTableName
     * @return
     * @throws ModuleException
     */
    Map<String, Column> getColumns(Long pId, String pTableName) throws ModuleException;

}