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
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.domain.PluginParameter;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.OAISIdentifier;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.framework.utils.plugins.PluginParametersFactory;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.datasources.plugins.AipDataSourcePlugin;
import fr.cnes.regards.modules.datasources.plugins.exception.DataSourceException;
import fr.cnes.regards.modules.datasources.plugins.interfaces.IDataSourcePlugin;
import fr.cnes.regards.modules.datasources.utils.exceptions.DataSourcesPluginException;
import fr.cnes.regards.modules.entities.domain.DataObject;
import fr.cnes.regards.modules.entities.domain.attribute.StringArrayAttribute;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPBuilder;

/**
 * @author oroussel
 */
@ContextConfiguration(classes = { AipDataSourceConfiguration.class })
@TestPropertySource("classpath:aip-datasource-test.properties")
public class AipDataSourcePluginTest extends AbstractRegardsServiceIT {

    private static final Logger LOG = LoggerFactory.getLogger(PostgreDataSourceFromSingleTablePluginTest.class);

    private static final String PLUGIN_CURRENT_PACKAGE = "fr.cnes.regards.modules.datasources.plugins";

    protected static final String TENANT = DEFAULT_TENANT;

    private static final String MODEL_FILE_NAME = "model.xml";

    private static final String MODEL_NAME = "model_1";

    private AipDataSourcePlugin dsPlugin;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Autowired
    private IAipClient aipClient;

    @Before
    public void setUp() throws DataSourcesPluginException, SQLException, ModuleException {
        tenantResolver.forceTenant(TENANT);

        Model model = modelService.getModelByName(MODEL_NAME);
        if (model != null) {
            modelService.deleteModel(model.getId());
        }

        importModel(MODEL_FILE_NAME);

        Map<Long, Object> pluginCacheMap = new HashMap<>();

        // Instantiate the data source plugin
        List<PluginParameter> parameters;
        parameters = PluginParametersFactory.build().addParameter(AipDataSourcePlugin.BINDING_MAP, createBindingMap())
                .addParameter(AipDataSourcePlugin.MODEL_NAME_PARAM, MODEL_NAME)
                .addParameter(IDataSourcePlugin.REFRESH_RATE, 1800).getParameters();

        dsPlugin = PluginUtils.getPlugin(parameters, AipDataSourcePlugin.class, Arrays.asList(PLUGIN_CURRENT_PACKAGE),
                                         pluginCacheMap);

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
        } catch (final IOException e) {
            final String errorMessage = "Cannot import " + filename;
            throw new AssertionError(errorMessage);
        }
    }

    // This method is called by Aip client proxy (from AipDataSOurceConfiguration) to provide some AIPs when calling
    // aip client method
    protected static List<AIP> createAIPs(int count, String... tags) {
        List<AIP> aips = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            UniformResourceName id = new UniformResourceName(OAISIdentifier.AIP, EntityType.DATA,
                    AipDataSourcePluginTest.TENANT, UUID.randomUUID(), 1);
            AIPBuilder builder = new AIPBuilder(id, "sipId" + i, EntityType.DATA);
            builder.addTags(tags);

            builder.addDescriptiveInformation("label", "libellé du data object " + i);
            builder.addDescriptiveInformation("START_DATE", OffsetDateTime.now());
            builder.addDescriptiveInformation("ALT_MAX", 1500 + i);
            builder.addDescriptiveInformation("HISTORY", new String[] { "H1", "H2", "H3" });
            builder.addDescriptiveInformation("POUET", "POUET");
            aips.add(builder.build());
        }
        return aips;
    }

    /**
     * Binding map from AIP (key) properties and associated model attributes (value)
     */
    private Map<String, String> createBindingMap() {
        HashMap<String, String> map = new HashMap<>();
        map.put("properties.descriptiveInformation.label", "label");
        map.put("properties.descriptiveInformation.START_DATE", "properties.START_DATE");
        map.put("properties.descriptiveInformation.ALT_MAX", "properties.ALTITUDE.MAX");
        map.put("properties.descriptiveInformation.ALT_MIN", "properties.ALTITUDE.MIN");
        map.put("properties.descriptiveInformation.HISTORY", "properties.history");
        map.put("properties.descriptiveInformation.NIMP", "properties.history");
        return map;
    }

    @Test
    public void test() throws DataSourceException {
        Page<DataObject> page = dsPlugin.findAll(TENANT, new PageRequest(0, 10));
        Assert.assertNotNull(page);
        Assert.assertNotNull(page.getContent());
        Assert.assertTrue(page.getContent().size() > 0);
        DataObject do1 = page.getContent().get(0);
        Assert.assertEquals("libellé du data object 0", do1.getLabel());
        Assert.assertNotNull(do1.getProperty("START_DATE"));
        Assert.assertNotNull(do1.getProperty("ALTITUDE.MAX"));
        Assert.assertNotNull(do1.getProperty("ALTITUDE.MIN"));
        Assert.assertNotNull(do1.getTags());
        Assert.assertTrue(do1.getTags().contains("tag1"));
        Assert.assertTrue(do1.getTags().contains("tag2"));
        Assert.assertTrue(do1.getProperty("history") instanceof StringArrayAttribute);
        Assert.assertTrue(Arrays.binarySearch(((StringArrayAttribute) do1.getProperty("history")).getValue(),
                                              "H1") > -1);
        Assert.assertTrue(Arrays.binarySearch(((StringArrayAttribute) do1.getProperty("history")).getValue(),
                                              "H2") > -1);
        Assert.assertNotNull(do1.getFiles());
        Assert.assertEquals(1, do1.getFiles().size());
        Assert.assertTrue(do1.getFiles().containsKey(DataType.RAWDATA));

    }

    @After
    public void tearDown() {
        Model model = modelService.getModelByName(MODEL_NAME);
        if (model != null) {
            modelService.deleteModel(model.getId());
        }
    }
}
