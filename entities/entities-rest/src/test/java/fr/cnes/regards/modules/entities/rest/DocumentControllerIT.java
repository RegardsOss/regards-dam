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
package fr.cnes.regards.modules.entities.rest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.gson.GsonBuilder;
import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.dao.IDocumentLSRepository;
import fr.cnes.regards.modules.entities.dao.IDocumentRepository;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.domain.Document;
import fr.cnes.regards.modules.entities.service.IDocumentService;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 */
@TestPropertySource(locations = {"classpath:test.properties"})
@MultitenantTransactional
@ContextConfiguration(classes = {ControllerITConfig.class})
public class DocumentControllerIT extends AbstractRegardsTransactionalIT {

    /**
     *
     */
    private static final String DOCUMENTS = "/documents";

    private static final String DOCUMENTS_DOCUMENT_IS_READY_TO_HANDLE_FILES = DOCUMENTS + "/isReadyToHandleFile";

    /**
     *
     */
    private static final String DOCUMENTS_DOCUMENT_ID = DOCUMENTS + "/{document_id}";

    /**
     *
     */
    private static final String DOCUMENTS_DOCUMENT_ID_ASSOCIATE = DOCUMENTS_DOCUMENT_ID + "/associate";

    private static final String DOCUMENTS_DOCUMENT_ID_DISSOCIATE = DOCUMENTS_DOCUMENT_ID + "/dissociate";


    private static final String DOCUMENTS_DOCUMENT_ID_FILES = DOCUMENTS_DOCUMENT_ID + "/files";


    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(DocumentControllerIT.class);

    private Model model1;

    private Model model2;

    private Collection collection1;

    private Collection collection2;

    private Document document1;

    private Document document2;

    private Document document3;

    @Autowired
    private IDocumentRepository documentRepository;

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private GsonBuilder gsonBuilder;

    private List<ResultMatcher> expectations;

    @Autowired
    private IDocumentService documentService;

    @Autowired
    private IDocumentLSRepository documentLSRepository;

    @Before
    public void initRepos() {
        expectations = new ArrayList<>();
        // Bootstrap default values
        model1 = Model.build("documentModelName1", "model desc", EntityType.COLLECTION);
        model2 = Model.build("documentModelName2", "model desc", EntityType.DOCUMENT);
        model2 = modelRepository.save(model2);
        model1 = modelRepository.save(model1);

        collection1 = new Collection(model1, "PROJECT", "collection1");
        collection1.setSipId("SipId1");
        collection1.setLabel("label");
        collection1.setCreationDate(OffsetDateTime.now());

        collection2 = new Collection(model1, "PROJECT", "collection2");
        collection2.setSipId("SipId1");
        collection2.setLabel("label");
        collection2.setCreationDate(OffsetDateTime.now());

        document1 = new Document(model2, "PROJECT", "document1");
        document1.setSipId("SipId2");
        document1.setLabel("label");
        document1.setCreationDate(OffsetDateTime.now());

        document2 = new Document(model2, "PROJECT", "document2");
        document2.setSipId("SipId3");
        document2.setLabel("label");
        document2.setCreationDate(OffsetDateTime.now());
        final Set<String> doc2Tags = new HashSet<>();
        doc2Tags.add(collection1.getIpId().toString());
        document2.setTags(doc2Tags);

        collection1 = collectionRepository.save(collection1);
        collection2 = collectionRepository.save(collection2);
        document1 = documentRepository.save(document1);
        document2 = documentRepository.save(document2);
    }


    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all documents")
    @Test
    public void testGetAllDocuments() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DOCUMENTS, expectations, "Failed to fetch document list");
    }


    @Requirement("REGARDS_DSL_DAM_DOC_010")
    @Purpose("Shall create a new document")
    @Test
    public void testPostDocument() {
        final Document document3 = new Document(model1, null, "document3");
        document3.setSipId("SipId3");
        document3.setLabel("label");
        document3.setCreationDate(OffsetDateTime.now());

        expectations.add(MockMvcResultMatchers.status().isCreated());
        expectations.add(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));

        performDefaultPost(DOCUMENTS, document3, expectations, "Failed to create a new document");
    }


    @Test
    public void testGetDocumentById() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DOCUMENTS_DOCUMENT_ID, expectations,
                "Failed to fetch a specific document using its id", document1.getId());
    }


    @Requirement("REGARDS_DSL_DAM_DOC_110")
    @Purpose("Shall delete the document")
    @Test
    public void testDeleteDocument() {
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(DOCUMENTS_DOCUMENT_ID, expectations,
                "Failed to delete a specific document using its id", document1.getId());
    }


    @Requirement("REGARDS_DSL_DAM_DOC_210")
    @Test
    public void testUpdateDocument() {
        final Document documentClone = new Document(document1.getModel(), "", "document1clone");
        documentClone.setIpId(document1.getIpId());
        documentClone.setCreationDate(document1.getCreationDate());
        documentClone.setId(document1.getId());
        documentClone.setTags(document1.getTags());
        documentClone.setSipId(document1.getSipId() + "new");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(DOCUMENTS_DOCUMENT_ID, documentClone, expectations,
                "Failed to update a specific document using its id", document1.getId());
    }


    @Requirement("REGARDS_DSL_DAM_DOC_230")
    @Purpose("Shall dissociate tag from the document")
    @Test
    public void testDissociateDocuments() {
        final List<UniformResourceName> toDissociate = new ArrayList<>();
        toDissociate.add(collection1.getIpId());
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultPut(DOCUMENTS_DOCUMENT_ID_DISSOCIATE, toDissociate, expectations,
                "Failed to dissociate collections from one document using its id", document2.getId());
    }


    @Requirement("REGARDS_DSL_DAM_DOC_230")
    @Purpose("Shall associate tag to the document")
    @Test
    public void testAssociateDocuments() {
        final List<UniformResourceName> toAssociate = new ArrayList<>();
        toAssociate.add(collection2.getIpId());
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultPut(DOCUMENTS_DOCUMENT_ID_ASSOCIATE, toAssociate, expectations,
                "Failed to associate collections from one document using its id", document1.getId());
    }

    @Requirement("TODO")
    @Purpose("Shall associate and dissociate files to the document")
    @Test
    public void testAddAndDeleteFiles() throws IOException, ModuleException {
        expectations.add(MockMvcResultMatchers.status().isOk());
        final byte[] input = Files.readAllBytes(Paths.get("src", "test", "resources", "test.pdf"));

        List<MockMultipartFile> pFileList = new ArrayList<>();
        pFileList.add(new MockMultipartFile("files", "test.pdf", MediaType.APPLICATION_PDF_VALUE, input));
        pFileList.add(new MockMultipartFile("files", "other-file-name.data", "text/plain", "some other type".getBytes()));
        // Upload files
        performDefaultFileUpload(DOCUMENTS_DOCUMENT_ID_FILES, pFileList, expectations,
                                 "Failed to upload files to a document", document1.getId());

        // Check if everything is ok
        document1 = documentRepository.findById(document1.getId());
        Assert.assertEquals(document1.getDocumentFiles().size(), 2);
        Assert.assertEquals(documentLSRepository.findAll().size(), 2);
        Optional<DataFile> first = document1.getDocumentFiles().stream().findFirst();
        DataFile dataFile = first.get();

        expectations.clear();
        expectations.add(MockMvcResultMatchers.status().isOk());
        // Upload files
        performDefaultDelete(DocumentController.ROOT_MAPPING + DocumentController.DOCUMENT_FILES_SINGLE_MAPPING, expectations,
                "Failed to remove a file from a document", document1.getId(), dataFile.getChecksum());

        // Check if everything is ok
        document1 = documentRepository.findById(document1.getId());
        Assert.assertEquals(document1.getDocumentFiles().size(), 1);
        Assert.assertEquals(documentLSRepository.findAll().size(), 1);
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}
