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
package fr.cnes.regards.modules.datasources.plugins.datasource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.feign.security.FeignSecurityManager;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParametersFactory;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.datasources.plugins.AipDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.exceptions.DataSourcesPluginException;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.storage.client.IAipClient;

/**
 * @author oroussel
 */
@ContextConfiguration(classes = { AipDataSourceConfiguration.class })
@TestPropertySource("classpath:aip-datasource-test.properties")
public class AipDataSourcePluginTest extends AbstractRegardsServiceIT {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourceFromSingleTablePluginTest.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

//    private static final String TENANT = "PLUGINS";
private static final String TENANT = DEFAULT_TENANT;

    private static final String MODEL_FILE_NAME = "model.xml";

    private static final String MODEL_NAME = "model_1";

    private AipDataSourcePlugin dsPlugin;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAipClient aipClient;

//    @Autowired
//    private IAttributeModelService attributeModelService;
//
//    @Autowired
//    private MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;


    @Before
    public void setUp() throws DataSourcesPluginException, SQLException, ModuleException {
        tenantResolver.forceTenant(TENANT);

        Model model = modelService.getModelByName(MODEL_NAME);
        if (model != null) {
            modelService.deleteModel(model.getId());
        }

        importModel(MODEL_FILE_NAME);

        FeignSecurityManager.asSystem();
//        Mockito.doReturn(null).when(aipClient).retrieveAIPs(Mockito.any(), Mockito.any(), Mockito.any(), Mockito.anyInt(), Mockito.anyInt());
//        aipClient.retrieveAIPs(AIPState.STORED, null, null, 0, 10);
        FeignSecurityManager.reset();

        Map<Long, Object> pluginCacheMap = new HashMap<>();

        // Instantiate the data source plugin
        List<PluginParameter> parameters;
        parameters = PluginParametersFactory.build().addParameter(AipDataSourcePlugin.BINDING_MAP, createBindingMap())
                .addParameter(AipDataSourcePlugin.MODEL_NAME_PARAM, MODEL_NAME)
                .addParameter(IDataSourcePlugin.REFRESH_RATE, "1800").getParameters();

        dsPlugin = PluginUtils.getPlugin(parameters, AipDataSourcePlugin.class, Arrays.asList(PLUGIN_CURRENT_PACKAGE),
                                         pluginCacheMap);

    }

    private Map<String, String> createBindingMap() {
        return new HashMap<>();
    }

    /**
     * Import model definition file from resources directory
     * @param filename filename
     * @return list of created model attributes
     * @throws ModuleException if error occurs
     */
    private void importModel(final String filename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", filename));
            modelService.importModel(input);

//            final List<AttributeModel> attributes = attributeModelService.getAttributes(null, null);
//            gsonAttributeFactory.refresh(TENANT, attributes);
        } catch (final IOException e) {
            final String errorMessage = "Cannot import " + filename;
            throw new AssertionError(errorMessage);
        }
    }

    @Test
    public void test() throws DataSourceException {
//        Page<DataObject> page = dsPlugin.findAll(TENANT, new PageRequest(0, 10));
    }

    @After
    public void tearDown() {
        Model model = modelService.getModelByName(MODEL_NAME);
        if (model != null) {
            modelService.deleteModel(model.getId());
        }
    }
}
