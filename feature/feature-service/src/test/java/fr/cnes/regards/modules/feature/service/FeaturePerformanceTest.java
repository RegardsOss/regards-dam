package fr.cnes.regards.modules.feature.service;

import static org.junit.Assert.assertEquals;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.assertj.core.util.Lists;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import fr.cnes.regards.framework.geojson.geometry.IGeometry;
import fr.cnes.regards.framework.oais.urn.EntityType;
import fr.cnes.regards.modules.feature.domain.FeatureEntity;
import fr.cnes.regards.modules.feature.domain.request.FeatureCreationRequest;
import fr.cnes.regards.modules.feature.dto.Feature;
import fr.cnes.regards.modules.feature.dto.FeatureSessionMetadata;
import fr.cnes.regards.modules.feature.dto.PriorityLevel;
import fr.cnes.regards.modules.feature.dto.event.in.FeatureCreationRequestEvent;
import fr.cnes.regards.modules.model.dto.properties.IProperty;
import fr.cnes.regards.modules.model.dto.properties.ObjectProperty;

@TestPropertySource(properties = { "spring.jpa.properties.hibernate.default_schema=feature_perf",
        "regards.amqp.enabled=true", "spring.jpa.properties.hibernate.jdbc.batch_size=1024",
        "spring.jpa.properties.hibernate.order_inserts=true" })
@ActiveProfiles({ "testAmqp", "noscheduler", "nohandler" })
public class FeaturePerformanceTest extends AbstractFeatureMultitenantServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(FeaturePerformanceTest.class);

    private static final Integer NB_FEATURES = 1000;

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

        long start = System.currentTimeMillis();
        String format = "F%05d";

        // Register creation requests
        FeatureSessionMetadata metadata = FeatureSessionMetadata.build("sessionOwner", "session", PriorityLevel.AVERAGE,
                                                                       Lists.emptyList());
        String modelName = mockModelClient("model_geode.xml");

        Thread.sleep(5_000);

        List<FeatureCreationRequestEvent> events = new ArrayList<>();
        for (int i = 1; i <= NB_FEATURES; i++) {
            String id = String.format(format, i);
            Feature feature = Feature.build(id, null, IGeometry.unlocated(), EntityType.DATA, modelName);
            addGeodeProperties(feature);
            events.add(FeatureCreationRequestEvent.build(metadata, feature));
        }

        featureService.registerRequests(events);

        assertEquals(NB_FEATURES.longValue(), this.featureCreationRequestRepo.count());

        boolean schedule;
        do {
            schedule = featureService.scheduleRequests();
        } while (schedule);

        waitFeature(NB_FEATURES, null, 3600_000);

        LOGGER.info(">>>>>>>>>>>>>>>>> {} requests processed in {} ms", NB_FEATURES,
                    System.currentTimeMillis() - start);

        assertEquals(NB_FEATURES.longValue(), this.featureRepo.count());
    }

    private void addGeodeProperties(Feature feature) {
        // System
        ObjectProperty system = IProperty.buildObject("system", IProperty.buildInteger("filesize", 8648),
                                                      IProperty.buildDate("creation_date", OffsetDateTime.now()),
                                                      IProperty.buildDate("modification_date", OffsetDateTime.now()),
                                                      IProperty.buildStringArray("urls", "file://home/geode/test.tar"),
                                                      IProperty.buildString("filename", "test.tar"),
                                                      IProperty.buildString("checksum",
                                                                            "4e188bd8a6288164c25c3728ce394927"),
                                                      IProperty.buildString("extension", "tar"));
        // File infos
        ObjectProperty fileInfos = IProperty.buildObject("file_infos", IProperty.buildString("type", "L0A_LR_Packet"),
                                                         IProperty.buildString("nature", "TM"),
                                                         IProperty.buildString("date_type", "BEGINEND"),
                                                         IProperty.buildString("level", "L0A"),
                                                         IProperty.buildDate("production_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("utc_start_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("utc_end_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("tai_start_date", OffsetDateTime.now()),
                                                         IProperty.buildDate("tai_end_date", OffsetDateTime.now()),
                                                         IProperty.buildBoolean("valid", true));
        // Ground segment
        ObjectProperty groundSegment = IProperty
                .buildObject("ground_segment", IProperty.buildBoolean("sended", true),
                             IProperty.buildDate("sending_date", OffsetDateTime.now()),
                             IProperty.buildStringArray("recipients", "JPL", "REGARDS"),
                             IProperty.buildBoolean("archived", true),
                             IProperty.buildDate("archiving_date", OffsetDateTime.now()),
                             IProperty.buildBoolean("public", false), IProperty.buildBoolean("distributed", false),
                             IProperty.buildBoolean("restored", false), IProperty.buildString("state", "NOT ARCHIVED"));

        // SWOT
        ObjectProperty swot = IProperty
                .buildObject("swot", IProperty.buildString("CRID", "crid"),
                             IProperty.buildInteger("product_counter", 1),
                             IProperty.buildBoolean("is_last_version", true), IProperty.buildString("station", "KUX"),
                             IProperty.buildDate("day_date", OffsetDateTime.now()), IProperty.buildInteger("cycle", 23),
                             IProperty.buildInteger("pass", 125), IProperty.buildInteger("tile", 25),
                             IProperty.buildString("tile_side", "Full"), IProperty.buildString("granule_type", "Cycle"),
                             IProperty.buildStringArray("continent_id", "eu"),
                             IProperty.buildString("bassin_id", "bass1"));
        // CORPUS
        ObjectProperty corpus = IProperty.buildObject("corpus", IProperty.buildInteger("corpus_id", 10),
                                                      IProperty.buildString("corpus_lot", "lot2"));

        feature.setProperties(IProperty.set(system, fileInfos, groundSegment, swot, corpus));
    }

    @SuppressWarnings("unused")
    private void addDefaultProperties(Feature feature) {
        feature.addProperty(IProperty.buildString("data_type", "TYPE01"));
        feature.addProperty(IProperty.buildObject("file_characterization",
                                                  IProperty.buildBoolean("valid", Boolean.TRUE)));
    }
}
