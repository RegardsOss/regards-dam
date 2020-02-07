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

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.annotations.Plugin;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParamDescriptor;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.PluginParamType;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DBConnectionPluginConstants;
import fr.cnes.regards.modules.dam.plugins.datasources.DefaultPostgreConnectionPlugin;

/**
 * Unit testing of {@link DBConnectionService}.
 * @author Christophe Mertz
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=connection_plugin_it" },
        locations = { "classpath:dataaccess.properties" })
public class DBConnectionServiceIT extends AbstractMultitenantServiceTest {

    /**
     * The JDBC PostgreSQL driver
     */
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";

    /**
     *
     */
    @Autowired
    private IPluginService pluginService;

    /**
     * A mock of {@link IDBConnectionService}
     */
    @Autowired
    private IDBConnectionService dbConnectionService;

    @Autowired
    private IPluginConfigurationRepository pluginConfigurationRepository;

    @After
    public void cleanUp() {
        runtimeTenantResolver.forceTenant(getDefaultTenant());
        pluginConfigurationRepository.deleteAll();
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_080")
    @Purpose("Test the creation of a connection by setting the connection's parameters including the pool size")
    public void createConnection() throws ModuleException {
        PluginConfiguration dbConnection = new PluginConfiguration();
        dbConnection.setParameters(initializePluginParameters());
        dbConnection.setLabel("the label of the new connection");
        dbConnection.setPluginId(DefaultPostgreConnectionPlugin.class.getAnnotation(Plugin.class).id());
        runtimeTenantResolver.forceTenant(getDefaultTenant());
        dbConnectionService.createDBConnection(dbConnection);
    }

    @Test(expected = EntityInvalidException.class)
    public void createConnectionUnknownPluginId() throws ModuleException {
        PluginConfiguration dbConnection = new PluginConfiguration();
        dbConnection.setParameters(initializePluginParameters());
        dbConnection.setLabel("the label of the new connection failed");
        dbConnection.setPluginId("unknown plugin id");
        runtimeTenantResolver.forceTenant(getDefaultTenant());
        dbConnectionService.createDBConnection(dbConnection);
    }

    private PluginMetaData initializePluginMetaDataPostGre(String pluginId) {
        final PluginMetaData pluginMetaData = new PluginMetaData();
        pluginMetaData
                .setPluginClassName("fr.cnes.regards.modules.dam.domain.datasources.plugins.DefaultPostgreConnectionPlugin");
        pluginMetaData.setPluginId(pluginId);
        pluginMetaData.setAuthor("CS-SI");
        pluginMetaData.setVersion("1.1");
        pluginMetaData.setParameters(initializePluginParameterType());
        return pluginMetaData;
    }

    private Set<IPluginParam> initializePluginParameters() {
        return IPluginParam.set(IPluginParam.build(DBConnectionPluginConstants.USER_PARAM, "user"),
                                IPluginParam.build(DBConnectionPluginConstants.PASSWORD_PARAM, "password"),
                                IPluginParam.build(DBConnectionPluginConstants.DB_HOST_PARAM, "host"),
                                IPluginParam.build(DBConnectionPluginConstants.DB_PORT_PARAM, "666"),
                                IPluginParam.build(DBConnectionPluginConstants.DB_NAME_PARAM, "name"),
                                IPluginParam.build(DBConnectionPluginConstants.DRIVER_PARAM, POSTGRESQL_JDBC_DRIVER));
    }

    private List<PluginParamDescriptor> initializePluginParameterType() {
        return Arrays.asList(
                             PluginParamDescriptor.create("model", "model", null, PluginParamType.STRING, false, false,
                                                          false, null),
                             PluginParamDescriptor.create("connection", "connection", null, PluginParamType.PLUGIN,
                                                          false, false, false, null));
    }

}
