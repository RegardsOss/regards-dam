package fr.cnes.regards.modules.feature.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.google.common.collect.ArrayListMultimap;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureMetadata;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.model.dto.properties.IProperty;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_perf",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles(value = { "testAmqp", "noscheduler", "nohandler" })
public class FeaturePerformanceTest extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeaturePerformanceTest.class);

    private static final Integer NB_FEATURES = 128;

    @Autowired
    private IFeatureCreationService featureService;

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

        String format = "F%05d";

        // Register creation requests
        FeatureMetadata metadata = FeatureMetadata.build("sessionOwner", "session", Lists.emptyList());
        String modelName = mockModelClient("feature_mutation_model.xml");

        Thread.sleep(5_000);

        long creationStart = System.currentTimeMillis();

        List<FeatureCreationRequestEvent> events = new ArrayList<>();
        for (int i = 1; i <= NB_FEATURES; i++) {
            String id = String.format(format, i);
            Feature feature = Feature.build(id, null, IGeometry.unlocated(), EntityType.DATA, modelName);
            feature.addProperty(IProperty.buildString("data_type", "TYPE01"));
            feature.addProperty(IProperty.buildObject("file_characterization",
                                                      IProperty.buildBoolean("valid", Boolean.TRUE)));
            events.add(FeatureCreationRequestEvent.build(metadata, feature));
        }

        featureService.registerRequests(events, new HashSet<String>(), ArrayListMultimap.create());

        assertEquals(NB_FEATURES.longValue(), this.featureCreationRequestRepo.count());

        featureService.scheduleRequests();

        waitFeature(NB_FEATURES, null, 3600_000);
        LOGGER.info(">>>>>>>>>>>>>>>>> {} creation requests done in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - creationStart);

        assertEquals(NB_FEATURES.longValue(), this.featureRepo.count());
    }

}
