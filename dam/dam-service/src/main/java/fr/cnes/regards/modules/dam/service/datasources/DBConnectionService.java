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
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.modules.plugins.service.PluginService;
import fr.cnes.regards.framework.utils.plugins.exception.NotAvailablePluginConfigurationException;
import fr.cnes.regards.modules.dam.domain.datasources.Column;
import fr.cnes.regards.modules.dam.domain.datasources.Table;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBConnectionPlugin;

/**
 * @author Christophe Mertz
 */
@Service
@MultitenantTransactional
public class DBConnectionService implements IDBConnectionService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBConnectionService.class);

    /**
     * Attribute plugin service
     */
    private final IPluginService pluginService;

    /**
     * The constructor with an instance of the {@link PluginService}
     * @param pPluginService The {@link PluginService} to used by this service
     */
    public DBConnectionService(IPluginService pPluginService) {
        super();
        this.pluginService = pPluginService;
    }

    @Override
    public List<PluginConfiguration> getAllDBConnections() {
        return pluginService.getPluginConfigurationsByType(IDBConnectionPlugin.class);
    }

    @Override
    public PluginConfiguration createDBConnection(PluginConfiguration dbConnection) throws ModuleException {
        return pluginService.savePluginConfiguration(dbConnection);
    }

    @Override
    public PluginConfiguration getDBConnection(String connConfbusinessId) throws ModuleException {
        return pluginService.getPluginConfiguration(connConfbusinessId);
    }

    @Override
    public PluginConfiguration updateDBConnection(PluginConfiguration dbConnection) throws ModuleException {
        return pluginService.updatePluginConfiguration(dbConnection);
    }

    @Override
    public void deleteDBConnection(String businessId) throws ModuleException {
        pluginService.deletePluginConfiguration(businessId);
    }

    @Override
    public Boolean testDBConnection(String businessId) throws ModuleException {

        Boolean result = false;
        IDBConnectionPlugin plg;
        try {
            // Instanciate plugin
            plg = pluginService.getPlugin(businessId);
            // Test connection
            result = plg.testConnection();
            // Remove plugin instance from cache after closing connection
            if (!result) {
                pluginService.cleanPluginCache(businessId);
            }
        } catch (NotAvailablePluginConfigurationException e) {
            LOGGER.error(e.getMessage(), e);
        }

        return result;
    }

    @Override
    public Map<String, Table> getTables(String businessId)
            throws ModuleException, NotAvailablePluginConfigurationException {
        IDBConnectionPlugin plg = pluginService.getPlugin(businessId);
        return plg == null ? null : plg.getTables(null, null);
    }

    @Override
    public Map<String, Column> getColumns(String businessId, String tableName)
            throws ModuleException, NotAvailablePluginConfigurationException {
        IDBConnectionPlugin plg = pluginService.getPlugin(businessId);
        return plg == null ? null : plg.getColumns(tableName);
    }

}
