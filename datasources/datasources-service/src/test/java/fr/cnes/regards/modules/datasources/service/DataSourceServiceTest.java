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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.mockito.Mockito;

import fr.cnes.regards.framework.modules.plugins.domain.PluginConfiguration;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameterType.ParamType;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.modules.datasources.plugins.PostgreDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDBDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;

/**
 * Unit testing of {@link DataSourceService}.
 * @author Christophe Mertz
 */
public class DataSourceServiceTest {

    private IPluginService pluginServiceMock;

    /**
     * A mock of {@link IDBConnectionService}
     */
    private IDataSourceService dataSourceServiceMock;

    private IDBConnectionService connectionServiceMock;

    /**
     * A {@link List} of {@link PluginConfiguration}
     */
    private final List<PluginConfiguration> plgConfs = new ArrayList<>();

    private PluginConfiguration internalConf;

    private PluginConfiguration externalConf;

    /**
     * This method is run before all tests
     */
    @Before
    public void init() {
        // create mock services
        pluginServiceMock = Mockito.mock(IPluginService.class);
        dataSourceServiceMock = new DataSourceService(pluginServiceMock, connectionServiceMock);

        // create PluginConfiguration
        internalConf = new PluginConfiguration(this.initializePluginMeta(), "internal configuration",
                                               initializePluginParameterIsInternalDataSource());
        externalConf = new PluginConfiguration(this.initializePluginMeta(), "external configuration",
                                               initializePluginParameterNotInternalDataSource());

        externalConf.setId(123456L);
        plgConfs.add(externalConf);
        plgConfs.add(new PluginConfiguration(this.initializePluginMeta(), "third configuration",
                                             initializePluginParameterNotInternalDataSource()));
        plgConfs.add(internalConf);
        plgConfs.add(new PluginConfiguration(this.initializePluginMeta(), "forth configuration",
                                             initializePluginParameterNotInternalDataSource()));
    }

    private PluginMetaData initializePluginMeta() {
        final PluginMetaData pluginMetaData = new PluginMetaData();
        pluginMetaData.setPluginClassName(PostgreDataSourcePlugin.class.getCanonicalName());
        pluginMetaData.setPluginId("plugin-id");
        pluginMetaData.setAuthor("CS-SI");
        pluginMetaData.setVersion("1.0");
        pluginMetaData.setParameters(initializePluginParameterType());
        return pluginMetaData;
    }

    private List<PluginParameter> initializePluginParameterIsInternalDataSource() {
        return PluginParametersFactory.build().addParameter(IDBDataSourcePlugin.FROM_CLAUSE, "from t_table_name")
                .addParameter(IDataSourcePlugin.MODEL_PARAM, "model param").getParameters();
    }

    private List<PluginParameter> initializePluginParameterNotInternalDataSource() {
        return PluginParametersFactory.build().addParameter(IDBDataSourcePlugin.FROM_CLAUSE, "from table")
                .addParameter(IDataSourcePlugin.MODEL_PARAM, "model").getParameters();
    }

    private List<PluginParameterType> initializePluginParameterType() {
        return Arrays.asList(PluginParameterType
                                     .create(IDataSourcePlugin.MODEL_PARAM, "MODEL_PARAM", null, String.class,
                                             ParamType.PRIMITIVE, false), PluginParameterType
                                     .create(IDBDataSourcePlugin.FROM_CLAUSE, "FROM_CLAUSE", null, String.class,
                                             ParamType.PRIMITIVE, false));
    }
}
