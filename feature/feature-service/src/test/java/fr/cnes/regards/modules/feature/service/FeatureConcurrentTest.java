package fr.cnes.regards.modules.feature.service;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureCreationSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.model.dto.properties.IProperty;

@TestPropertySource(
        properties = { "spring.jpa.properties.hibernate.default_schema=feature_cct", "regards.amqp.enabled=true" },
        locations = { "classpath:regards_perf.properties", "classpath:batch.properties" })
@ActiveProfiles(value = { "testAmqp", "noscheduler", "nohandler" })
public class FeatureConcurrentTest extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeatureConcurrentTest.class);

    @Autowired
    private IFeatureCreationService featureService;

    @Test
    public void createConcurrentFeatures() throws InterruptedException {

        // Register creation requests
        FeatureCreationSessionMetadata metadata = FeatureCreationSessionMetadata
                .build("sessionOwner", "session", PriorityLevel.NORMAL, Lists.emptyList(), true);
        String modelName = mockModelClient(GeodeProperties.getGeodeModel());

        Thread.sleep(5_000);

        List<FeatureCreationRequestEvent> events = new ArrayList<>();

        OffsetDateTime requestDate = OffsetDateTime.now();

        // First feature
        IGeometry firstPosition = IGeometry.point(0, 0);
        Feature feature = Feature.build("SAME ID", null, firstPosition, EntityType.DATA, modelName);
        GeodeProperties.addGeodeProperties(feature);
        events.add(FeatureCreationRequestEvent.build(metadata, feature, requestDate));

        // Second feature with same id
        IGeometry secondPosition = IGeometry.point(10, 10);
        feature = Feature.build("SAME ID", null, secondPosition, EntityType.DATA, modelName);
        GeodeProperties.addGeodeProperties(feature);
        events.add(FeatureCreationRequestEvent.build(metadata, feature, requestDate.minusSeconds(1)));

        // Third feature with same id
        IGeometry thirdPosition = IGeometry.point(22, 22);
        feature = Feature.build("SAME ID", null, thirdPosition, EntityType.DATA, modelName);
        GeodeProperties.addGeodeProperties(feature);
        events.add(FeatureCreationRequestEvent.build(metadata, feature, requestDate.minusSeconds(2)));

        saveEvents(events);

        featureService.scheduleRequests();
        waitFeature(1, null, 60_000);

        featureService.scheduleRequests();
        waitFeature(2, null, 60_000);

        featureService.scheduleRequests();
        waitFeature(3, null, 60_000);

        // Control request processing order (older to newer, look at request date)
        List<FeatureEntity> entities = featureRepo.findAll(Sort.by(Order.asc("version")));
        Assert.assertEquals(thirdPosition, entities.get(0).getFeature().getGeometry());
        Assert.assertEquals(secondPosition, entities.get(1).getFeature().getGeometry());
        Assert.assertEquals(firstPosition, entities.get(2).getFeature().getGeometry());
    }

    private void saveEvents(List<FeatureCreationRequestEvent> events) {
        long start = System.currentTimeMillis();
        LOGGER.info(">>>>>>>>>>>>>>>>> Registering {} requests", events.size());
        featureService.registerRequests(events);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} requests registered in {} ms", events.size(),
                    System.currentTimeMillis() - start);
    }

    @SuppressWarnings("unused")
    private void addDefaultProperties(Feature feature) {
        feature.addProperty(IProperty.buildString("data_type", "TYPE01"));
        feature.addProperty(IProperty.buildObject("file_characterization",
                                                  IProperty.buildBoolean("valid", Boolean.TRUE)));
    }
}
