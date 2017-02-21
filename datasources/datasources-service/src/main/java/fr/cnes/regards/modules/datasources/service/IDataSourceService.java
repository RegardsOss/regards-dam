/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.service;

import java.util.List;
import java.util.Map;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.datasources.domain.Column;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.domain.Index;
import fr.cnes.regards.modules.datasources.domain.Table;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;

/**
 *
 * 
 * 
 * 
 * @author Christophe Mertz
 */
public interface IDataSourceService {

    /**
     *
     * Get all the {@link PluginConfiguration} for the plugin types {@link IDataSourceFromSingleTablePlugin}.
     *
     * @return all the {@link PluginConfiguration}.
     */
    List<PluginConfiguration> getAllDataSources();

    /**
     *
     * Create a {@link PluginConfiguration} for the plugin types {@link IDataSourceFromSingleTablePlugin}.
     *
     * @param pDataSource
     *            the {@link DataSource} to the database
     * @return the new {@link PluginConfiguration}
     * @throws ModuleException
     *             throw if an error occurs
     */
    PluginConfiguration createDataSource(DataSource pDataSource) throws ModuleException;

    /**
     * Get the {@link PluginConfiguration}.
     *
     * @param pId
     *            a {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws EntityNotFoundException
     *             throw if an error occurs
     */
    PluginConfiguration getDataSource(Long pId) throws EntityNotFoundException;

    /**
     * Update the {@link PluginConfiguration} linked to the {@link DataSource}
     *
     * @param pDataSource
     *            the {@link DataSource} to update
     * @return the updated {@link PluginConfiguration}
     * @throws ModuleException
     *             throw if an error occurs
     */
    PluginConfiguration updateDataSource(DataSource pDataSource) throws ModuleException;

    /**
     * Delete a {@link DataSource}.
     * 
     * @param pId
     *            a {@link DataSource} identifier
     * @throws ModuleException
     *             throw if an error occurs
     */
    void deleteDataSouce(Long pId) throws ModuleException;

    Map<String, Table> getTables(Long pId) throws ModuleException;

    Map<String, Column> getColumns(Long pId, Table pTable) throws ModuleException;

    Map<String, Index> getIndexes(Long pId, Table pTable) throws ModuleException;

}