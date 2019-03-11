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
package fr.cnes.regards.modules.dam.rest.entities;

import java.time.OffsetDateTime;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.domain.entities.Document;
import fr.cnes.regards.modules.dam.rest.DamRestConfiguration;

/**
 * @author lmieulet
 */
@TestPropertySource(locations = { "classpath:test.properties" },
        properties = { "spring.jpa.properties.hibernate.default_schema=dam_doc_test" })
@ContextConfiguration(classes = { DamRestConfiguration.class })
public class DocumentControllerIT extends AbstractDocumentControllerIT {

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all documents")
    @Test
    public void testGetAllDocuments() {
        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expectContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        performDefaultGet(DocumentController.TYPE_MAPPING, customizer, "Failed to fetch document list");
    }

    @Requirement("REGARDS_DSL_DAM_DOC_010")
    @Purpose("Shall create a new document")
    @Test
    public void testPostDocument() {
        final Document document3 = new Document(model1, null, "DOC3", "document3");
        document3.setProviderId("ProviderId3");
        document3.setLabel("label");
        document3.setCreationDate(OffsetDateTime.now());

        RequestBuilderCustomizer customizer = customizer().expectStatusCreated()
                .expect(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        performDefaultPost(DocumentController.TYPE_MAPPING, document3, customizer, "Failed to create a new document");
    }

    @Test
    public void testGetDocumentById() {
        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expectContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
        performDefaultGet(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_MAPPING, customizer,
                          "Failed to fetch a specific document using its id", document1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_DOC_110")
    @Purpose("Shall delete the document")
    @Test
    public void testDeleteDocument() {
        RequestBuilderCustomizer customizer = customizer().expectStatusNoContent();
        performDefaultDelete(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_MAPPING, customizer,
                             "Failed to delete a specific document using its id", document1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_DOC_210")
    @Test
    public void testUpdateDocument() {
        final Document documentClone = new Document(document1.getModel(), "", "DOC1CLONE", "document1clone");
        documentClone.setIpId(document1.getIpId());
        documentClone.setCreationDate(document1.getCreationDate());
        documentClone.setId(document1.getId());
        documentClone.setProviderId(document1.getProviderId() + "new");

        RequestBuilderCustomizer customizer = customizer().expectStatusOk()
                .expectContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);

        performDefaultPut(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_MAPPING, documentClone,
                          customizer, "Failed to update a specific document using its id", document1.getId());
    }
}
