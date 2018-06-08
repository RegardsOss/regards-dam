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
import java.util.Set;

import javax.validation.Valid;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.multipart.MultipartFile;

import com.google.common.collect.Sets;
import com.google.gson.Gson;

import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.modules.plugins.dao.IPluginConfigurationRepository;
import fr.cnes.regards.framework.modules.plugins.domain.PluginMetaData;
import fr.cnes.regards.framework.multitenant.IRuntimeTenantResolver;
import fr.cnes.regards.framework.test.integration.AbstractRegardsServiceIT;
import fr.cnes.regards.framework.utils.plugins.PluginUtils;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDatasetRepository;
import fr.cnes.regards.modules.entities.dao.IDocumentLSRepository;
import fr.cnes.regards.modules.entities.dao.IDocumentRepository;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Dataset;
import fr.cnes.regards.modules.entities.domain.DescriptionFile;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.domain.attribute.AbstractAttribute;
import fr.cnes.regards.modules.entities.domain.attribute.builder.AttributeBuilder;
import fr.cnes.regards.modules.entities.domain.geometry.Geometry;
import fr.cnes.regards.modules.entities.service.plugins.AipStoragePlugin;
import fr.cnes.regards.modules.models.domain.Model;
import fr.cnes.regards.modules.models.service.IModelService;
import fr.cnes.regards.modules.storage.client.IAipClient;
import fr.cnes.regards.modules.storage.domain.AIP;
import fr.cnes.regards.modules.storage.domain.AIPCollection;
import fr.cnes.regards.modules.storage.domain.RejectedAip;

/**
 * This test IT allows to test the AIP storage for the entities {@link Dataset}, {@link Document} and {@link Collection}.
 * This test use the {@link AipStoragePlugin}.
 *  
 * @author Christophe Mertz
 */
@TestPropertySource(locations = { "classpath:test-with-storage.properties" })
public class AIPStorageEntityIT extends AbstractRegardsServiceIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(AIPStorageEntityIT.class);

    private static final String TENANT = DEFAULT_TENANT;

    private static final String MODEL_DATASET_FILE_NAME = "modelDataSet.xml";

    private static final String MODEL_DATASET_NAME = "modelDataSet";

    private static final String MODEL_DOCUMENT_FILE_NAME = "modelDocument.xml";

    private static final String MODEL_DOCUMENT_NAME = "modelDocument";

    private static final String MODEL_COLLECTION_FILE_NAME = "modelCollection.xml";

    private static final String MODEL_COLLECTION_NAME = "modelCollection";

    @Autowired
    private IModelService modelService;

    @Autowired
    private IDatasetService dsService;

    @Autowired
    private IDocumentService docService;

    @Autowired
    private ICollectionService colService;

    //    @Autowired
    //    private IPluginService pluginService;

    @Autowired
    private IDatasetRepository dsRepository;

    @Autowired
    private IDocumentRepository docRepository;

    @Autowired
    private IDocumentLSRepository docLsRepository;

    @Autowired
    private ICollectionRepository colRepository;

    @Autowired
    private IPluginConfigurationRepository pluginConfRepository;

    private Model modelDataset;

    private Model modelDocument;

    private Model modelCollection;

    private Dataset dataset1;

    private Document document1;

    private Collection collection1;

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

            @SuppressWarnings("unused")
            public ResponseEntity<List<RejectedAip>> store(@RequestBody AIPCollection aips) {

                String gsonString = gson.toJson(aips.getFeatures().get(0));
                LOGGER.debug("==========>  CREATION ===> " + aips.getFeatures().get(0).getId() + " ============="
                        + gsonString);

                return new ResponseEntity<>(HttpStatus.CREATED);
            }

            @SuppressWarnings("unused")
            public ResponseEntity<AIP> updateAip(@PathVariable(name = "ip_id") String ipId,
                    @RequestBody @Valid AIP updated) {

                String gsonString = gson.toJson(updated);
                LOGGER.debug("==========>  UPDATE   ===> " + ipId + " =============" + gsonString);
                return new ResponseEntity<>(updated, HttpStatus.OK);
            }

        }
    }

    @Test
    public void createDataset() throws ModuleException, IOException {

        dataset1 = dsService.create(dataset1);
        LOGGER.info("===> create dataset1 (" + dataset1.getIpId() + ")");

        //        dataset2 = dsService.create(dataset2);
        //        LOGGER.info("===> create dataset1 (" + dataset2.getIpId() + ")");

        Assert.assertEquals(1, dsRepository.count());
    }

    @Test
    public void createDocument() throws ModuleException, IOException {

        document1 = docService.create(document1);
        LOGGER.info("===> create document (" + document1.getIpId() + ")");

        String fileLsUriTemplate = "file:///documents/files/" + DocumentLSService.FILE_CHECKSUM_URL_TEMPLATE;
        MockMultipartFile mockMultipartFile = new MockMultipartFile("document1.xml", "document1.xml", "doc/xml",
                "content of my file".getBytes());
        MockMultipartFile mockMultipartFile2 = new MockMultipartFile("document2.png", "document2.png", "image/png",
                "some pixels informations".getBytes());
        MultipartFile[] multipartFiles = { mockMultipartFile, mockMultipartFile2 };
        document1 = docService.addFiles(document1.getId(), multipartFiles, fileLsUriTemplate);

        Assert.assertEquals(1, docRepository.count());
    }

    @Test
    public void createCollection() throws ModuleException, IOException {

        collection1.setDescriptionFile(new DescriptionFile(new byte[0], MediaType.APPLICATION_PDF));
        final byte[] input = Files.readAllBytes(Paths.get("src", "test", "resources", "data", "test.md"));
        final MockMultipartFile multiPartFile = new MockMultipartFile("description-markdown-format", "test.pdf",
                MediaType.APPLICATION_PDF_VALUE, input);

        collection1 = colService.create(collection1, multiPartFile);
        LOGGER.info("===> create collection1 (" + collection1.getIpId() + ")");

        Assert.assertEquals(1, colRepository.count());
    }

    private PluginMetaData getPluginMetaData() {
        return PluginUtils.createPluginMetaData(FakeDataSourcePlugin.class,
                                                FakeDataSourcePlugin.class.getPackage().getName());
    }

    @Test
    public void updateDataset() throws ModuleException, IOException {

        dataset1 = dsService.create(dataset1);
        LOGGER.info("===> create dataset1 (" + dataset1.getIpId() + ")");

        dataset1.addQuotation("a new quotation");
        AbstractAttribute<?> attr2Delete = dataset1.getProperty("LONG_VALUE");
        dataset1.removeProperty(attr2Delete);

        Set<String> tags = dataset1.getTags();
        tags.remove(tags.iterator().next());
        dataset1.setTags(tags);

        dsService.update(dataset1);

        Assert.assertEquals(1, dsRepository.count());
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

    @Before
    public void init() throws ModuleException {
        cleanRepository();

        initDataset();

        initDocument();

        initCollection();
    }

    @After
    public void cleanAfter() {
        cleanRepository();
    }

    private void cleanRepository() {
        tenantResolver.forceTenant(TENANT);

        dsRepository.deleteAll();
        docLsRepository.deleteAll();
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

        dataset1 = new Dataset(modelDataset, TENANT, "dataset one label");
        dataset1.setLicence("the licence");
        dataset1.setSipId("SipId1");
        dataset1.setTags(Sets.newHashSet("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"));
        dataset1.addQuotation("hello");
        dataset1.addQuotation("coucou");
        dataset1.addQuotation("bonjour");
        dataset1.addQuotation("guten tag");
        dataset1.setScore(99);
        dataset1.setGeometry(new Geometry.MultiPoint(
                new Double[][] { { 41.12, -10.5 }, { 42., -72. }, { 15., -72. }, { 15., -9. } }));

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
    }

    private void initDocument() throws ModuleException {
        modelDocument = importModel(MODEL_DOCUMENT_FILE_NAME);

        document1 = new Document(modelDocument, TENANT, "My document label");
        document1.setTags(Sets.newHashSet("azertyui"));

        document1.addProperty(AttributeBuilder.buildDate("creation_date", OffsetDateTime.now().minusHours(452)));
        document1.addProperty(AttributeBuilder.buildStringArray("AUTHORS", "Jo", "John Doe", "Bill"));
        document1.addProperty(AttributeBuilder.buildString("TITLE", "the document title"));
    }

    private void initCollection() throws ModuleException {
        modelCollection = importModel(MODEL_COLLECTION_FILE_NAME);

        collection1 = new Collection(modelCollection, TENANT, "My collection label");
        collection1.setTags(Sets.newHashSet("COLLECTION-ONE", "COLLECTION-02", "COLLECTION-03"));

        //        try {
        //            URL baseStorageLocation = new URL("file", "",
        //                    System.getProperty("user.dir") + "/src/test/resources/data/test.md");
        //            DescriptionFile dFile = new DescriptionFile(baseStorageLocation.toString());
        //            dFile.setType(MediaType.TEXT_MARKDOWN);
        //            collection1.setDescriptionFile(dFile);
        //            
        //            
        //            collection1.setDescriptionFile(new DescriptionFile(new byte[0], MediaType.APPLICATION_PDF));
        //            final byte[] input = Files.readAllBytes(Paths.get("src", "test", "resources", "data", "test.md"));
        //            final MockMultipartFile pdf = new MockMultipartFile("file", "test.pdf", MediaType.APPLICATION_PDF_VALUE, input);
        //            
        //            
        //            
        //        } catch (IOException e) {
        //            // TODO Auto-generated catch block
        //            e.printStackTrace();
        //        }
        //
        collection1.addProperty(AttributeBuilder.buildDate("creation_date", OffsetDateTime.now().minusHours(452)));
    }

}
