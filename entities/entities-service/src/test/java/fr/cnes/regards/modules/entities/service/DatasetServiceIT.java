/*
 * Copyright 2017-2018 CNES - CENTRE NATIONAL d'ETUDES SPATIALES
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
package fr.cnes.regards.modules.entities.service;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.RequestBody;

import com.google.common.collect.Sets;
import com.google.gson.Gson;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.modules.plugins.service.IPluginService;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIPCollection;
import fr.cnes.regards.modules.storage.domain.RejectedAip;

/**
 * @author Christophe Mertz
 */
@TestPropertySource(locations = { "classpath:test.properties" })
public class DatasetServiceIT extends AbstractRegardsServiceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasetServiceIT.class);

    private static final String TENANT = DEFAULT_TENANT;

    private static final String MODEL_FILE_NAME = "modelDataSet.xml";

    private static final String MODEL_NAME = "modelDataSet";

    //    @Autowired
    //    private Gson gson;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private IDatasetRepository datasetRepository;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepository;

    private Model modelDataset;

    private Dataset dataset1;

    private Dataset dataset2;

    @Autowired
    IAipClient aipClient;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Configuration
    static class AipClientConfigurationMock {

        @Autowired
        private Gson gson;

        @Bean
        public IAipClient aipClient() {
            AipClientProxy aipClientProxy = new AipClientProxy();
            InvocationHandler handler = (proxy, method, args) -> {
                for (Method aipClientProxyMethod : aipClientProxy.getClass().getMethods()) {
                    if (aipClientProxyMethod.getName().equals(method.getName())) {
                        return aipClientProxyMethod.invoke(aipClientProxy, args);
                    }
                }
                return null;
            };
            return (IAipClient) Proxy.newProxyInstance(IAipClient.class.getClassLoader(),
                                                       new Class<?>[] { IAipClient.class }, handler);
        }

        private class AipClientProxy {

            public ResponseEntity<List<RejectedAip>> store(@RequestBody AIPCollection aips) {

                String gsonString = gson.toJson(aips.getFeatures().get(0));
                LOGGER.debug("============= " + aips.getFeatures().get(0).getSipId() + " =============" + gsonString);

                return new ResponseEntity<>(HttpStatus.CREATED);
            }

        }
    }

    @Before
    public void init() throws ModuleException {
        tenantResolver.forceTenant(TENANT);

        datasetRepository.deleteAll();
        pluginConfRepository.deleteAll();

        try {
            // Remove the model if existing
            modelService.getModelByName(MODEL_NAME);
            modelService.deleteModel(MODEL_NAME);
        } catch (ModuleException e) {
            // There is nothing to do - we create the model later
        }
        modelDataset = importModel(MODEL_FILE_NAME);

        dataset1 = new Dataset(modelDataset, TENANT, "labelDs1");
        dataset1.setLicence("the licence");
        dataset1.setSipId("SipId1");
        dataset1.setTags(Sets.newHashSet("Monday", "Tuesday", "Wednesday", "Thusrday", "Friday", "Saturday"));
        dataset1.addQuotation("hello");
        dataset1.addQuotation("coucou");
        dataset1.addQuotation("bonjour");
        dataset1.addQuotation("guten tag");
        dataset1.setScore(99);

        dataset1.addProperty(AttributeBuilder.buildInteger("SIZE", (int) 12345));
        dataset1.addProperty(AttributeBuilder.buildDate("START_DATE", OffsetDateTime.now().minusHours(15)));
        dataset1.addProperty(AttributeBuilder.buildDouble("SPEED", 98765.12345));
        dataset1.addProperty(AttributeBuilder.buildBoolean("IS_UPDATE", true));
        dataset1.addProperty(AttributeBuilder.buildString("ORIGIN", "the dataset origin"));
        dataset1.addProperty(AttributeBuilder.buildLong("LONG_VALUE", new Long(98765432L)));

        dataset1.addProperty(AttributeBuilder.buildDateInterval("RANGE_DATE", OffsetDateTime.now().minusMinutes(133),
                                                                OffsetDateTime.now().minusMinutes(45)));
        dataset1.addProperty(AttributeBuilder.buildIntegerInterval("RANGE_INTEGER", 133, 187));

        dataset1.addProperty(AttributeBuilder
                .buildDateArray("DATES", OffsetDateTime.now().minusMinutes(90), OffsetDateTime.now().minusMinutes(80),
                                OffsetDateTime.now().minusMinutes(70), OffsetDateTime.now().minusMinutes(60),
                                OffsetDateTime.now().minusMinutes(50)));
        dataset1.addProperty(AttributeBuilder.buildIntegerArray("SIZES", 150, 250, 350));
        dataset1.addProperty(AttributeBuilder.buildStringArray("HISTORY", "one", "two", "three", "for", "five"));
        dataset1.addProperty(AttributeBuilder.buildDoubleArray("DOUBLE_VALUES", 1.23, 0.232, 1.2323, 54.656565,
                                                               0.5656565656565));

        dataset1.addProperty(AttributeBuilder.buildLongArray("LONG_VALUES", new Long(985432L), new Long(5656565465L),
                                                             new Long(5698L), new Long(5522336689L), new Long(7748578L),
                                                             new Long(22000014L), new Long(9850012565556565L)));

        //        PluginConfiguration datasource = pluginService.savePluginConfiguration(new PluginConfiguration(
        //                getPluginMetaData(), "a datasource pluginconf fake", PluginParametersFactory.build()
        //                        .addParameter(FakeDataSourcePlugin.MODEL, gson.toJson(modelData)).getParameters(),
        //                0));
        //        dataset1.setSubsettingClause(ICriterion.all());
        //        dataset1.setDataSource(datasource);

        dataset1.setOpenSearchSubsettingClause("the open search subsetting claise");

        //        dataset2 = new Dataset(modelDataset, tenant, "labelDs2");
        //        dataset2.setLicence("licence");
        //        dataset2.setSipId("SipId2");
        //        dataset2.setTags(Sets.newHashSet("one", "two", "three"));
    }

    @Test
    public void testCreateDataset() throws ModuleException, IOException {
        Assert.assertTrue(true);

        dataset1 = dsService.create(dataset1);
        LOGGER.info("===> create dataset1 (" + dataset1.getIpId() + ")");

        //        dataset2 = dsService.create(dataset2);
        //        LOGGER.info("===> create dataset1 (" + dataset2.getIpId() + ")");

        Assert.assertEquals(1, datasetRepository.count());

    }

    private PluginMetaData getPluginMetaData() {
        return PluginUtils.createPluginMetaData(FakeDataSourcePlugin.class,
                                                FakeDataSourcePlugin.class.getPackage().getName());
    }

    /**
     * Import model definition file from resources directory
     * @param filename filename
     * @return the created model attributes
     * @throws ModuleException if error occurs
     */
    private Model importModel(final String filename) throws ModuleException {
        try {
            final InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", filename));
            return modelService.importModel(input);
        } catch (final IOException e) {
            final String errorMessage = "Cannot import " + filename;
            throw new AssertionError(errorMessage);
        }
    }

}
