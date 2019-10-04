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
package fr.cnes.regards.modules.dam.service.entities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDocumentRepository;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.EntityAipState;
import fr.cnes.regards.modules.dam.domain.entities.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.domain.models.attributes.AttributeModel;
import fr.cnes.regards.modules.dam.gson.entities.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.dam.service.entities.plugins.AipStoragePlugin;
import fr.cnes.regards.modules.dam.service.models.IAttributeModelService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;
import fr.cnes.regards.modules.project.domain.Project;
import fr.cnes.regards.modules.storage.client.IAipClient;

/**
 * This test IT allows to test the AIP storage for the entities {@link Dataset}.
 * The storage of the entity failed.
 * This test use the {@link AipStoragePlugin}.
 *
 * @author Christophe Mertz
 */
@ContextConfiguration(classes = AipClientPartialResponseConfigurationMock.class)
@TestPropertySource(locations = { "classpath:test-with-storage.properties" })
public class AIPStorageEntityPartialResponseIT extends AbstractRegardsServiceIT {

    private static Logger LOGGER = LoggerFactory.getLogger(AIPStorageEntityPartialResponseIT.class);

    private static int SLEEP_TIME = 20000;

    private static String MODEL_DATASET_FILE_NAME = "modelDataSet.xml";

    private static String MODEL_DATASET_NAME = "modelDataSet";

    @Autowired
    private IModelService modelService;

    @Autowired
    protected IAttributeModelService attributeModelService;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IDatasetRepository dsRepository;

    @Autowired
    private IDocumentRepository docRepository;

    @Autowired
    private ICollectionRepository colRepository;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepository;

    @Autowired
    private IProjectsClient projectsClient;

    @Autowired
    protected MultitenantFlattenedAttributeAdapterFactory gsonAttributeFactory;

    private Model modelDataset;

    private Dataset dataset1;

    @Autowired
    private IAipClient aipClient;

    @Autowired
    private IRuntimeTenantResolver tenantResolver;

    @Test
    @Ignore
    public void createDataset() throws ModuleException, InterruptedException {

        dataset1 = dsService.create(dataset1);
        LOGGER.info("===> create dataset1 (" + dataset1.getIpId() + ")");

        Assert.assertEquals(1, dsRepository.count());

        Thread.sleep(SLEEP_TIME);

        Dataset dsFind = dsRepository.findById(dataset1.getId()).get();
        Assert.assertEquals(EntityAipState.AIP_STORE_ERROR, dsFind.getStateAip());
    }

    /**
     * Import model definition file from resources directory
     * @param filename the XML file containing the model to import
     * @return the created model attributes
     * @throws ModuleException if error occurs
     */
    private Model importModel(String filename) throws ModuleException {
        try {
            InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", filename));
            return modelService.importModel(input);
        } catch (IOException e) {
            String errorMessage = "Cannot import " + filename;
            throw new AssertionError(errorMessage);
        }
    }

    @Before
    public void init() throws ModuleException {
        Project project = new Project();
        project.setHost("http://regardsHost");

        Mockito.when(projectsClient.retrieveProject(Mockito.anyString()))
                .thenReturn(new ResponseEntity<>(new Resource<>(project), HttpStatus.OK));

        cleanRepository();

        initDataset();

    }

    @After
    public void cleanAfter() {
        cleanRepository();
    }

    private void cleanRepository() {
        tenantResolver.forceTenant(getDefaultTenant());

        dsRepository.deleteAll();
        docRepository.deleteAll();
        colRepository.deleteAll();
        pluginConfRepository.deleteAll();

        try {
            // Remove the model if existing
            modelService.deleteModel(MODEL_DATASET_NAME);
        } catch (ModuleException e) {
            // There is nothing to do - we create the model later
        }
    }

    private void initDataset() throws ModuleException {
        modelDataset = importModel(MODEL_DATASET_FILE_NAME);

        // - Refresh attribute factory
        List<AttributeModel> atts = attributeModelService.getAttributes(null, null, null, null);
        gsonAttributeFactory.refresh(getDefaultTenant(), atts);

        dataset1 = new Dataset(modelDataset, getDefaultTenant(), "DS1", "dataset one label");
        dataset1.setLicence("the licence");
        dataset1.setProviderId("ProviderId1");
        dataset1.setTags(Sets.newHashSet("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        dataset1.setNormalizedGeometry(IGeometry
                .multiPoint(IGeometry.position(41.12, -10.5), IGeometry.position(42., -72.),
                            IGeometry.position(15., -72.), IGeometry.position(15., -9.)));

        dataset1.addProperty(AttributeBuilder.buildInteger("VSIZE", 12345));
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

        dataset1.setOpenSearchSubsettingClause("label:TheSubsettingClause");
    }

}
