package fr.cnes.regards.modules.feature.service;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureUpdateRequestEvent;
import fr.cnes.regards.modules.feature.dto.urn.FeatureIdentifier;
import fr.cnes.regards.modules.feature.dto.urn.FeatureUniformResourceName;

@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=feature_uperf", "regards.amqp.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties" })
@ActiveProfiles(value = { "testAmqp", "noscheduler", "nohandler" })
@DirtiesContext
public class FeatureUpdatePerformanceTest extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureUpdatePerformanceTest.class);

    private static final Integer NB_FEATURES = 10000;

    private static final String PROVIDER_ID_FORMAT = "F%05d";

    @Autowired
    private IFeatureUpdateService featureService;

    /**
     * Test creation of EVENTS_NUMBER features Check if
     * {@link FeatureCreationRequest} and {@link FeatureEntity}are stored in
     * database then at the end of the job test if all
     * {@link FeatureCreationRequest} are deleted
     *
     * @throws InterruptedException
     */
    @Test
    public void createFeatures() throws InterruptedException {

        // Register creation requests
        FeatureSessionMetadata metadata = FeatureSessionMetadata.build("sessionOwner", "session", PriorityLevel.NORMAL,
                                                                       Lists.emptyList());
        String modelName = mockModelClient(GeodeProperties.getGeodeModel());

        Thread.sleep(5_000);

        // Register referenced features
        Map<String, FeatureUniformResourceName> refs = savePreviousVersions(modelName);

        long start = System.currentTimeMillis();

        List<FeatureUpdateRequestEvent> events = new ArrayList<>();
        OffsetDateTime requestDate = OffsetDateTime.now();
        int bulk = 0;
        for (int i = 1; i <= NB_FEATURES; i++) {
            bulk++;
            String id = String.format(PROVIDER_ID_FORMAT, i);
            Feature feature = Feature.build(id, refs.get(id), IGeometry.unlocated(), EntityType.DATA, modelName);
            GeodeProperties.addGeodeUpdateProperties(feature);
            events.add(FeatureUpdateRequestEvent.build(metadata, feature, requestDate));

            if (bulk == properties.getMaxBulkSize()) {
                saveEvents(events);
                events.clear();
                bulk = 0;
            }
        }

        if (bulk > 0) {
            saveEvents(events);
        }

        LOGGER.info(">>>>>>>>>>>>>>>>> {} requests registered in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - start);

        assertEquals(NB_FEATURES.longValue(), this.featureUpdateRequestRepo.count());

        // wait ...
        Thread.sleep(properties.getDelayBeforeProcessing() * 1000);

        boolean schedule;
        do {
            schedule = featureService.scheduleRequests() > 0;
        } while (schedule);

        waitFeature(NB_FEATURES, requestDate, 3600_000);

        LOGGER.info(">>>>>>>>>>>>>>>>> {} requests processed in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - start);

        assertEquals(NB_FEATURES.longValue(), this.featureRepo.count());
    }

    /**
     * Save features to update
     */
    private Map<String, FeatureUniformResourceName> savePreviousVersions(String modelName) {

        long start = System.currentTimeMillis();
        Map<String, FeatureUniformResourceName> urns = new HashMap<>();

        List<FeatureEntity> refEntities = new ArrayList<>();
        int bulk = 0;
        for (int i = 1; i <= NB_FEATURES; i++) {
            bulk++;
            String id = String.format(PROVIDER_ID_FORMAT, i);
            Feature feature = Feature.build(id, null, IGeometry.unlocated(), EntityType.DATA, modelName);
            UUID uuid = UUID.nameUUIDFromBytes(feature.getId().getBytes());
            feature.setUrn(FeatureUniformResourceName.build(FeatureIdentifier.FEATURE, feature.getEntityType(),
                                                            runtimeTenantResolver.getTenant(), uuid, 1));
            GeodeProperties.addGeodeProperties(feature);
            // Keep track of urn for further update
            urns.put(feature.getId(), feature.getUrn());
            refEntities.add(FeatureEntity.build("sessionOwner", "session", feature, null));

            if (bulk == properties.getMaxBulkSize()) {
                featureRepo.saveAll(refEntities);
                refEntities.clear();
                bulk = 0;
            }
        }

        if (bulk > 0) {
            featureRepo.saveAll(refEntities);
        }

        LOGGER.info(">>>>>>>>>>>>>>>>> {} features registered in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - start);
        return urns;
    }

    private void saveEvents(List<FeatureUpdateRequestEvent> events) {
        long start = System.currentTimeMillis();
        LOGGER.info(">>>>>>>>>>>>>>>>> Registering {} requests", events.size());
        featureService.registerRequests(events);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} requests registered in {} ms", events.size(),
                    System.currentTimeMillis() - start);
    }

}
