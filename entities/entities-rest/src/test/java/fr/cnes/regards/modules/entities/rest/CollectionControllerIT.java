/*
 * LICENSE_PLACEHOLDER
 */
package fr.cnes.regards.modules.entities.rest;

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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.google.gson.GsonBuilder;

import fr.cnes.regards.framework.jpa.multitenant.transactional.MultitenantTransactional;
import fr.cnes.regards.framework.test.integration.AbstractRegardsTransactionalIT;
import fr.cnes.regards.framework.test.report.annotation.Purpose;
import fr.cnes.regards.framework.test.report.annotation.Requirement;
import fr.cnes.regards.modules.entities.dao.ICollectionRepository;
import fr.cnes.regards.modules.entities.domain.Collection;
import fr.cnes.regards.modules.entities.urn.UniformResourceName;
import fr.cnes.regards.modules.models.dao.IModelRepository;
import fr.cnes.regards.modules.models.domain.EntityType;
import fr.cnes.regards.modules.models.domain.Model;

/**
 * @author lmieulet
 * @author Sylvain Vissiere-Guerinet
 */
@TestPropertySource(locations = { "classpath:test.properties" })
@MultitenantTransactional
@ContextConfiguration(classes = { ControllerITConfig.class })
public class CollectionControllerIT extends AbstractRegardsTransactionalIT {

    /**
     *
     */
    private static final String COLLECTIONS_COLLECTION_ID = "/collections/{collection_id}";

    /**
     *
     */
    private static final String COLLECTIONS = "/collections";

    /**
    *
    */
    private static final String COLLECTIONS_COLLECTION_ID_ASSOCIATE = COLLECTIONS_COLLECTION_ID + "/associate";

    private static final String COLLECTIONS_COLLECTION_ID_DISSOCIATE = COLLECTIONS_COLLECTION_ID + "/dissociate";

    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(CollectionControllerIT.class);

    private Model model1;

    private Collection collection1;

    private Collection collection3;

    private Collection collection4;

    @Autowired
    private ICollectionRepository collectionRepository;

    @Autowired
    private IModelRepository modelRepository;

    @Autowired
    private GsonBuilder gsonBuilder;

    private List<ResultMatcher> expectations;

    @Before
    public void initRepos() {
        expectations = new ArrayList<>();
        // Bootstrap default values
        model1 = Model.build("modelName1", "model desc", EntityType.COLLECTION);
        model1 = modelRepository.save(model1);

        collection1 = new Collection(model1, "PROJECT", "collection1");
        collection1.setSipId("SipId1");
        collection1.setLabel("label");
        collection1.setCreationDate(OffsetDateTime.now());
        collection3 = new Collection(model1, "PROJECT", "collection3");
        collection3.setSipId("SipId3");
        collection3.setLabel("label");
        collection3.setCreationDate(OffsetDateTime.now());
        collection4 = new Collection(model1, "PROJECT", "collection4");
        collection4.setSipId("SipId4");
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

    // TODO: test retrieve Collection by (S)IP_ID, by modelId and sipId

    @Requirement("REGARDS_DSL_DAM_COL_510")
    @Purpose("Shall retrieve all collections")
    @Test
    public void testGetAllCollections() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(COLLECTIONS, expectations, "Failed to fetch collection list");
    }

    @Requirement("REGARDS_DSL_DAM_COL_010")
    @Requirement("REGARDS_DSL_DAM_COL_020")
    @Purpose("Shall create a new collection")
    @Test
    public void testPostCollection() {
        final Collection collection2 = new Collection(model1, null, "collection2");
        collection2.setCreationDate(OffsetDateTime.now());

        expectations.add(MockMvcResultMatchers.status().isCreated());

        final String collectionStr = gsonBuilder.create().toJson(collection2);
        final MockMultipartFile collection = new MockMultipartFile("collection", "", MediaType.APPLICATION_JSON_VALUE,
                collectionStr.getBytes());
        final List<MockMultipartFile> parts = new ArrayList<>();
        parts.add(collection);
        performDefaultFileUpload(COLLECTIONS, parts, expectations, "Failed to create a new collection");

    }

    // TODO add get by ip id
    @Requirement("REGARDS_DSL_DAM_COL_310")
    @Purpose("Shall retrieve a collection using its id")
    @Test
    public void testGetCollectionById() {
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultGet(COLLECTIONS_COLLECTION_ID, expectations, "Failed to fetch a specific collection using its id",
                          collection1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_210")
    @Purpose("Le système doit permettre de mettre à jour les valeurs d’une collection via son IP_ID et d’archiver ces "
            + "modifications dans son AIP au niveau du composant « Archival storage » si ce composant est déployé.")
    @Test
    public void testUpdateCollection() {
        final Collection collectionClone = new Collection(collection1.getModel(), "", "collection1clone");
        collectionClone.setIpId(collection1.getIpId());
        collectionClone.setCreationDate(collection1.getCreationDate());
        collectionClone.setId(collection1.getId());
        collectionClone.setTags(collection1.getTags());
        collectionClone.setSipId(collection1.getSipId() + "new");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));
        performDefaultPut(COLLECTIONS_COLLECTION_ID, collectionClone, expectations,
                          "Failed to update a specific collection using its id", collection1.getId());
    }

    @Requirement("REGARDS_DSL_DAM_COL_220")
    @Purpose("Le système doit permettre d’associer/dissocier des collections à la collection courante lors de la mise à jour.")
    @Test
    public void testFullUpdate() {
        final Collection collectionClone = new Collection(collection1.getModel(), "", "collection1clone");
        collectionClone.setIpId(collection1.getIpId());
        collectionClone.setCreationDate(collection1.getCreationDate());
        collectionClone.setId(collection1.getId());
        collectionClone.setSipId(collection1.getSipId() + "new");
        collectionClone.setLabel("label");
        expectations.add(MockMvcResultMatchers.status().isOk());
        expectations.add(MockMvcResultMatchers.content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE));

        performDefaultPut(COLLECTIONS_COLLECTION_ID, collectionClone, expectations,
                          "Failed to update a specific collection using its id", collection1.getId());

    }

    // TODO: add delete by ip id
    @Requirement("REGARDS_DSL_DAM_COL_110")
    @Purpose("Shall delete a collection")
    @Test
    public void testDeleteCollection() {
        expectations.add(MockMvcResultMatchers.status().isNoContent());
        performDefaultDelete(COLLECTIONS_COLLECTION_ID, expectations,
                             "Failed to delete a specific collection using its id", collection1.getId());
    }

    @Test
    public void testDissociateCollections() {
        final List<UniformResourceName> toDissociate = new ArrayList<>();
        toDissociate.add(collection3.getIpId());
        expectations.add(MockMvcResultMatchers.status().isOk());
        performDefaultPut(COLLECTIONS_COLLECTION_ID_DISSOCIATE, toDissociate, expectations,
                          "Failed to dissociate collections from one collection using its id", collection1.getId());
    }

    @Test
    public void testAssociateCollections() {
        final List<UniformResourceName> toAssociate = new ArrayList<>();
        toAssociate.add(collection4.getIpId());
        expectations.add(MockMvcResultMatchers.status().isOk());
        performDefaultPut(COLLECTIONS_COLLECTION_ID_ASSOCIATE, toAssociate, expectations,
                          "Failed to associate collections from one collection using its id", collection1.getId());
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }
}