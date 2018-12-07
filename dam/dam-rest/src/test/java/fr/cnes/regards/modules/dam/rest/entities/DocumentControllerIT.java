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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.framework.oais.urn.UniformResourceName;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.integration.RequestBuilderCustomizer;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.dam.dao.entities.ICollectionRepository;
import fr.cnes.regards.modules.dam.dao.entities.IDocumentRepository;
import fr.cnes.regards.modules.dam.dao.models.IModelRepository;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.entities.Document;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.rest.DamRestConfiguration;

/**
 * @author lmieulet
 */
@TestPropertySource(locations = { "classpath:test.properties" })
@MultitenantTransactional
@ContextConfiguration(classes = { DamRestConfiguration.class })
public class DocumentControllerIT extends AbstractRegardsTransactionalIT {

    private Model model1;

    private Model model2;

    private Collection collection1;

    private Collection collection2;

    private Document document1;

    private Document document2;

    @Autowired
    private IDocumentRepository documentRepository;

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private IModelRepository modelRepository;

    @Before
    public void initRepos() {

        // Bootstrap default values
        model1 = Model.build("documentModelName1", "model desc", EntityType.COLLECTION);
        model2 = Model.build("documentModelName2", "model desc", EntityType.DOCUMENT);
        model2 = modelRepository.save(model2);
        model1 = modelRepository.save(model1);

        collection1 = new Collection(model1, "PROJECT", "COL1", "collection1");
        collection1.setProviderId("ProviderId1");
        collection1.setLabel("label");
        collection1.setCreationDate(OffsetDateTime.now());

        collection2 = new Collection(model1, "PROJECT", "COL2", "collection2");
        collection2.setProviderId("ProviderId1");
        collection2.setLabel("label");
        collection2.setCreationDate(OffsetDateTime.now());

        document1 = new Document(model2, "PROJECT", "DOC1", "document1");
        document1.setProviderId("ProviderId2");
        document1.setLabel("label");
        document1.setCreationDate(OffsetDateTime.now());

        document2 = new Document(model2, "PROJECT", "DOC2", "document2");
        document2.setProviderId("ProviderId3");
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
        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        customizer.addExpectation(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
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

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isCreated());
        customizer.addExpectation(MockMvcResultMatchers.jsonPath(JSON_ID, Matchers.notNullValue()));
        performDefaultPost(DocumentController.TYPE_MAPPING, document3, customizer, "Failed to create a new document");
    }

    @Test
    public void testGetDocumentById() {
        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        customizer.addExpectation(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_MAPPING, customizer,
                          "Failed to fetch a specific document using its id", document1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_DOC_110")
    @Purpose("Shall delete the document")
    @Test
    public void testDeleteDocument() {
        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isNoContent());
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

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        customizer.addExpectation(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_MAPPING, documentClone,
                          customizer, "Failed to update a specific document using its id", document1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_DOC_230")
    @Purpose("Shall dissociate tag from the document")
    @Test
    public void testDissociateDocuments() {
        final List<UniformResourceName> toDissociate = new ArrayList<>();
        toDissociate.add(collection1.getIpId());

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isNoContent());
        performDefaultPut(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_DISSOCIATE_MAPPING,
                          toDissociate, customizer, "Failed to dissociate collections from one document using its id",
                          document2.getId());
    }

    @Requirement("REGARDS_DSL_DAM_DOC_230")
    @Purpose("Shall associate tag to the document")
    @Test
    public void testAssociateDocuments() {
        final List<UniformResourceName> toAssociate = new ArrayList<>();
        toAssociate.add(collection2.getIpId());

        RequestBuilderCustomizer customizer = getNewRequestBuilderCustomizer();
        customizer.addExpectation(MockMvcResultMatchers.status().isNoContent());
        performDefaultPut(DocumentController.TYPE_MAPPING + DocumentController.DOCUMENT_ASSOCIATE_MAPPING, toAssociate,
                          customizer, "Failed to associate collections from one document using its id",
                          document1.getId());
    }
}
