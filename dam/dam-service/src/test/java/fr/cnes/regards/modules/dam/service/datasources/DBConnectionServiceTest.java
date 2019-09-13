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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;

import fr.cnes.regards.framework.module.rest.exception.EntityInvalidException;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParamDescriptor;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.IPluginParam;
import fr.cnes.regards.framework.modules.plugins.domain.parameter.PluginParamType;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.DBConnectionPluginConstants;
import fr.cnes.regards.modules.dam.domain.datasources.plugins.IDBConnectionPlugin;

/**
 * Unit testing of {@link DBConnectionService}.
 * @author Christophe Mertz
 */
public class DBConnectionServiceTest {

    /**
     * The JDBC PostgreSQL driver
     */
    private static final String POSTGRESQL_JDBC_DRIVER = "org.postgresql.Driver";

    @Value("${postgresql.datasource.host}")
    private String dbHost;

    @Value("${postgresql.datasource.port}")
    private String dbPort;

    @Value("${postgresql.datasource.name}")
    private String dbName;

    @Value("${postgresql.datasource.username}")
    private String dbUser;

    @Value("${postgresql.datasource.password}")
    private String dbPassword;

    /**
     *
     */
    private IPluginService pluginServiceMock;

    /**
     * A mock of {@link IDBConnectionService}
     */
    private IDBConnectionService dbConnectionServiceMock;

    /**
     * A {@link List} of {@link PluginConfiguration}
     */
    private final List<PluginConfiguration> plgConfs = new ArrayList<>();

    /**
     * This method is run before all tests
     */
    @Before
    public void init() {
        // create mock services
        pluginServiceMock = Mockito.mock(IPluginService.class);
        dbConnectionServiceMock = new DBConnectionService(pluginServiceMock);

        // create PluginConfiguration
        Set<IPluginParam> parameters = initializePluginParameters();
        plgConfs.add(new PluginConfiguration(initializePluginMetaDataPostGre("plugin-id-2"), "first configuration",
                parameters));
        plgConfs.add(new PluginConfiguration(initializePluginMetaDataPostGre("plugin-id-2"), "second configuration",
                parameters, 5));
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_060")
    @Purpose("The system allows to list all the connections")
    public void getAllDBConnection() {
        Mockito.when(pluginServiceMock.getPluginConfigurationsByType(IDBConnectionPlugin.class)).thenReturn(plgConfs);
        List<PluginConfiguration> connections = dbConnectionServiceMock.getAllDBConnections();
        Assert.assertNotNull(connections);
        Assert.assertEquals(plgConfs.size(), connections.size());
    }

    @Test
    @Requirement("REGARDS_DSL_DAM_SRC_080")
    @Purpose("Test the creation of a connection by setting the connection's parameters including the pool size")
    public void createConnection() throws ModuleException {
        PluginConfiguration dbConnection = new PluginConfiguration();
        String className = "fr.cnes.regards.modules.dam.plugins.datasources.DefaultPostgreConnectionPlugin";
        PluginMetaData metaData = new PluginMetaData();
        metaData.setPluginClassName(className);
        metaData.setPluginId("test");
        dbConnection.setMetaData(metaData);
        dbConnection.setParameters(initializePluginParameters());
        dbConnection.setLabel("the label of the new connection");
        Mockito.when(pluginServiceMock.checkPluginClassName(IDBConnectionPlugin.class, className))
                .thenReturn(initializePluginMetaDataPostGre("plugin-id-2"));
        dbConnectionServiceMock.createDBConnection(dbConnection);
        Assert.assertTrue(true);
    }

    @Test(expected = EntityInvalidException.class)
    public void createConnectionUnknownPluginClassName() throws ModuleException {
        PluginConfiguration dbConnection = new PluginConfiguration();
        String className = "fr.cnes.regards.modules.dam.plugins.datasources.DefaultPostgreConnectionPlugin";
        dbConnection.setParameters(initializePluginParameters());
        dbConnection.setLabel("the label of the new connection failed");
        PluginMetaData metaData = new PluginMetaData();
        metaData.setPluginClassName(className);
        metaData.setPluginId("test");
        dbConnection.setMetaData(metaData);
        Mockito.when(pluginServiceMock.checkPluginClassName(IDBConnectionPlugin.class, className))
                .thenThrow(EntityInvalidException.class);
        dbConnectionServiceMock.createDBConnection(dbConnection);
        Assert.fail();
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
        return IPluginParam.set(IPluginParam.build(DBConnectionPluginConstants.USER_PARAM, dbUser),
                                IPluginParam.build(DBConnectionPluginConstants.PASSWORD_PARAM, dbPassword),
                                IPluginParam.build(DBConnectionPluginConstants.DB_HOST_PARAM, dbHost),
                                IPluginParam.build(DBConnectionPluginConstants.DB_PORT_PARAM, dbPort),
                                IPluginParam.build(DBConnectionPluginConstants.DB_NAME_PARAM, dbName),
                                IPluginParam.build(DBConnectionPluginConstants.DRIVER_PARAM, POSTGRESQL_JDBC_DRIVER));
    }

    private List<PluginParamDescriptor> initializePluginParameterType() {
        return Arrays.asList(
                             PluginParamDescriptor.create("model", "model", null, PluginParamType.STRING, false, false,
                                                          false),
                             PluginParamDescriptor.create("connection", "connection", null, PluginParamType.PLUGIN,
                                                          false, false, false));
    }

}
