/*
 * LICENSE_PLACEHOLDER
 */

package fr.cnes.regards.modules.datasources.service;

import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourceFromSingleTablePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;

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
     * Get all the {@link DataSource}.</br>
     * The {@link DataSource} is converts from a {@link PluginConfiguration} for the plugin types
     * {@link IDataSourceFromSingleTablePlugin}.
     *
     * @return all the {@link DataSource}.
     */
    List<DataSource> getAllDataSources();

    /**
     *
     * Create a {@link PluginConfiguration} for the plugin types {@link IDataSourceFromSingleTablePlugin}.</br>
     *
     * @param pDataSource
     *            the {@link DataSource} to the database
     * @return the new {@link PluginConfiguration}
     * @throws ModuleException
     *             throw if an error occurs
     */
    DataSource createDataSource(DataSource pDataSource) throws ModuleException;

    /**
     * Get the {@link DataSource}.
     *
     * @param pId
     *            a {@link PluginConfiguration} identifier
     * @return a {@link PluginConfiguration}
     * @throws EntityNotFoundException
     *             throw if an error occurs
     */
    DataSource getDataSource(Long pId) throws EntityNotFoundException;

    /**
     * Update the {@link PluginConfiguration} linked to the {@link DataSource}
     *
     * @param pDataSource
     *            the {@link DataSource} to update
     * @return the updated {@link DataSource}
     * @throws ModuleException
     *             throw if an error occurs
     */
    DataSource updateDataSource(DataSource pDataSource) throws ModuleException;

    /**
     * Delete a {@link DataSource}.
     * 
     * @param pId
     *            a {@link DataSource} identifier
     * @throws ModuleException
     *             throw if an error occurs
     */
    void deleteDataSouce(Long pId) throws ModuleException;

    /**
     * Get the {@link PluginConfiguration} of the plugin's type {@link IDataSourcePlugin} that is marked as the internal
     * REGARDS data source
     * 
     * @return the {@link PluginConfiguration} of the plugin that is marked as the internal REGARDS data source
     */
    PluginConfiguration getInternalDataSource();

    /**
     * Set a {@link PluginConfiguration} as the plugin that is marked as the internal REGARDS data source
     * 
     * @param pPluginConfiguration
     *            the {@link PluginConfiguration} to set
     * @return the {@link PluginConfiguration} that is marked as the internal REGARDS data source
     * @throws ModuleException
     *             throw if an error occurs
     */
    PluginConfiguration setInternalDataSource(PluginConfiguration pPluginConfiguration) throws ModuleException;

}