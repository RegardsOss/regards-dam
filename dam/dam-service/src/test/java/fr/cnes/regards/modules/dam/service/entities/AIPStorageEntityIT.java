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
package fr.cnes.regards.modules.dam.service.entities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Arrays;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Sets;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDatasetRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDocumentRepository;
import fr.cnes.regards.modules.dam.dao.entities.ILocalFileRepository;
import fr.cnes.regards.modules.dam.domain.entities.AbstractEntity;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.Dataset;
import fr.cnes.regards.modules.dam.domain.entities.Document;
import fr.cnes.regards.modules.dam.domain.entities.EntityAipState;
import fr.cnes.regards.modules.dam.domain.entities.attribute.AbstractAttribute;
import fr.cnes.regards.modules.dam.domain.entities.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.service.entities.plugins.AipStoragePlugin;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.project.client.rest.IProjectsClient;
import fr.cnes.regards.modules.project.domain.Project;
import fr.cnes.regards.modules.storage.client.IAipClient;

/**
 * This test IT allows to test the AIP storage for the entities {@link Dataset}, {@link Document} and
 * {@link Collection}.
 * This test use the {@link AipStoragePlugin}.
 *
 * @author Christophe Mertz
 */
@ContextConfiguration(
        classes = { AbstractMultitenantServiceTest.ScanningConfiguration.class, AipClientConfigurationMock.class })
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=storage",
        "regards.dam.post.aip.entities.to.storage=true", "regards.amqp.enabled=true",
        "regards.dam.store.aip.entities.delay:1000", "regards.dam.store.aip.entities.initial.delay:1000",
        "zuul.prefix=/api/v1" }, locations = "classpath:es.properties")
@ActiveProfiles({ "testAmqp" })
public class AIPStorageEntityIT extends AbstractMultitenantServiceTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(AIPStorageEntityIT.class);

    private static final String MODEL_DATASET_FILE_NAME = "modelDataSet.xml";

    private static final String MODEL_DATASET_NAME = "modelDataSet";

    private static final String MODEL_DOCUMENT_FILE_NAME = "modelDocument.xml";

    private static final String MODEL_DOCUMENT_NAME = "modelDocument";

    private static final String MODEL_COLLECTION_FILE_NAME = "modelCollection.xml";

    private static final String MODEL_COLLECTION_NAME = "modelCollection";

    private static final String DESCRIPTION_MEDIA_TYPE_VALUE = MediaType.TEXT_MARKDOWN_VALUE + " ;charset="
            + StandardCharsets.UTF_8.toString();

    private static final MediaType DESCRIPTION_MEDIA_TYPE = MediaType.parseMediaType(DESCRIPTION_MEDIA_TYPE_VALUE);

    private static final String URL_TEMPLATE = "file:///documents/files/"
            + LocalStorageService.FILE_CHECKSUM_URL_TEMPLATE;

    @Autowired
    private IModelService modelService;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IDocumentService docService;

    @Autowired
    private ICollectionService colService;

    @Autowired
    private IDatasetRepository dsRepository;

    @Autowired
    private IDocumentRepository docRepository;

    @Autowired
    private ICollectionRepository colRepository;

    @Autowired
    private ILocalFileRepository localFileRepository;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepository;

    @Autowired
    private IProjectsClient projectsClient;

    private Model modelDataset;

    private Model modelDocument;

    private Model modelCollection;

    private Dataset dataset1;

    private Document document1;

    private Collection collection1;

    @Autowired
    IAipClient aipClient;

    @Test
    public void createDataset() throws ModuleException, IOException, InterruptedException {
        // Wait for asynchronous tasks
        waitAndCheck(dataset1, EntityAipState.AIP_STORE_OK);
    }

    private MockMultipartFile getDescription() throws IOException {
        byte[] input = Files.readAllBytes(Paths.get("src", "test", "resources", "data", "test.md"));
        MockMultipartFile multiPartFile = new MockMultipartFile("description-markdown-format", "test.md",
                DESCRIPTION_MEDIA_TYPE_VALUE, input);
        return multiPartFile;
    }

    @Test
    public void createDatasetWithDescription() throws ModuleException, IOException, InterruptedException {

        // Attach a description
        MultipartFile[] attachments = new MultipartFile[] { getDescription() };
        dsService.attachFiles(dataset1.getIpId(), DataType.DESCRIPTION, attachments, null, URL_TEMPLATE);

        // Wait for asynchronous tasks
        waitAndCheck(dataset1, EntityAipState.AIP_STORE_OK);
    }

    @Requirement("REGARDS_DSL_DAM_DOC_010")
    @Requirement("REGARDS_DSL_DAM_DOC_210")
    @Purpose("Document archiving")
    @Test
    public void createDocument() throws ModuleException, IOException, InterruptedException {

        // Attach documents
        MockMultipartFile mockMultipartFile = new MockMultipartFile("document1.xml", "document1.xml", "doc/xml",
                "content of my file".getBytes());
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("document2.png", "document2.png", "image/png",
                "some pixels informations".getBytes());
        MultipartFile[] multipartFiles = { mockMultipartFile, mockMultipartFile2 };
        docService.attachFiles(document1.getIpId(), DataType.DOCUMENT, multipartFiles, null, URL_TEMPLATE);

        // Wait for asynchronous tasks
        waitAndCheck(document1, EntityAipState.AIP_STORE_OK);
    }

    @Test
    public void createCollectionWithDescription() throws ModuleException, IOException, InterruptedException {

        // Attach a description
        MultipartFile[] attachments = new MultipartFile[] { getDescription() };
        colService.attachFiles(collection1.getIpId(), DataType.DESCRIPTION, attachments, null, URL_TEMPLATE);

        // Wait for asynchronous tasks
        waitAndCheck(collection1, EntityAipState.AIP_STORE_OK);
    }

    @Test
    public void createCollectionWithDescriptionAsUrl() throws ModuleException, IOException, InterruptedException {

        // Attach a description as URL
        DataFile ref = DataFile.build(DataType.DESCRIPTION, "README.md",
                                      "https://github.com/RegardsOss/regards-microservice/blob/master/README.md",
                                      DESCRIPTION_MEDIA_TYPE, true, true);
        colService.attachFiles(collection1.getIpId(), DataType.DESCRIPTION, null, Arrays.asList(ref), URL_TEMPLATE);

        // Wait for asynchronous tasks
        waitAndCheck(collection1, EntityAipState.AIP_STORE_OK);
    }

    @Test
    public void createCollectionWithoutDescription() throws ModuleException, IOException, InterruptedException {
        // Wait for asynchronous tasks
        waitAndCheck(collection1, EntityAipState.AIP_STORE_OK);
    }

    @Test
    public void updateDataset() throws ModuleException, IOException, InterruptedException {

        // Update dataset
        AbstractAttribute<?> attr2Delete = dataset1.getProperty("LONG_VALUE");
        dataset1.removeProperty(attr2Delete);
        dataset1.removeTags(Arrays.asList(dataset1.getTags().iterator().next()));
        dsService.update(dataset1);

        // Wait for asynchronous tasks
        waitAndCheck(dataset1, EntityAipState.AIP_STORE_OK);
    }

    private void waitAndCheck(AbstractEntity<?> entity, EntityAipState expectedState) throws InterruptedException {
        int iteration = 0;
        boolean testOK = false;
        AbstractEntity<?> loadedEntity = null;
        while ((iteration <= 20) && !testOK) {
            switch (entity.getFeature().getEntityType()) {
                case COLLECTION:
                    loadedEntity = colRepository.findOne(entity.getId());
                    break;
                case DATASET:
                    loadedEntity = dsRepository.findOne(entity.getId());
                    break;
                case DOCUMENT:
                    loadedEntity = docRepository.findOne(entity.getId());
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported entity type");
            }
            testOK = expectedState.equals(loadedEntity.getStateAip());
            Thread.sleep(1000);
            iteration++;
        }
        Assert.assertNotNull(loadedEntity);
        Assert.assertEquals(expectedState, loadedEntity.getStateAip());
    }

    /**
     * Import model definition file from resources directory
     * @param filename the XML file containing the model to import
     * @return the created model attributes
     * @throws ModuleException if error occurs
     */
    private Model importModel(final String filename) throws ModuleException {
        try (InputStream input = Files.newInputStream(Paths.get("src", "test", "resources", filename))) {
            Model model = modelService.importModel(input);
            Thread.sleep(2000);
            return model;
        } catch (final IOException | InterruptedException e) {
            final String errorMessage = "Cannot import " + filename;
            throw new AssertionError(errorMessage);
        }
    }

    @Before
    public void init() throws ModuleException, InterruptedException {
        simulateApplicationReadyEvent();

        runtimeTenantResolver.forceTenant(getDefaultTenant());

        Project project = new Project();
        project.setHost("http://regardsHost");

        Mockito.when(projectsClient.retrieveProject(Mockito.anyString()))
                .thenReturn(new ResponseEntity<>(new Resource<>(project), HttpStatus.OK));

        cleanUp();
        initDataset();
        initDocument();
        initCollection();

        // Wait for asynchronous tasks
        Thread.sleep(5000);
    }

    @After
    public void cleanAfter() {
        cleanUp();
    }

    private void cleanUp() {
        localFileRepository.deleteAll();
        dsRepository.deleteAll();
        docRepository.deleteAll();
        colRepository.deleteAll();
        pluginConfRepository.deleteAll();

        try {
            // Remove the model if existing
            modelService.deleteModel(MODEL_DATASET_NAME);
            modelService.deleteModel(MODEL_DOCUMENT_NAME);
            modelService.deleteModel(MODEL_COLLECTION_NAME);
        } catch (ModuleException e) {
            // There is nothing to do - we create the model later
        }
    }

    private void initDataset() throws ModuleException {
        modelDataset = importModel(MODEL_DATASET_FILE_NAME);

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

        dataset1.setOpenSearchSubsettingClause("the open search subsetting clause");

        // Create dataset
        dsService.create(dataset1);
        LOGGER.info("===> create dataset1 (" + dataset1.getIpId() + ")");
        Assert.assertEquals(1, dsRepository.count());
    }

    private void initDocument() throws ModuleException {
        modelDocument = importModel(MODEL_DOCUMENT_FILE_NAME);

        document1 = new Document(modelDocument, getDefaultTenant(), "DOC", "My document label");
        document1.setTags(Sets.newHashSet("azertyui"));

        document1.addProperty(AttributeBuilder.buildDate("creation_date", OffsetDateTime.now().minusHours(452)));
        document1.addProperty(AttributeBuilder.buildStringArray("AUTHORS", "Jo", "John Doe", "Bill"));
        document1.addProperty(AttributeBuilder.buildString("TITLE", "the document title"));

        // Create document
        docService.create(document1);
        LOGGER.info("===> create document (" + document1.getIpId() + ")");
        Assert.assertEquals(1, docRepository.count());
    }

    private void initCollection() throws ModuleException {
        modelCollection = importModel(MODEL_COLLECTION_FILE_NAME);

        collection1 = new Collection(modelCollection, getDefaultTenant(), "DOC", "My collection label");
        collection1.setTags(Sets.newHashSet("COLLECTION-ONE", "COLLECTION-02", "COLLECTION-03"));

        collection1.addProperty(AttributeBuilder.buildDate("creation_date", OffsetDateTime.now().minusHours(452)));

        // Create collection
        colService.create(collection1);
        LOGGER.info("===> create collection1 (" + collection1.getIpId() + ")");
        Assert.assertEquals(1, colRepository.count());
    }
}
