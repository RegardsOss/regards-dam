package fr.cnes.regards.modules.feature.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.hateoas.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.util.MimeType;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.jpa.multitenant.test.AbstractMultitenantServiceTest;
import fr.cnes.regards.framework.oais.urn.DataType;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.dao.IFeatureCreationRequestRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureEntityRepository;
import fr.cnes.regards.modules.feature.dao.IFeatureUpdateRequestRepository;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureFile;
import fr.cnes.regards.modules.feature.dto.FeatureFileAttributes;
import fr.cnes.regards.modules.feature.dto.FeatureFileLocation;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.model.client.IModelAttrAssocClient;
import fr.cnes.regards.modules.model.domain.ModelAttrAssoc;
import fr.cnes.regards.modules.model.domain.attributes.AttributeModel;
import fr.cnes.regards.modules.model.gson.MultitenantFlattenedAttributeAdapterFactory;
import fr.cnes.regards.modules.model.service.exception.ImportException;
import fr.cnes.regards.modules.model.service.xml.IComputationPluginService;
import fr.cnes.regards.modules.model.service.xml.XmlImportHelper;

public abstract class AbstractFeatureMultitenantServiceTest extends AbstractMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractFeatureMultitenantServiceTest.class);

    // Mock for test purpose
    @Autowired
    private IComputationPluginService cps;

    @Autowired
    protected IModelAttrAssocClient modelAttrAssocClientMock;

    @Autowired
    private MultitenantFlattenedAttributeAdapterFactory factory;

    @Autowired
    protected IFeatureCreationRequestRepository featureCreationRequestRepo;

    @Autowired
    protected IFeatureEntityRepository featureRepo;

    @Autowired
    protected IFeatureUpdateRequestRepository featureUpdateRequestRepo;

    @Before
    public void before() throws InterruptedException {
        this.featureCreationRequestRepo.deleteAllInBatch();
        this.featureUpdateRequestRepo.deleteAllInBatch();
        this.featureRepo.deleteAllInBatch();
        simulateApplicationReadyEvent();
    }

    /**
     * Wait until feature are properly created
     * @param expected expected feature number
     * @param from feature updated after from date. May be <code>null</code>.
     * @param timeout timeout in milliseconds
     */
    protected void waitFeature(long expected, @Nullable OffsetDateTime from, long timeout) {
        long end = System.currentTimeMillis() + timeout;
        // Wait
        long entityCount;
        do {
            if (from != null) {
                entityCount = featureRepo.countByLastUpdateGreaterThan(from);
            } else {
                entityCount = featureRepo.count();
            }
            LOGGER.trace("{} feature(s) created in database", entityCount);
            if (entityCount == expected) {
                break;
            }
            long now = System.currentTimeMillis();
            if (end > now) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Assert.fail("Thread interrupted");
                }
            } else {
                Assert.fail("Timeout");
            }
        } while (true);
    }

    /**
     * Wait until feature creation request(s) are properly deleted
     * @param expected expected request number
     * @param timeout timeout in milliseconds
     */
    protected void waitCreationRequestDeletion(long expected, long timeout) {
        waitRequestDeletion(featureCreationRequestRepo, expected, timeout);
    }

    /**
     * Wait until feature update request(s) are properly deleted
     * @param expected expected request number
     * @param timeout timeout in milliseconds
     */
    protected void waitUpdateRequestDeletion(long expected, long timeout) {
        waitRequestDeletion(featureUpdateRequestRepo, expected, timeout);
    }

    private void waitRequestDeletion(JpaRepository<?, ?> repo, long expected, long timeout) {
        long end = System.currentTimeMillis() + timeout;
        // Wait
        long entityCount;
        do {
            entityCount = repo.count();
            LOGGER.trace("{} request(s) remain(s) in database", entityCount);
            if (entityCount == expected) {
                break;
            }
            long now = System.currentTimeMillis();
            if (end > now) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Assert.fail("Thread interrupted");
                }
            } else {
                Assert.fail("Timeout");
            }
        } while (true);
    }

    public String mockModelClient(String filename) {
        return mockModelClient(filename, cps, factory, getDefaultTenant(), modelAttrAssocClientMock);
    }

    /**
     * Mock model client importing model specified by its filename
     * @param filename model filename found using {@link Class#getResourceAsStream(String)}
     * @return mocked model name
     */
    public String mockModelClient(String filename, IComputationPluginService cps,
            MultitenantFlattenedAttributeAdapterFactory factory, String tenant,
            IModelAttrAssocClient modelAttrAssocClientMock) {

        try (InputStream input = this.getClass().getResourceAsStream(filename)) {
            // Import model
            Iterable<ModelAttrAssoc> assocs = XmlImportHelper.importModel(input, cps);

            // Translate to resources and attribute models and extract model name
            String modelName = null;
            List<AttributeModel> atts = new ArrayList<>();
            List<Resource<ModelAttrAssoc>> resources = new ArrayList<>();
            for (ModelAttrAssoc assoc : assocs) {
                atts.add(assoc.getAttribute());
                resources.add(new Resource<ModelAttrAssoc>(assoc));
                if (modelName == null) {
                    modelName = assoc.getModel().getName();
                }
            }

            // Property factory registration
            factory.registerAttributes(tenant, atts);

            // Mock client
            Mockito.when(modelAttrAssocClientMock.getModelAttrAssocs(modelName))
                    .thenReturn(ResponseEntity.ok(resources));

            return modelName;
        } catch (IOException | ImportException e) {
            String errorMessage = "Cannot import model";
            LOGGER.debug(errorMessage);
            throw new AssertionError(errorMessage);
        }
    }

    protected void initFeatureCreationRequestEvent(List<FeatureCreationRequestEvent> events,
            int featureNumberToCreate) {
        FeatureCreationRequestEvent toAdd;
        Feature featureToAdd;
        FeatureFile file;
        // create events to publish
        for (int i = 0; i < featureNumberToCreate; i++) {
            file = FeatureFile.build(
                                     FeatureFileAttributes.build(DataType.DESCRIPTION, new MimeType("mime"), "toto",
                                                                 1024l, "MD5", "checksum"),
                                     FeatureFileLocation.build("www.google.com", "GPFS"));
            featureToAdd = Feature
                    .build("id" + i, null, IGeometry.point(IGeometry.position(10.0, 20.0)), EntityType.DATA, "model")
                    .withFiles(file);
            toAdd = FeatureCreationRequestEvent
                    .build(FeatureSessionMetadata.build("owner", "session", PriorityLevel.AVERAGE, Lists.emptyList()),
                           featureToAdd);
            toAdd.setRequestId(String.valueOf(i));
            toAdd.setFeature(featureToAdd);
            toAdd.setRequestDate(OffsetDateTime.now());
            events.add(toAdd);
        }
    }

    public IComputationPluginService getCps() {
        return cps;
    }

    public IModelAttrAssocClient getModelAttrAssocClientMock() {
        return modelAttrAssocClientMock;
    }

    public MultitenantFlattenedAttributeAdapterFactory getFactory() {
        return factory;
    }

}
