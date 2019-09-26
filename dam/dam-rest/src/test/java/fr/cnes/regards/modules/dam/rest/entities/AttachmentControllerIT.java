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
package fr.cnes.regards.modules.dam.rest.entities;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.matchers.JsonPathMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.module.rest.exception.ModuleException;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.rest.entities.dto.DataFileReference;
import fr.cnes.regards.modules.dam.service.entities.ICollectionService;
import fr.cnes.regards.modules.dam.service.models.IModelService;
import fr.cnes.regards.modules.indexer.domain.DataFile;
import fr.cnes.regards.modules.model.domain.Model;

/**
 * Test entity attachments processing
 *
 * @author Marc Sordi
 *
 */
@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=attachment",
        "regards.dam.local_storage.path=target/store" })
@MultitenantTransactional
public class AttachmentControllerIT extends AbstractRegardsTransactionalIT {

    private static final Path ATTACHMENT_FOLDER = Paths.get("src", "test", "resources", "attachments");

    private static final String PDF_CONTENT_TYPE = MediaType.APPLICATION_PDF_VALUE + " ;charset="
            + StandardCharsets.UTF_8.toString();

    private static final String HTML_CONTENT_TYPE = MediaType.TEXT_HTML_VALUE + " ;charset="
            + StandardCharsets.UTF_8.toString();

    @Autowired
    private IModelService modelService;

    @Autowired
    private ICollectionService collectionService;

    private Collection collection;

    @Before
    public void init() throws ModuleException {

        // Create a collection model
        Model collectionModel = Model.build("MODEL", "Empty model for testing", EntityType.COLLECTION);
        modelService.createModel(collectionModel);

        // Create a collection
        collection = new Collection(collectionModel, getDefaultTenant(), "COL1", "Collection label");
        collectionService.create(collection);
    }

    private MockMultipartFile getMultipartFile(String originalFilename, String contentType) throws IOException {
        return getMultipartFile(originalFilename, originalFilename, contentType);
    }

    private MockMultipartFile getMultipartFile(String filename, String originalFilename, String contentType)
            throws IOException {
        Path filePath = ATTACHMENT_FOLDER.resolve(filename);
        MockMultipartFile file = new MockMultipartFile("file", originalFilename, contentType,
                Files.newInputStream(filePath));
        return file;
    }

    private MockMultipartFile getMultipartFileRefs(DataFileReference... refs) throws IOException {
        Assert.assertNotNull(refs);
        String contentAsString = gson(Arrays.asList(refs));
        MockMultipartFile file = new MockMultipartFile("refs", "", MediaType.APPLICATION_JSON_VALUE,
                contentAsString.getBytes());
        return file;
    }

    @Test
    public void attachDescription() throws IOException {

        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expect(MockMvcResultMatchers
                        .jsonPath("$.content.feature.files." + DataType.DESCRIPTION.toString() + ".length()",
                                  Matchers.equalTo(2)));

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(getMultipartFile("description.pdf", PDF_CONTENT_TYPE));
        files.add(getMultipartFile("description2.pdf", PDF_CONTENT_TYPE));

        performDefaultFileUpload(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENTS_MAPPING, files,
                                 customizer, "Attachment error", collection.getIpId().toString(), DataType.DESCRIPTION);
    }

    @Test
    public void attachUrlDescription() throws IOException {

        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expect(MockMvcResultMatchers
                        .jsonPath("$.content.feature.files." + DataType.DESCRIPTION.toString() + ".length()",
                                  Matchers.equalTo(1)));

        List<MockMultipartFile> files = new ArrayList<>();

        // Create description reference
        DataFileReference ref = new DataFileReference();
        ref.setMimeType(MediaType.parseMediaType(HTML_CONTENT_TYPE));
        ref.setUri("https://tools.ietf.org/html/rfc7946");
        ref.setFilename("rfc7946");

        files.add(getMultipartFileRefs(ref));

        performDefaultFileUpload(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENTS_MAPPING, files,
                                 customizer, "Attachment error", collection.getIpId().toString(), DataType.DESCRIPTION);
    }

    @Test
    public void attachGetAndRemoveDescription() throws IOException {

        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expect(MockMvcResultMatchers
                        .jsonPath("$.content.feature.files." + DataType.DESCRIPTION.toString() + ".length()",
                                  Matchers.equalTo(1)));

        List<MockMultipartFile> files = new ArrayList<>();

        // Create description reference
        DataFileReference ref = new DataFileReference();
        ref.setMimeType(MediaType.parseMediaType(PDF_CONTENT_TYPE));
        ref.setUri("https://public.ccsds.org/pubs/650x0m2.pdf");
        ref.setFilename("650x0m2.pdf");

        files.add(getMultipartFileRefs(ref));

        ResultActions result = performDefaultFileUpload(AttachmentController.TYPE_MAPPING
                + AttachmentController.ATTACHMENTS_MAPPING, files, customizer, "Attachment error",
                                                        collection.getIpId().toString(), DataType.DESCRIPTION);

        String json = payload(result);
        String checksum = JsonPath.read(json,
                                        "$.content.feature.files." + DataType.DESCRIPTION.toString() + "[0].checksum");
        Assert.assertNotNull(checksum);

        // Get it
        customizer = customizer().expectStatus(HttpStatus.UNPROCESSABLE_ENTITY);
        performDefaultGet(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENT_MAPPING, customizer,
                          "Download error", collection.getIpId().toString(), checksum);

        // Remove it
        customizer = customizer().expectStatusOk();

        result = performDefaultDelete(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENT_MAPPING,
                                      customizer, "Remove error", collection.getIpId().toString(), checksum);
        json = payload(result);
        Assert.assertThat("Description must be removed",
                          JsonPathMatchers.hasNoJsonPath("$.content.feature.files." + DataType.DESCRIPTION.toString()));

    }

    @Test
    public void attachRefAndNormalDescription() throws IOException {

        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expect(MockMvcResultMatchers
                        .jsonPath("$.content.feature.files." + DataType.DESCRIPTION.toString() + ".length()",
                                  Matchers.equalTo(2)));

        List<MockMultipartFile> files = new ArrayList<>();

        // Create description reference
        DataFileReference ref = new DataFileReference();
        ref.setMimeType(MediaType.parseMediaType(PDF_CONTENT_TYPE));
        ref.setUri("https://public.ccsds.org/pubs/650x0m2.pdf");
        ref.setFilename("650x0m2.pdf");
        files.add(getMultipartFileRefs(ref));

        // Create normal description
        files.add(getMultipartFile("description.pdf", PDF_CONTENT_TYPE));

        performDefaultFileUpload(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENTS_MAPPING, files,
                                 customizer, "Attachment error", collection.getIpId().toString(), DataType.DESCRIPTION);
    }

    @Test
    public void attachDescriptionWithoutName() throws IOException {
        RequestBuilderCustomizer customizer = customizer().expectStatus(HttpStatus.UNPROCESSABLE_ENTITY);

        String pdfContentType = MediaType.APPLICATION_PDF_VALUE + " ;charset=" + StandardCharsets.UTF_8.toString();

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(getMultipartFile("description.pdf", "", pdfContentType));

        performDefaultFileUpload(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENTS_MAPPING, files,
                                 customizer, "Attachment error", collection.getIpId().toString(), DataType.DESCRIPTION);
    }

    @Test
    public void attachDescriptionWithBadContentType() throws IOException {
        RequestBuilderCustomizer customizer = customizer().expectStatus(HttpStatus.UNPROCESSABLE_ENTITY);

        String pdfContentType = MediaType.APPLICATION_ATOM_XML_VALUE + " ;charset=" + StandardCharsets.UTF_8.toString();

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(getMultipartFile("description.pdf", pdfContentType));

        performDefaultFileUpload(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENTS_MAPPING, files,
                                 customizer, "Attachment error", collection.getIpId().toString(), DataType.DESCRIPTION);
    }

    private void uploadDocument() throws IOException {
        // Upload document
        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expect(MockMvcResultMatchers
                        .jsonPath("$.content.feature.files." + DataType.DOCUMENT.toString() + ".length()",
                                  Matchers.equalTo(1)));

        List<MockMultipartFile> files = new ArrayList<>();
        files.add(getMultipartFile("description.docx", MediaType.APPLICATION_OCTET_STREAM_VALUE));

        performDefaultFileUpload(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENTS_MAPPING, files,
                                 customizer, "Attachment error", collection.getIpId().toString(), DataType.DOCUMENT);
    }

    @Test
    public void attachDocument() throws IOException {

        uploadDocument();

        // Download document
        DataFile dataFile = collection.getFiles().get(DataType.DOCUMENT).stream().findFirst().get();

        RequestBuilderCustomizer customizer = customizer().expectStatusOk();
        performDefaultGet(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENT_MAPPING, customizer,
                          "Download error", collection.getIpId().toString(), dataFile.getChecksum());
    }

    @Test
    public void removeDocument() throws IOException {

        uploadDocument();

        // Remove document
        DataFile dataFile = collection.getFiles().get(DataType.DOCUMENT).stream().findFirst().get();

        RequestBuilderCustomizer customizer = customizer().expectStatusOk();
        performDefaultDelete(AttachmentController.TYPE_MAPPING + AttachmentController.ATTACHMENT_MAPPING, customizer,
                             "Remove error", collection.getIpId().toString(), dataFile.getChecksum());
    }

}
