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

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import fr.cnes.regards.modules.dam.dao.models.IModelRepository;
import fr.cnes.regards.modules.dam.domain.entities.Collection;
import fr.cnes.regards.modules.dam.domain.models.Model;
import fr.cnes.regards.modules.dam.rest.DamRestConfiguration;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(locations = { "classpath:test.properties" })
@MultitenantTransactional
@ContextConfiguration(classes = { DamRestConfiguration.class })
public class CollectionControllerIT extends AbstractRegardsTransactionalIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionControllerIT.class);

    private Model model1;

    private Collection collection1;

    private Collection collection3;

    private Collection collection4;

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private IModelRepository modelRepository;

    private RequestBuilderCustomizer customizer;

    @Before
    public void initRepos() {
        customizer = getNewRequestBuilderCustomizer();
        // Bootstrap default values
        model1 = Model.build("modelName1", "model desc", EntityType.COLLECTION);
        model1 = modelRepository.save(model1);

        collection1 = new Collection(model1, "PROJECT", "COL1", "collection1");
        collection1.setProviderId("ProviderId1");
        collection1.setLabel("label");
        collection1.setCreationDate(OffsetDateTime.now());
        collection3 = new Collection(model1, "PROJECT", "COL3", "collection3");
        collection3.setProviderId("ProviderId3");
        collection3.setLabel("label");
        collection3.setCreationDate(OffsetDateTime.now());
        collection4 = new Collection(model1, "PROJECT", "COL4", "collection4");
        collection4.setProviderId("ProviderId4");
        collection4.setLabel("label");
        collection4.setCreationDate(OffsetDateTime.now());
        final Set<String> col1Tags = new HashSet<>();
        final Set<String> col4Tags = new HashSet<>();
        col1Tags.add(collection4.getIpId().toString());
        col4Tags.add(collection1.getIpId().toString());
        collection1.setTags(col1Tags);
        collection4.setTags(col4Tags);

        collection1 = collectionRepository.save(collection1);
        collection3 = collectionRepository.save(collection3);
    }

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections")
    @Test
    public void testGetAllCollections() {
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        customizer.addExpectation(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(CollectionController.TYPE_MAPPING, customizer, "Failed to fetch collection list");
    }

    @Requirement("REGARDS_DSL_DAM_COL_010")
    @Requirement("REGARDS_DSL_DAM_COL_020")
    @Purpose("Shall create a new collection")
    @Test
    public void testPostCollection() {
        Collection collection2 = new Collection(model1, null, "COL2", "collection2");
        collection2.setCreationDate(OffsetDateTime.now());
        customizer.addExpectation(MockMvcResultMatchers.status().isCreated());
        performDefaultPost(CollectionController.TYPE_MAPPING, collection2, customizer,
                           "Failed to create a new collection");
    }

    @Requirement("REGARDS_DSL_DAM_COL_310")
    @Purpose("Shall retrieve a collection using its id")
    @Test
    public void testGetCollectionById() {
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        customizer.addExpectation(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING, customizer,
                          "Failed to fetch a specific collection using its id", collection1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_210")
    @Purpose("Le système doit permettre de mettre à jour les valeurs d’une collection via son IP_ID et d’archiver ces "
            + "modifications dans son AIP au niveau du composant « Archival storage » si ce composant est déployé.")
    @Test
    public void testUpdateCollection() {
        Collection collectionClone = new Collection(collection1.getModel(), "", "COL1", "collection1clone");
        collectionClone.setIpId(collection1.getIpId());
        collectionClone.setCreationDate(collection1.getCreationDate());
        collectionClone.setId(collection1.getId());
        collectionClone.setTags(collection1.getTags());
        collectionClone.setProviderId(collection1.getProviderId() + "new");
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        customizer.addExpectation(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING, collectionClone,
                          customizer, "Failed to update a specific collection using its id", collection1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_220")
    @Purpose("Le système doit permettre d’associer/dissocier des collections à la collection courante lors de la mise à jour.")
    @Test
    public void testFullUpdate() {
        Collection collectionClone = new Collection(collection1.getModel(), "", "COL1", "collection1clone");
        collectionClone.setIpId(collection1.getIpId());
        collectionClone.setCreationDate(collection1.getCreationDate());
        collectionClone.setId(collection1.getId());
        collectionClone.setProviderId(collection1.getProviderId() + "new");
        collectionClone.setLabel("label");
        customizer.addExpectation(MockMvcResultMatchers.status().isOk());
        customizer.addExpectation(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING, collectionClone,
                          customizer, "Failed to update a specific collection using its id", collection1.getId());

    }

    @Requirement("REGARDS_DSL_DAM_COL_110")
    @Purpose("Shall delete a collection")
    @Test
    public void testDeleteCollection() {
        customizer.addExpectation(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_MAPPING, customizer,
                             "Failed to delete a specific collection using its id", collection1.getId());
    }

    @Test
    public void testDissociateCollections() {
        final List<UniformResourceName> toDissociate = new ArrayList<>();
        toDissociate.add(collection3.getIpId());
        customizer.addExpectation(MockMvcResultMatchers.status().isNoContent());
        performDefaultPut(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_DISSOCIATE_MAPPING,
                          toDissociate, customizer, "Failed to dissociate collections from one collection using its id",
                          collection1.getId());
    }

    @Test
    public void testAssociateCollections() {
        final List<UniformResourceName> toAssociate = new ArrayList<>();
        toAssociate.add(collection4.getIpId());
        customizer.addExpectation(MockMvcResultMatchers.status().isNoContent());
        performDefaultPut(CollectionController.TYPE_MAPPING + CollectionController.COLLECTION_ASSOCIATE_MAPPING,
                          toAssociate, customizer, "Failed to associate collections from one collection using its id",
                          collection1.getId());
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }
}
