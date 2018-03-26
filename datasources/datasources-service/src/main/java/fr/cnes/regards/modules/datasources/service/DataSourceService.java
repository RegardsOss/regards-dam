/*
 *
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
 *
 */
package fr.cnes.regards.modules.datasources.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.datasources.domain.plugins.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.domain.plugins.IDataSourcePlugin;

/**
 * DataSource specific plugin service façade implementation
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class DataSourceService implements IDataSourceService, ApplicationListener<ApplicationReadyEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceService.class);

    @Autowired
    private IPluginService service;

    @Override
    @MultitenantTransactional(propagation = Propagation.SUPPORTS)
    public void onApplicationEvent(ApplicationReadyEvent event) {
        this.service.addPluginPackage("fr.cnes.regards.modules.datasources");
    }

    @Override
    public List<PluginConfiguration> getAllDataSources() {
        return service.getPluginConfigurationsByType(IDataSourcePlugin.class);
    }

    @Override
    public PluginConfiguration createDataSource(PluginConfiguration dataSource) throws ModuleException {
        return service.savePluginConfiguration(dataSource);
    }

    @Override
    public PluginConfiguration getDataSource(Long id) throws EntityNotFoundException {
        return service.getPluginConfiguration(id);
    }

    @Override
    public PluginConfiguration updateDataSource(PluginConfiguration dataSource) throws ModuleException {
        LOGGER.info("updateDataSource : id = {}, [new] label = {}", dataSource.getId(), dataSource.getLabel());

        // Get current datasource PluginConfiguration
        PluginConfiguration dataSourceFromDb = service.getPluginConfiguration(dataSource.getId());

        // Manage the label change
        dataSourceFromDb.setLabel(dataSource.getLabel());

        // Manage the activation change
        dataSourceFromDb.setIsActive(dataSource.isActive());

        // Update all PluginParameters
        dataSourceFromDb.getParameters().replaceAll(param -> mergeParameter(param, dataSource));
        return service.updatePluginConfiguration(dataSourceFromDb);
    }

    @Override
    public void deleteDataSource(Long id) throws ModuleException {
        LOGGER.info("deleting DataSource {}", id);
        service.deletePluginConfiguration(id);
    }

    /**
     * Update the {@link PluginParameter} with the appropriate {@link PluginConfiguration} datasource attribute
     * @param pluginParam a {@link PluginParameter} to update
     * @param dataSource a data source
     * @return a {{@link PluginParameter}
     */
    private PluginParameter mergeParameter(PluginParameter pluginParam, PluginConfiguration dataSource) {
        if (pluginParam.getName().equals(IDBDataSourcePlugin.CONNECTION_PARAM)) {
            mergePluginConfigurationParameter(pluginParam, dataSource);
        } else {
            // BEWARE : DataSource comes from frontend, its value is already gson-normalized SO don't use
            // PluginParametersFactory.updateParameter(...) method
            pluginParam.setValue(dataSource.getParameterValue(pluginParam.getName()));
        }
        return pluginParam;
    }

    /**
     * Update a {@link PluginParameter} of type connection
     * @param connectionPluginParam a {@link PluginParameter} to update
     * @param dataSource a {@link PluginConfiguration}
     */
    private void mergePluginConfigurationParameter(PluginParameter connectionPluginParam,
            PluginConfiguration dataSource) {
        PluginConfiguration dbConf = connectionPluginParam.getPluginConfiguration();
        PluginConfiguration currentDbConf = dataSource.getParameterConfiguration(IDBDataSourcePlugin.CONNECTION_PARAM);
        if ((dbConf == null) || !dbConf.getId().equals(currentDbConf.getId())) {
            connectionPluginParam.setPluginConfiguration(currentDbConf);
        }
    }

}
