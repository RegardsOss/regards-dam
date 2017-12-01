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

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.modules.datasources.domain.DataSource;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourceFromSingleTablePlugin;

// And the winner of "BEST COMMENT ON CLASS AWARD" is .....
// ...Christopher Mertz !!!
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
     * {@link IDBDataSourceFromSingleTablePlugin}.
     *
     * @return all the {@link DataSource}.
     */
    List<DataSource> getAllDataSources();

    /**
     *
     * Create a {@link PluginConfiguration} for the plugin types {@link IDBDataSourceFromSingleTablePlugin}.</br>
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
}