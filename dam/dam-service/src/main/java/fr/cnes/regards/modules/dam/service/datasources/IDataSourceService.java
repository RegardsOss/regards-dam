/*
 * Copyright 2017-2019 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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

package fr.cnes.regards.modules.dam.service.datasources;

import java.util.List;

import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;

/**
 * DataSource façade of PluginService ie a service specific to PluginConfigurations of type IDataSourcePlugin
 * @author Christophe Mertz
 * @author oroussel
 */
public interface IDataSourceService {

    /**
     * Get all {@link PluginConfiguration} datasources.</br>
     * @return all PluginConfigurations of type IDataSourcePlugin.
     */
    List<PluginConfiguration> getAllDataSources();

    /**
     * Create a datasource {@link PluginConfiguration}.</br>
     * @param dataSource datasource {@link PluginConfiguration} to create
     * @return created {@link PluginConfiguration}
     * @throws ModuleException
     */
    PluginConfiguration createDataSource(PluginConfiguration dataSource) throws ModuleException;

    /**
     * Get datasource {@link PluginConfiguration}.
     * @param businessId a {@link PluginConfiguration} business identifier
     * @return {@link PluginConfiguration}
     * @throws EntityNotFoundException if entity does not exist
     */
    PluginConfiguration getDataSource(String businessId) throws EntityNotFoundException;

    /**
     * Update datasource {@link PluginConfiguration}
     * @param dataSource {@link PluginConfiguration} to update
     * @return updated {@link PluginConfiguration}
     * @throws ModuleException
     */
    PluginConfiguration updateDataSource(PluginConfiguration dataSource) throws ModuleException;

    /**
     * Delete datasource {@link PluginConfiguration}.
     * @param id {@link PluginConfiguration} business id
     * @throws ModuleException
     */
    void deleteDataSource(String businessId) throws ModuleException;
}