/*
 *
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
 *
 */
package fr.cnes.regards.modules.dam.service.datasources;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.amqp.IPublisher;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.EntityNotFoundException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.dam.domain.datasources.event.DatasourceEvent;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDataSourcePlugin;

/**
 * DataSource specific plugin service façade implementation
 * @author Sylvain Vissiere-Guerinet
 * @author Christophe Mertz
 * @author oroussel
 */
@Service
@MultitenantTransactional
public class DataSourceService implements IDataSourceService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceService.class);

    @Autowired
    private IPluginService service;

    @Autowired
    private IPublisher publisher;

    @Override
    public List<PluginConfiguration> getAllDataSources() {
        return service.getPluginConfigurationsByType(IDataSourcePlugin.class);
    }

    @Override
    public PluginConfiguration createDataSource(PluginConfiguration dataSource) throws ModuleException {
        return service.savePluginConfiguration(dataSource);
    }

    @Override
    public PluginConfiguration getDataSource(String businessId) throws EntityNotFoundException {
        return service.getPluginConfiguration(businessId);
    }

    @Override
    public PluginConfiguration updateDataSource(PluginConfiguration dataSource) throws ModuleException {
        LOGGER.info("updateDataSource : id = {}, [new] label = {}", dataSource.getId(), dataSource.getLabel());
        return service.updatePluginConfiguration(dataSource);
    }

    @Override
    public void deleteDataSource(String businessId) throws ModuleException {
        LOGGER.info("deleting DataSource {}", businessId);
        PluginConfiguration dataSource = service.getPluginConfiguration(businessId);
        service.deletePluginConfiguration(businessId);
        publisher.publish(DatasourceEvent.buildDeleted(dataSource.getId()));
    }

}
